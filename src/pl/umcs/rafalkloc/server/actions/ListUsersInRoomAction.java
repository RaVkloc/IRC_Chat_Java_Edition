package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListUsersInRoomAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 8;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {
        try {
            if (!validateToken(msg)) {
                setError("Invalid token. Sign in and try again.");
                return false;
            }
            String query = "SELECT username FROM IRC_USERS WHERE curr_room_name = ? ORDER BY username";
            PreparedStatement statement = getStatementForQuery(query,
                                                               new String[]{msg.getBodyElem("Room")});
            ResultSet result = statement.executeQuery();
            StringBuilder usersList = new StringBuilder();
            while (result.next()) {
                usersList.append(result.getString(1));
                usersList.append(';');
            }

            statement.close();
            addResponseBodyElem("Users", usersList.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            setError("Some errors occurred.");
            return false;
        }

        return true;
    }
}
