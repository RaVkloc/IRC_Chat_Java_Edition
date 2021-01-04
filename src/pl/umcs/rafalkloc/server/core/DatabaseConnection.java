package pl.umcs.rafalkloc.server.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    private Connection mConnection;

    public DatabaseConnection()
    {
        try {
            Class.forName("org.sqlite.JDBC");
            mConnection = DriverManager.getConnection(
                    "jdbc:sqlite:/home/klocrafi/IdeaProjects/IRC Chat JavaEdition/irc_chat_database.sqlite3");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public PreparedStatement getStatement(String sqlQuery) throws SQLException
    {
        return mConnection.prepareStatement(sqlQuery);
    }
}


