package pl.umcs.rafalkloc.server.core;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.server.actions.ServerLogoutAction;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoreServer {
    private static CoreServer mInstance;

    private ServerSocket mServerSocket;
    private final List<ConnectedClientHandler> mConnectedClients;

    private CoreServer()
    {
        try {
            mServerSocket = new ServerSocket(7698);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        mConnectedClients = Collections.synchronizedList(new LinkedList<>());
    }

    public static CoreServer instance()
    {
        if (mInstance == null)
            mInstance = new CoreServer();

        return mInstance;
    }

    public void runServer()
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true) {
            try {
                Socket newClientSocket = mServerSocket.accept();
                ConnectedClientHandler connectedClientHandler = new ConnectedClientHandler(newClientSocket);
                mConnectedClients.add(connectedClientHandler);
                executorService.execute(connectedClientHandler);
            } catch (IOException ignored) {
            }
        }
    }

    public void sendToAllFromList(ClientMessage message, String receivers)
    {
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setActionNumber(message.getActionNumber());
        serverMessage.addBodyElem("Message", message.getBodyElem("Message"));

        for (ConnectedClientHandler client : mConnectedClients) {
            if (receivers.contains(client.getUsername())) {
                client.send(serverMessage);
            }
        }
    }

    public void disconnect(ConnectedClientHandler client)
    {
        ServerLogoutAction.logout(client.getUsername());
        mConnectedClients.remove(client);
    }

    public static void main(String[] args)
    {
        CoreServer server = CoreServer.instance();
        server.runServer();
    }
}
