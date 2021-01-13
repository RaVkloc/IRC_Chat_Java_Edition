package pl.umcs.rafalkloc.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import pl.umcs.rafalkloc.common.ServerMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ListViewWithIrcEventHandler extends ListView<String> implements IrcEventHandler {

    public ListViewWithIrcEventHandler()
    {
        super();
    }

    @Override
    public void handleEvent(ServerMessage message)
    {
        Platform.runLater(() -> {
            if (!message.getBodyElem("Error").isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(message.getBodyElem("Error"));
                alert.showAndWait();
                return;
            }
            this.getItems().addAll(Arrays.stream(message.getBodyElem("Message").split(";"))
                                           .filter(e -> !e.isEmpty())
                                           .collect(Collectors.toList()));
            this.scrollTo(this.getItems().size());
        });
    }
}
