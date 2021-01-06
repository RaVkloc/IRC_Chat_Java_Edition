package pl.umcs.rafalkloc.server.core;

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

    public static void main(String[] args)
    {
        CoreServer server = CoreServer.instance();
        server.runServer();
    }
}
