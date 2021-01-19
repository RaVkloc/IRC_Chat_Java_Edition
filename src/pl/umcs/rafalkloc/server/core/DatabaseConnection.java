package pl.umcs.rafalkloc.server.core;

import pl.umcs.rafalkloc.common.FillBeforeRun;

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
            mConnection = DriverManager.getConnection("jdbc:sqlite:" + FillBeforeRun.DATABASE_PATH);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public PreparedStatement getStatement(String sqlQuery) throws SQLException
    {
        return mConnection.prepareStatement(sqlQuery);
    }
}


