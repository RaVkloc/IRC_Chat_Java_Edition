package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

public class UnsupportedAction extends ActionBase {
    @Override
    public int getActionNumber()
    {
        return -1;
    }

    @Override
    public boolean execute(ClientMessage msg)
    {
        setError("Unsupported action.");
        return true;
    }
}
