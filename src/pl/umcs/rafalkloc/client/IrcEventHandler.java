package pl.umcs.rafalkloc.client;

import pl.umcs.rafalkloc.common.ServerMessage;

public interface IrcEventHandler {

    public void handleEvent(ServerMessage message);
}
