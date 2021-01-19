package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.server.core.CoreServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JoinRoomAction extends ActionBase {
    @Override
    public int getActionNumber()
    {
        return 4;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {
        try {
            // Check if logged in
            if (!validateToken(msg)) {
                setError("Invalid token.");
                return false;
            }

            {
                //Check if room exists
                String query = "SELECT COUNT(*) FROM IRC_ROOMS WHERE name=?";
                PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Room")});
                ResultSet result = statement.executeQuery();
                if (result.getInt(1) == 0) {
                    setError("Required room does not exists.");
                    statement.close();
                    getResponse().addBodyElem("Room", "");
                    return false;
                }
                statement.close();
            }

            {
                // Set current room to user
                String query = "UPDATE IRC_USERS SET curr_room_name=? WHERE token=?";
                PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Room"),
                                                                                       msg.getToken()
                });
                statement.executeUpdate();
                statement.close();
            }

            {
                String query = "SELECT *" +
                        "FROM (" +
                        "         SELECT message, from_user, strftime('%d-%m-%Y %H:%M:%S', datetime(sent_timestamp, 'localtime')) as d" +
                        "         FROM IRC_MESSAGES" +
                        "         WHERE to_room = ?" +
                        "         ORDER BY sent_timestamp DESC" +
                        "         LIMIT 10" +
                        "     )" +
                        "ORDER BY d ASC;";
                PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Room")});
                ResultSet result = statement.executeQuery();

                StringBuilder builder = new StringBuilder();
                String messageTemplate = "[%s]@%s: %s;";
                while (result.next()) {
                    builder.append(messageTemplate.formatted(result.getString(3),
                                                             result.getString(2),
                                                             result.getString(1)));
                }
                statement.close();

                ServerMessage message = new ServerMessage();
                message.setActionNumber(10);
                message.addBodyElem("Message", builder.toString());

                mToSendAfterResponse.add(message);
            }

            // Inform other user that someone has joined to room
            {
                ListUsersInRoomAction action = new ListUsersInRoomAction();
                action.execute(msg);
                informOtherUsersInRoom(action.getResponse(), action.getResponse().getBodyElem("Users"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setError("Some errors occurred while joining to room.");
            getResponse().addBodyElem("Room", "");
            return false;
        }

        getResponse().addBodyElem("Room", msg.getBodyElem("Room"));
        return true;
    }

    private void informOtherUsersInRoom(ServerMessage message, String users)
    {
        CoreServer.instance().sendToAllFromList(message, users);
    }
}
