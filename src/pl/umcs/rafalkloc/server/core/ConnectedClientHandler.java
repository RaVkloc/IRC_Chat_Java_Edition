package pl.umcs.rafalkloc.server.core;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ClientMessageCH;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.common.ServerMessageCH;
import pl.umcs.rafalkloc.server.actions.ActionBase;
import pl.umcs.rafalkloc.server.actions.ActionsProvider;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConnectedClientHandler implements Runnable {
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private PrintWriter mPrintWriter;
    private String mUsername;

    public ConnectedClientHandler(Socket clientSocket)
    {
        mUsername = "";
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
        mPrintWriter = new PrintWriter(new OutputStreamWriter(mOutputStream, StandardCharsets.UTF_8), true);
        ClientMessage clientMessage;
        ActionsProvider provider = new ActionsProvider();

        while (in.hasNextLine()) {
            String msg = in.nextLine();

            clientMessage = ClientMessageCH.deserializeFromString(msg);
            ActionBase action = provider.getAction(clientMessage);
            if (action.execute(clientMessage) && clientMessage.getActionNumber() == 2) {
                mUsername = clientMessage.getBodyElem("Username");
            }

            send(action.getResponse());
        }

        CoreServer.instance().disconnect(this);
    }

    public String getUsername()
    {
        return mUsername;
    }

    public void send(ServerMessage message)
    {
        mPrintWriter.println(ServerMessageCH.serializeToString(message));
    }
}
