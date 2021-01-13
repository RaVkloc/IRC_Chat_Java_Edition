package pl.umcs.rafalkloc.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.umcs.rafalkloc.common.ServerMessage;

import java.util.Optional;


public class ChatApplication extends Application implements IrcEventHandler {
    private Stage mStage;
    private TreeViewWithIrcEventHandler mRoomsTree;
    private ListViewWithIrcEventHandler mMessageList;
    private TextInputDialog mTextDialog;
    private final Client mClient;

    public ChatApplication()
    {
        mClient = new Client();
        mClient.addSubscriber(4, this);
        mClient.addSubscriber(5, this);
    }

    @Override
    public void start(Stage stage)
    {
        // Synchronous
        loginOrRegisterClient();
        if (!mClient.isLoggedIn()) {
            return;
        }

        // Client starts listening for responses from server
        new Thread(mClient).start();


        mStage = stage;
        mStage.setOnCloseRequest(event -> {
            Dialog<ButtonType> confirmExit = new Dialog<>();
            confirmExit.setContentText("You're logged in. Are you sure you want to quit?");
            confirmExit.setTitle("Exit");
            confirmExit.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
            confirmExit.initModality(Modality.WINDOW_MODAL);
            confirmExit.showAndWait().ifPresent(type -> {
                if (type.getButtonData().isDefaultButton()) {
                    Platform.exit();
                    System.exit(0);
                } else {
                    event.consume();
                }
            });


        });
        createGui();

        mClient.listRooms();

        mStage.show();
    }

    private void createGui()
    {
        initWidgets();

        mStage.setTitle("IRC Chat Java Edition");

        // Menu
        MenuBar menuBar = new MenuBar();

        // Room menu
        {
            Menu roomMenu = new Menu("Room");
            roomMenu.setGraphic(new ImageView(
                    "file:/home/klocrafi/IdeaProjects/IRC Chat JavaEdition/graphic/room_icon.png"));
            MenuItem create = new MenuItem("Create new room");
            create.setOnAction(e -> mTextDialog.showAndWait().ifPresent(name -> mClient.createRoom(name.trim())));

            MenuItem refresh = new MenuItem("Refresh rooms' list");
            refresh.setOnAction(e -> mClient.listRooms());

            MenuItem leave = new MenuItem("Leave room");
            leave.setOnAction(e -> mClient.leaveRoom());

            MenuItem users = new MenuItem("List users");
            users.setOnAction(e -> {
                if (mRoomsTree.getSelectionModel().getSelectedItem() != null)
                    mClient.listUsers(mRoomsTree.getSelectionModel().getSelectedItem().getValue());
            });

            roomMenu.getItems().addAll(create, refresh, leave, users);
            menuBar.getMenus().addAll(roomMenu);
        }

        //Session menu
        {
            Menu sessionMenu = new Menu("Session");
            MenuItem logout = new MenuItem("Logout");
            sessionMenu.getItems().addAll(logout);

            menuBar.getMenus().addAll(sessionMenu);
        }


        // Main widget
        SplitPane splitPane = new SplitPane();
        {
            mRoomsTree = new TreeViewWithIrcEventHandler(mClient);
            mRoomsTree.setRoot(new TreeItem<>("Rooms"));
            mClient.addSubscriber(7, mRoomsTree);
            mClient.addSubscriber(8, mRoomsTree);
            mRoomsTree.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (mRoomsTree.getSelectionModel().getSelectedItem() == null) {
                        mouseEvent.consume();
                        return;
                    }
                    String room = mRoomsTree.getSelectionModel().getSelectedItem().getValue();
                    mClient.joinRoom(room);
                }
            });
            VBox leftControl = new VBox(mRoomsTree);
            mMessageList = new ListViewWithIrcEventHandler();
            mClient.addSubscriber(9, mMessageList);
            TextArea messageEdit = new TextArea();
            messageEdit.setPromptText("Type your message here...");
            messageEdit.setPrefRowCount(5);
            Button sendButton = new Button("Send message");
            sendButton.setGraphic(new ImageView("file:/home/klocrafi/IdeaProjects/IRC Chat JavaEdition/graphic/send.png"));
            sendButton.autosize();
            sendButton.setOnAction(actionEvent -> {
                mClient.sendMessage(messageEdit.getText());
                messageEdit.clear();
            });
            VBox rightControl = new VBox(mMessageList, messageEdit, sendButton);
            rightControl.fillWidthProperty().setValue(true);
            rightControl.setAlignment(Pos.CENTER);
            rightControl.setSpacing(15);

            splitPane.getItems().addAll(leftControl, rightControl);
        }

        mStage.setScene(new Scene(new VBox(menuBar, splitPane), 800, 600));
    }

    private void initWidgets()
    {
        mTextDialog = new TextInputDialog();
        mTextDialog.setHeaderText("Create new room");
        mTextDialog.setContentText("Type name for a new room:");
        mTextDialog.initModality(Modality.WINDOW_MODAL);
    }

    @Override
    public void handleEvent(ServerMessage message)
    {
        if (!message.getBodyElem("Error").isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setResizable(false);
                alert.setTitle("Error");
                alert.setContentText(message.getBodyElem("Error"));

                alert.showAndWait();
            });

            return;
        }

        if (message.getActionNumber() == 4) { //join room
            String currentRoom = message.getBodyElem("Room");
            mClient.setCurrentRoom(currentRoom);
            Platform.runLater(() -> {
                mStage.setTitle("IRC Chat Java Edition @ " + currentRoom);
                mMessageList.getItems().clear();
            });
        }

        if (message.getActionNumber() == 5) { // leave room
            mClient.setCurrentRoom("");
            Platform.runLater(() -> {
                mStage.setTitle("IRC Chat Java Edition");
                mMessageList.getItems().clear();
            });
        }
    }

    private void loginOrRegisterClient()
    {
        LoginDialog loginDialog = new LoginDialog();

        Alert loginAlert = new Alert(Alert.AlertType.ERROR);
        loginAlert.setTitle("Sign in error");
        loginAlert.setHeaderText("Unable to sign in to server");
        loginAlert.setContentText("Invalid credentials provided or account does not exists.");

        Alert registerAlert = new Alert(Alert.AlertType.ERROR);
        registerAlert.setTitle("Sign up error");
        registerAlert.setHeaderText("Unable to sign up");
        registerAlert.setContentText("Username is already taken. Try another one.");

        Optional<LoginDialog.LoginDialogResult> loginResult;

        while (true) {
            loginResult = loginDialog.showAndWait();
            if (loginResult.isEmpty() || loginResult.get().username.isEmpty()) {
                return;
            }
            if (loginResult.get().login) {
                if (!mClient.login(loginResult.get().username, loginResult.get().password)) {
                    loginAlert.showAndWait();
                } else {
                    break;
                }
            } else {
                if (!mClient.register(loginResult.get().username, loginResult.get().password)) {
                    registerAlert.showAndWait();
                }
            }
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
