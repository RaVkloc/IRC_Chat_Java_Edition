package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LoginAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 2;
    }

    @Override
    public boolean execute(ClientMessage msg)
    {
        String query = "SELECT COUNT(*) FROM IRC_USERS WHERE username=? AND password=?";
        try {
            PreparedStatement statement =
                    getStatementForQuery(query,
                                         new String[]{msg.getBodyElem("Username"),
                                                      createHashForPassword(msg.getBodyElem("Password"))
                                         });
            ResultSet result = statement.executeQuery();
            if (result.getInt(1) != 1) {
                setError("Invalid username or password");
                statement.close();
                return false;
            }
            statement.close();

        } catch (SQLException | NoSuchAlgorithmException ignored) {
            return false;
        }

        String token = UUID.randomUUID().toString();
        query = "UPDATE IRC_USERS SET token = ? WHERE username = ?";
        try {
            PreparedStatement statement = getStatementForQuery(query, new String[]{token,
                                                                                   msg.getBodyElem("Username")
            });

            statement.executeUpdate();
            statement.close();
        } catch (SQLException ignored) {
            return false;
        }

        addResponseBodyElem("Token", token);

        return true;
    }

    private String createHashForPassword(String password) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        return new String(digest.digest(password.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
