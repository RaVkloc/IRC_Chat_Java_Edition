package pl.umcs.rafalkloc.client;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import pl.umcs.rafalkloc.common.ServerMessage;

public class TreeViewWithIrcEventHandler extends TreeView<String> implements IrcEventHandler {
    TreeItem<String> mLastUserListedRoom;

    public TreeViewWithIrcEventHandler(Client client)
    {
        super();
        mLastUserListedRoom = new TreeItem<>(); // fake for first usage
    }

    @Override
    public void handleEvent(ServerMessage message)
    {
        if (!message.getBodyElem("Error").isEmpty()) {
            Platform.runLater(() -> DialogHelper.getErrorDialog("",
                                                                message.getBodyElem("Error")).showAndWait());
            return;
        }

        if (message.getActionNumber() == 7) { //list rooms
            getRoot().getChildren().clear();

            for (String roomName : message.getBodyElem("Rooms").split(";")) {
                getRoot().getChildren().add(new TreeItem<>(roomName));
            }
        } else { // list users
            mLastUserListedRoom.getChildren().clear();
            mLastUserListedRoom = getSelectionModel().getSelectedItem();

            for (String username : message.getBodyElem("Users").split(";")) {
                mLastUserListedRoom.getChildren().add(new TreeItem<>(username));
            }

        }
    }
}
