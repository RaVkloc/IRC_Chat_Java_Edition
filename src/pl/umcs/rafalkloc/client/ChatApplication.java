package pl.umcs.rafalkloc.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;


public class ChatApplication extends Application {
    private Stage mStage;
    private ListView mListView;
    private final Client mClient;

    private Thread mClientThread;


    public ChatApplication()
    {
        mClient = new Client();
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
            Platform.exit();
            System.exit(0);
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
            create.setOnAction(actionEvent -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setHeaderText("Create new room");
                dialog.setContentText("Type name for a new room:");
                Optional<String> name = dialog.showAndWait();
                mClient.createRoom(name.orElse(""));
            });
            MenuItem leave = new MenuItem("Leave room");
            create.setOnAction(actionEvent -> {
                mClient.leaveRoom();
            });
            MenuItem users = new MenuItem("List users");
            users.setOnAction(actionEvent -> {

            });
            roomMenu.getItems().addAll(create, leave, users);
            menuBar.getMenus().addAll(roomMenu);
        }

        //Session menu
        {
            Menu sessionMenu = new Menu("Session");
            MenuItem logout = new MenuItem("Logout");

            menuBar.getMenus().addAll(sessionMenu);
        }


        // Main widget
        SplitPane splitPane = new SplitPane();
        {
            VBox leftControl = new VBox(mListView);
            ListViewWithIrcEventHandler messageList = new ListViewWithIrcEventHandler("Message");
            mClient.addSubscriber(9, messageList);
            TextArea messageEdit = new TextArea();
            messageEdit.setPromptText("Type your message here...");
            messageEdit.setPrefRowCount(5);
            Button sendButton = new Button("Send message");
            sendButton.autosize();
            sendButton.setOnAction(actionEvent -> mClient.sendMessage(messageEdit.getText()));
            VBox rightControl = new VBox(messageList, messageEdit, sendButton);
            rightControl.fillWidthProperty().setValue(true);
            rightControl.setAlignment(Pos.CENTER);
            rightControl.setSpacing(15);

            splitPane.getItems().addAll(leftControl, rightControl);
        }

        mStage.setScene(new Scene(new VBox(menuBar, splitPane), 800, 600));
    }

    private void initWidgets()
    {
        mListView = new ListViewWithIrcEventHandler("Rooms");
        mClient.addSubscriber(7, (IrcEventHandler) mListView);

        mListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    mStage.setTitle("IRC Chat Java Edition @ " + mListView.getSelectionModel().getSelectedItem());
                    mClient.joinRoom(mListView.getSelectionModel().getSelectedItem().toString());
                }
            }
        });

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
            if (!loginResult.isPresent() || loginResult.get().username.isEmpty()) {
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
