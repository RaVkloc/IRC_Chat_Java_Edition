package pl.umcs.rafalkloc.common;


public class ClientMessage extends ServerMessage {
    private String mToken;

    public String getToken()
    {
        return mToken;
    }

    public void setToken(String token)
    {
        mToken = token;
    }
}
