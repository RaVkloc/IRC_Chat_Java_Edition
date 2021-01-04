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
    public boolean execute(ClientMessage msg)
    {

        String query = "UPDATE IRC_USERS SET token = NULL WHERE username = ?";
        try {
            if (!validateToken(msg)) {
                setError("Invalid token.");
                return false;
            }

            PreparedStatement statement = getDatabaseConnection().getStatement(query);
            statement.setString(1, msg.getBodyElem("Username"));

            statement.executeUpdate();
            statement.close();
        } catch (SQLException ignored) {
            setError("Some errors occurred.");
            return false;
        }

        return true;
    }
}
