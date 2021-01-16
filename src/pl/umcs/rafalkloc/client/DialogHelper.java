package pl.umcs.rafalkloc.client;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static Dialog<Pair<Date, Date>> getTimeRangeDialog()
    {
        Dialog<Pair<Date, Date>> timeRangeDialog = new Dialog<>();
        timeRangeDialog.initModality(Modality.WINDOW_MODAL);
        timeRangeDialog.setTitle("Choose range");
        timeRangeDialog.setHeaderText("Choose range to get message.");
        timeRangeDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        Label fromDateLabel = new Label("From date:");
        DatePicker fromDateEdit = new DatePicker();
        fromDateEdit.setShowWeekNumbers(false);
        Tooltip fromTooltip = new Tooltip();
        fromTooltip.setShowDelay(Duration.millis(300));
        fromTooltip.setText("To get messages from specific time, type it after date.");
        fromDateEdit.setTooltip(fromTooltip);
        HBox fromBox = new HBox(fromDateLabel, fromDateEdit);
        fromBox.setSpacing(5);
        fromBox.setAlignment(Pos.CENTER_RIGHT);

        Label tillDateLabel = new Label("Till date:");
        DatePicker tillDateEdit = new DatePicker();
        tillDateEdit.setShowWeekNumbers(false);
        Tooltip tillTooltip = new Tooltip();
        tillTooltip.setShowDelay(Duration.millis(500));
        tillTooltip.setText("To get messages till specific time, type it after date.");
        tillDateEdit.setTooltip(tillTooltip);
        HBox tillBox = new HBox(tillDateLabel, tillDateEdit);
        tillBox.setSpacing(5);
        tillBox.setAlignment(Pos.CENTER_RIGHT);

        VBox widgets = new VBox(fromBox, tillBox);
        widgets.setAlignment(Pos.CENTER);
        widgets.setSpacing(10);

        timeRangeDialog.getDialogPane().setContent(widgets);
        timeRangeDialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() != ButtonBar.ButtonData.APPLY) {
                return null;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String fromTxt = fromDateEdit.getEditor().getText().trim(),
                    tillTxt = tillDateEdit.getEditor().getText().trim();
            try {
                return new Pair<>(formatter.parse(normalizeDateTime(fromTxt, true)),
                                  formatter.parse(normalizeDateTime(tillTxt, false)));
            } catch (ParseException e) {
                return null;
            }
        });

        return timeRangeDialog;
    }

    private static String normalizeDateTime(String datetime, boolean isFromDate)
    {
        if (datetime.contains(" ")) {
            if (datetime.split(":").length == 1) { // typed only hour
                datetime += isFromDate ? ":00:00" : ":59:59";
            } else if (datetime.split(":").length == 2) { // typed HH:MM
                datetime += isFromDate ? ":00" : ":59";
            }
        } else {
            datetime += isFromDate ? " 00:00:00" : " 23:59:59";
        }

        return datetime;
    }
}
