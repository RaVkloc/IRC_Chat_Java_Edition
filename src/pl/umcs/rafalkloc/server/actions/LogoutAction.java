package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LogoutAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 3;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {

        String query = "UPDATE IRC_USERS SET token = NULL, curr_room_name = NULL WHERE username = ?";
        try {
            if (!validateToken(msg)) {
                setError("Invalid token.");
                return false;
            }

            PreparedStatement statement = getStatementForQuery(query, new String[]{msg.getBodyElem("Username")});
            statement.executeUpdate();
            statement.close();
        } catch (SQLException ignored) {
            setError("Some errors occurred during logging out.");
            return false;
        }

        return true;
    }
}
