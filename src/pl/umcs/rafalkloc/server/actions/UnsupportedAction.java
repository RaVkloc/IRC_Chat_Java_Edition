package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

public class UnsupportedAction extends ActionBase {
    @Override
    public int getActionNumber()
    {
        return -1;
    }

    @Override
    protected boolean executePriv(ClientMessage msg)
    {
        setError("Unsupported action.");
        return true;
    }
}
