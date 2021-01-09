package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListRoomAction extends ActionBase {

    @Override
    public int getActionNumber()
    {
        return 7;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {
        try {
            PreparedStatement statement = getStatementForQuery("SELECT name from IRC_ROOMS ORDER BY name;",
                                                               new String[]{});

            ResultSet result = statement.executeQuery();
            StringBuilder roomsList = new StringBuilder();

            while (result.next()) {
                roomsList.append(result.getString(1));
                roomsList.append(';');
            }
            statement.close();

            addResponseBodyElem("Rooms", roomsList.toString());
        } catch (SQLException exception) {
            exception.printStackTrace();
            setError("Some errors occurred while listing rooms.");
            return false;
        }

        return true;
    }
}
