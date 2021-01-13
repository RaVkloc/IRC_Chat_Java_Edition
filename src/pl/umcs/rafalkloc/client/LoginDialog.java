package pl.umcs.rafalkloc.client;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class LoginDialog extends Dialog<LoginDialog.LoginDialogResult> {
    public static class LoginDialogResult {
        String username;
        String password;
        boolean login;
    }

    private final LoginDialogResult mResult;

    public LoginDialog()
    {
        super();

        mResult = new LoginDialogResult();
        createGui();
    }

    private void createGui()
    {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(createLoginTab(), createRegisterTab());

        getDialogPane().setContent(tabs);
        getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        setTitle("Login/Register");

        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData().isCancelButton())
                return null;

            if (tabs.getSelectionModel().getSelectedItem().getText().equals("Login"))
                mResult.login = true;

            return mResult;
        });

        this.initModality(Modality.APPLICATION_MODAL);
    }

    private Tab createLoginTab()
    {
        Label usernameLabel = new Label("Username:");
        TextField usernameEdit = new TextField();
        usernameLabel.setLabelFor(usernameEdit);
        usernameEdit.setPromptText("Username...");
        usernameEdit.textProperty().addListener((observable, oldValue, newValue) -> mResult.username = newValue);
        HBox usernameBox = new HBox(usernameLabel, usernameEdit);
        usernameBox.setSpacing(20);
        usernameBox.setAlignment(Pos.CENTER);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordEdit = new PasswordField();
        passwordLabel.setLabelFor(passwordEdit);
        passwordEdit.setPromptText("Password...");
        passwordEdit.textProperty().addListener((observable, oldValue, newValue) -> mResult.password = newValue);
        HBox passwordBox = new HBox(passwordLabel, passwordEdit);
        passwordBox.setSpacing(20);
        passwordBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(usernameBox, passwordBox);
        layout.setSpacing(10);

        return new Tab("Login", layout);
    }

    private Tab createRegisterTab()
    {
        Label usernameLabel = new Label("Username:");
        TextField usernameEdit = new TextField();
        usernameLabel.setLabelFor(usernameEdit);
        usernameEdit.setPromptText("Username...");
        usernameEdit.textProperty().addListener((observable, oldValue, newValue) -> mResult.username = newValue);
        HBox usernameBox = new HBox(usernameLabel, usernameEdit);
        usernameBox.setSpacing(20);
        usernameBox.setAlignment(Pos.CENTER);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordEdit = new PasswordField();
        passwordLabel.setLabelFor(passwordEdit);
        passwordEdit.setPromptText("Password...");
        passwordEdit.textProperty().addListener((observable, oldValue, newValue) -> mResult.password = newValue);

        PasswordField passwordEditRepeat = new PasswordField();
        passwordEditRepeat.setPromptText("Repeat password...");
        VBox passwordsBox = new VBox(passwordEdit, passwordEditRepeat);
        passwordsBox.setSpacing(5);

        HBox passwordBox = new HBox(passwordLabel, passwordsBox);
        passwordBox.setSpacing(20);
        passwordBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(usernameBox, passwordBox);
        layout.setSpacing(10);

        return new Tab("Register", layout);
    }
}
