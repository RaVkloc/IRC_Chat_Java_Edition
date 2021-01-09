package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.server.core.CoreServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMessageAction extends ActionBase {
    @Override
    public int getActionNumber()
    {
        return 9;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {
        try {
            if (!validateToken(msg)) {
                setError("Invalid token. Sign in and try again.");
                return false;
            }

            saveMessageInDatabase(msg);
            String receivers = getMessageReceivers(msg);
            sendMessageToUsers(msg, receivers);

        } catch (SQLException e) {
            e.printStackTrace();
            setError("Errors occurred while sending message.");
            return false;
        }
        return false;
    }

    private void saveMessageInDatabase(ClientMessage msg) throws SQLException
    {
        String query = "INSERT INTO IRC_MESSAGES (from_user, to_room, message, sent_timestamp) VALUES (?,?,?,datetime('now'));";
        PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("User"),
                                                                               msg.getBodyElem("Room"),
                                                                               msg.getBodyElem("Message")
        });

        statement.executeUpdate();
        statement.close();
    }

    private String getMessageReceivers(ClientMessage msg) throws SQLException
    {
        String query = "SELECT username FROM IRC_USERS WHERE curr_room_name=?";
        PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Room")});

        ResultSet result = statement.executeQuery();
        StringBuilder receivers = new StringBuilder();
        while (result.next()) {
            receivers.append(result.getString(1));
            receivers.append(';');
        }

        return receivers.toString();
    }

    private void sendMessageToUsers(ClientMessage msg, String receivers)
    {
        CoreServer.instance().sendToAllFromList(msg, receivers);
    }

}
