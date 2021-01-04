package pl.umcs.rafalkloc.server.core;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ClientMessageCH;
import pl.umcs.rafalkloc.common.ServerMessageCH;
import pl.umcs.rafalkloc.server.actions.ActionBase;
import pl.umcs.rafalkloc.server.actions.ActionsProvider;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConnectedClientHandler implements Runnable {
    private final Socket mClientSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public ConnectedClientHandler(Socket clientSocket)
    {
        mClientSocket = clientSocket;
        try {
            mInputStream = clientSocket.getInputStream();
            mOutputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run()
    {
        Scanner in = new Scanner(mInputStream, StandardCharsets.UTF_8);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(mOutputStream, StandardCharsets.UTF_8), true);
        ClientMessage clientMessage;
        ActionsProvider provider = new ActionsProvider();

        while (in.hasNextLine()) {
            String msg = in.nextLine();

            clientMessage = ClientMessageCH.deserializeFromString(msg);
            ActionBase action = provider.getAction(clientMessage);
            action.execute(clientMessage);
            String responseString = ServerMessageCH.serializeToString(action.getResponse());
            out.println(responseString);
        }
    }
}
