package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateRoomAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 6;
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
                String query = "SELECT COUNT(*) FROM IRC_ROOMS WHERE name=?;";
                PreparedStatement statement = getDatabaseConnection().getStatement(query);
                statement.setString(1, msg.getBodyElem("Room"));
                ResultSet result = statement.executeQuery();
                if (result.getInt(1) == 1) {
                    setError("Provided room's name already exists. Try another one.");
                    statement.close();
                    return false;
                }
                statement.close();
            }

            {
                // Create new room
                String query = "INSERT INTO IRC_ROOMS (name) VALUES (?);";
                PreparedStatement statement = getDatabaseConnection().getStatement(query);
                statement.setString(1, msg.getBodyElem("Room"));

                statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setError("Some errors occurred while creating new room.");
            return false;
        }

        return true;
    }
}
