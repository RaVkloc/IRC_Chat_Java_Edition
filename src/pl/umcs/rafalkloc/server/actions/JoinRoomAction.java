package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

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
    public boolean execute(ClientMessage msg)
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
                // Set current room to user
                String query = "UPDATE IRC_USERS SET curr_room_name=? WHERE token=?";
                PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Room"),
                                                                                       msg.getBodyElem("Token")});
                statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setError("Some errors occurred while joining to room.");
            return false;
        }

        return true;
    }
}
