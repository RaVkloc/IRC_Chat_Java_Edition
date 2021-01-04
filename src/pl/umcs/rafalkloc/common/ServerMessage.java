package pl.umcs.rafalkloc.common;

import java.util.HashMap;
import java.util.Map;

public class ServerMessage {
    private int mActionNumber;
    private Map<String, String> mBody;

    public ServerMessage()
    {
        mBody = new HashMap<>();
    }

    public int getActionNumber()
    {
        return mActionNumber;
    }

    public void setActionNumber(int actionNumber)
    {
        mActionNumber = actionNumber;
    }

    public Map<String, String> getBody()
    {
        return mBody;
    }

    public void setBody(Map<String, String> body)
    {
        mBody = body;
    }

    public void addBodyElem(String type, String text)
    {
        mBody.put(type, text);
    }

    public String getBodyElem(String type)
    {
        return mBody.getOrDefault(type, "");
    }
}
