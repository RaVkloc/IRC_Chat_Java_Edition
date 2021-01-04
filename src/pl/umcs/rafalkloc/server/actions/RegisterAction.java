package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.server.core.DatabaseConnection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 1;
    }

    @Override
    public boolean execute(ClientMessage msg)
    {
        if (!checkMessageCorrectness(msg)) {
            setError("Empty username or password.");
            return false;
        }

        DatabaseConnection db_conn = getDatabaseConnection();

        // Check if such a username exists in DB.
        try {
            PreparedStatement statement = db_conn.getStatement("SELECT COUNT(*) FROM IRC_USERS WHERE username=?");
            statement.setString(1, msg.getBodyElem("Username"));
            ResultSet result = statement.executeQuery();

            if (result.getInt(1) != 0) {
                setError("Such username is already taken. Try another one.");
                statement.close();
                return false;
            }
            statement.close();
        } catch (SQLException ignore) {
            setError("Some errors occurred while executing query.");
            return false;
        }

        //Add new user to DB.
        try {
            PreparedStatement statement = db_conn.getStatement("INSERT INTO IRC_USERS(username, password) VALUES (?,?)");
            statement.setString(1, msg.getBodyElem("Username"));
            statement.setString(2, createHashForPassword(msg.getBodyElem("Password")));

            statement.executeUpdate();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            setError("Some errors occurred while executing query.");
            return false;
        } catch (NoSuchAlgorithmException ignored) {
            setError("Some errors occurred while processing password.");
            return false;
        }

        return true;
    }

    private boolean checkMessageCorrectness(ClientMessage msg)
    {
        return !msg.getBodyElem("Username").isEmpty() &&
                !msg.getBodyElem("Password").isEmpty();
    }

    private String createHashForPassword(String password) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        return new String(digest.digest(password.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
