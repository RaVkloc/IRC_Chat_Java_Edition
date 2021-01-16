package pl.umcs.rafalkloc.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.umcs.rafalkloc.client.weather.WeatherWidget;
import pl.umcs.rafalkloc.common.ServerMessage;

import java.util.Optional;


public class ChatApplication extends Application implements IrcEventHandler {
    private Stage mStage;
    private TreeViewWithIrcEventHandler mRoomsTree;
    private ListViewWithIrcEventHandler mMessageList;
    private VBox mRightControl;
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
        mStage.setOnCloseRequest(event -> DialogHelper.getConfirmExitDialog().showAndWait().ifPresent(type -> {
            if (type.getButtonData().isDefaultButton()) {
                Platform.exit();
                System.exit(0);
            } else {
                event.consume();
            }
        }));


        createGui();
        disableMessagingPart(true);

        mClient.listRooms();

        mStage.show();
    }

    private void createGui()
    {
        mStage.setTitle("IRC Chat Java Edition");

        // Menu
        MenuBar menuBar = new MenuBar();

        // Room menu
        {
            Menu roomMenu = new Menu("Room");
            roomMenu.setGraphic(new ImageView(
                    "file:/home/klocrafi/IdeaProjects/IRC Chat JavaEdition/graphic/room_icon.png"));
            MenuItem create = new MenuItem("Create new room");
            create.setOnAction(e -> DialogHelper.getNameDialog().showAndWait().ifPresent(name -> mClient.createRoom(name.trim())));

            MenuItem refresh = new MenuItem("Refresh rooms' list");
            refresh.setOnAction(e -> mClient.listRooms());

            MenuItem leave = new MenuItem("Leave room");
            leave.setOnAction(e -> mClient.leaveRoom());

            MenuItem users = new MenuItem("List users");
            users.setOnAction(e -> {
                if (mRoomsTree.getSelectionModel().getSelectedItem() != null)
                    mClient.listUsers(mRoomsTree.getSelectionModel().getSelectedItem().getValue());
            });

            MenuItem archiveMessages = new MenuItem("Get archive messages");
            archiveMessages.setOnAction(actionEvent -> DialogHelper.getTimeRangeDialog().showAndWait()
                    .ifPresent(dateDatePair -> mClient.archiveMessages(dateDatePair.getKey(), dateDatePair.getValue())
                    ));

            roomMenu.getItems().addAll(create, refresh, leave, users, archiveMessages);
            menuBar.getMenus().addAll(roomMenu);
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
            VBox leftControl = new VBox(mRoomsTree, new WeatherWidget());
            mMessageList = new ListViewWithIrcEventHandler();
            mClient.addSubscriber(9, mMessageList);
            mClient.addSubscriber(10, mMessageList);
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
            mRightControl = new VBox(mMessageList, messageEdit, sendButton);
            mRightControl.fillWidthProperty().setValue(true);
            mRightControl.setAlignment(Pos.CENTER);
            mRightControl.setSpacing(15);

            splitPane.getItems().addAll(leftControl, mRightControl);
        }

        mStage.setScene(new Scene(new VBox(menuBar, splitPane), 800, 600));
    }

    private void disableMessagingPart(boolean disabled)
    {
        mRightControl.setDisable(disabled);
    }

    @Override
    public void handleEvent(ServerMessage message)
    {
        if (!message.getBodyElem("Error").isEmpty()) {
            Platform.runLater(() -> DialogHelper.getErrorDialog("",
                                                                message.getBodyElem("Error")).showAndWait());

            return;
        }

        if (message.getActionNumber() == 4) { //join room
            String currentRoom = message.getBodyElem("Room");
            mClient.setCurrentRoom(currentRoom);
            Platform.runLater(() -> {
                mStage.setTitle("IRC Chat Java Edition @ " + currentRoom);
                mMessageList.getItems().clear();
                disableMessagingPart(false);
            });
        } else if (message.getActionNumber() == 5) { // leave room
            mClient.setCurrentRoom("");
            Platform.runLater(() -> {
                mStage.setTitle("IRC Chat Java Edition");
                mMessageList.getItems().clear();
                disableMessagingPart(true);
            });
        }
    }

    private void loginOrRegisterClient()
    {
        LoginDialog loginDialog = new LoginDialog();
        Alert loginAlert = DialogHelper.getErrorDialog("Unable to sign in to server",
                                                       "Invalid credentials provided or account does not exists.");
        Alert registerAlert = DialogHelper.getErrorDialog("Unable to sign up.",
                                                          "Username is already taken. Try another one.");

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
