package pl.umcs.rafalkloc.client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;

public class DialogHelper {
    public static TextInputDialog getNameDialog()
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Create new room");
        dialog.setContentText("Type name for a new room:");
        dialog.initModality(Modality.WINDOW_MODAL);

        return dialog;
    }

    public static Dialog<ButtonType> getConfirmExitDialog()
    {
        Dialog<ButtonType> confirmExit = new Dialog<>();
        confirmExit.setContentText("You're logged in. Are you sure you want to quit?");
        confirmExit.setTitle("Exit");
        confirmExit.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        confirmExit.initModality(Modality.WINDOW_MODAL);

        return confirmExit;
    }

    public static Alert getErrorDialog(String header, String content)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(false);
        alert.setTitle("Error");
        if (!header.isEmpty()) {
            alert.setHeaderText(header);
        }
        alert.setContentText(content);
        alert.initModality(Modality.WINDOW_MODAL);

        return alert;
    }
}
