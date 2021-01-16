package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ServerMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceiveArchiveMessagesAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 10;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {
        try {
            if (!validateToken(msg)) {
                setError("Invalid token. Please login and try again.");
                return false;
            }

            {
                String query = "SELECT message, from_user, datetime(sent_timestamp, 'localtime') FROM IRC_MESSAGES " +
                        "WHERE to_room = ? " +
                        "AND sent_timestamp BETWEEN datetime(?, 'localtime') AND datetime(?, 'localtime') " +
                        "ORDER BY sent_timestamp;";
                PreparedStatement statement = getStatementForQuery(query,
                                                                   new String[]{msg.getBodyElem("Room"),
                                                                                msg.getBodyElem("DateFrom"),
                                                                                msg.getBodyElem("DateTill")
                                                                   });

                ResultSet result = statement.executeQuery();
                ServerMessage message;
                while (result.next()) {
                    message = new ServerMessage();
                    message.setActionNumber(10);
                    message.addBodyElem("Message", "[%s]@%s: %s".formatted(result.getString(3),
                                                                           result.getString(2),
                                                                           result.getString(1)));
                    mToSendAfterResponse.add(message);
                }
                result.close();
                statement.close();
            }
        } catch (SQLException exception) {
            setError(exception.getMessage());
            return false;
        }

        return true;
    }
}
