package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.server.core.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class ActionBase {
    private final DatabaseConnection mDatabaseConnection;
    private ServerMessage mResponse;
    protected final ArrayList<ServerMessage> mToSendAfterResponse;

    public ActionBase()
    {
        mDatabaseConnection = new DatabaseConnection();
        mToSendAfterResponse = new ArrayList<>();
    }

    public abstract int getActionNumber();

    public boolean execute(ClientMessage msg)
    {
        mResponse = new ServerMessage();
        mResponse.setActionNumber(getActionNumber());
        mToSendAfterResponse.clear();

        return executePriv(msg);
    }

    protected abstract boolean executePriv(ClientMessage msg);

    public ServerMessage getResponse()
    {
        return mResponse;
    }

    public boolean hasAdditionalMessages()
    {
        return mToSendAfterResponse.size() != 0;
    }

    public List<ServerMessage> getAdditionalMessages()
    {
        return mToSendAfterResponse;
    }

    protected void setError(String error)
    {
        mResponse.addBodyElem("Error", error);
    }

    protected DatabaseConnection getDatabaseConnection()
    {
        return mDatabaseConnection;
    }

    protected void addResponseBodyElem(String type, String value)
    {
        mResponse.addBodyElem(type, value);
    }

    protected boolean validateToken(ClientMessage message) throws SQLException
    {
        String query = "SELECT token FROM IRC_USERS WHERE username = ?";
        PreparedStatement statement = getStatementForQuery(query, new String[]{message.getBodyElem("Username")});
        ResultSet result = statement.executeQuery();

        boolean retVal = result.getString(1).equals(message.getToken());
        statement.close();
        return retVal;
    }

    protected PreparedStatement getStatementForQuery(String query, String[] args) throws SQLException
    {
        PreparedStatement statement = getDatabaseConnection().getStatement(query);

        for (int i = 0; i < args.length; i++) {
            // statement starts indexing parameters from 1
            statement.setString(i + 1, args[i]);
        }

        return statement;
    }

    protected String getUsernameFromToken(ClientMessage message) throws SQLException
    {
        String query = "SELECT username FROM IRC_USERS WHERE token = ?";
        PreparedStatement statement = getStatementForQuery(query, new String[]{message.getToken()});
        ResultSet result = statement.executeQuery();

        String retVal = result.getString(1);
        statement.close();
        return retVal;
    }

}
