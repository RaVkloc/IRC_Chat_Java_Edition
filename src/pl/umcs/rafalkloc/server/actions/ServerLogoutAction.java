package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.server.core.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServerLogoutAction {
    public static void logout(String username)
    {
        DatabaseConnection db_conn = new DatabaseConnection();
        try {
            PreparedStatement statement = db_conn.getStatement(
                    "UPDATE IRC_USERS SET curr_room_name = NULL, token = NULL WHERE username = ?");
            statement.setString(1, username);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
