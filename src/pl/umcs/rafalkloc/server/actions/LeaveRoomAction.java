package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.server.core.CoreServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaveRoomAction extends ActionBase {
    @Override
    public int getActionNumber()
    {
        return 5;
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
                    return false;
                }
                statement.close();
            }

            {
                // Set current room to NULL
                String query = "UPDATE IRC_USERS SET curr_room_name = NULL WHERE token=?";
                PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Token")});
                statement.executeUpdate();
                statement.close();
            }

            // Inform other user that someone has left room
            {
                ListUsersInRoomAction action = new ListUsersInRoomAction();
                action.execute(msg);

                String currentUser = getUsernameFromToken(msg);
                String receivers = action.getResponse().getBodyElem("Users");
                // in case previous has still not been committed into DB
                action.getResponse().addBodyElem("Users", receivers.replace(currentUser + ";", ""));
                informOtherUsersInRoom(action.getResponse(), receivers);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setError("Some errors occurred while joining to room.");
            return false;
        }
        return true;
    }

    private void informOtherUsersInRoom(ServerMessage message, String users)
    {
        CoreServer.instance().sendToAllFromList(message, users);
    }
}
