package pl.umcs.rafalkloc.client;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ClientMessageCH;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.common.ServerMessageCH;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Client implements Runnable {
    private class UserData {
        String mToken = "";
        String mUsername = "";
        String mCurrentRoomName = "";
    }

    private final Map<Integer, Set<IrcEventHandler>> mReceivers;

    private Socket mSocket;
    private Scanner mInput;
    private PrintWriter mOutput;
    private UserData mUserData;


    Client()
    {
        try {
            mSocket = new Socket("localhost", 7698);
            mInput = new Scanner(mSocket.getInputStream());
            mOutput = new PrintWriter(mSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mUserData = new UserData();
        mReceivers = new HashMap<>();
        for (int i = 1; i < 10; i++) {
            mReceivers.put(i, new HashSet<>());
        }
    }

    public boolean isLoggedIn()
    {
        return !mUserData.mUsername.isEmpty() && !mUserData.mToken.isEmpty();
    }

    public void addSubscriber(int actionNumber, IrcEventHandler subscriber)
    {
        mReceivers.get(actionNumber).add(subscriber);
    }

    @Override
    public void run()
    {
        ServerMessage response;
        while (mInput.hasNext()) {
            response = ServerMessageCH.deserializeFromString(mInput.nextLine());

            for (IrcEventHandler receiver : mReceivers.get(response.getActionNumber())) {
                receiver.handleEvent(response);
            }
        }
    }

    public void setCurrentRoom(final String currentRoom)
    {
        mUserData.mCurrentRoomName = currentRoom;
    }

    private void sendToServer(ClientMessage message)
    {
        mOutput.println(ClientMessageCH.serializeToString(message));
    }

    public boolean login(String username, String password)
    {
        ClientMessage loginMessage = new ClientMessage();
        loginMessage.setActionNumber(2);
        loginMessage.addBodyElem("Username", username);
        loginMessage.addBodyElem("Password", password);

        sendToServer(loginMessage);
        mUserData.mUsername = username;

        String s = mInput.nextLine();
        System.out.println(s);
        ServerMessage response = ServerMessageCH.deserializeFromString(s);
        if (response.getBodyElem("Error").isEmpty()) {
            mUserData.mToken = response.getBodyElem("Token");
            return true;
        }

        return false;
    }

    public boolean register(String username, String password)
    {
        ClientMessage registerMessage = new ClientMessage();
        registerMessage.setActionNumber(1);
        registerMessage.addBodyElem("Username", username);
        registerMessage.addBodyElem("Password", password);

        sendToServer(registerMessage);

        String s = mInput.nextLine();
        System.out.println(s);
        ServerMessage response = ServerMessageCH.deserializeFromString(s);
        return response.getBodyElem("Error").isEmpty();
    }

    public void listRooms()
    {
        ClientMessage listRoomMessage = new ClientMessage();
        listRoomMessage.setActionNumber(7);
        listRoomMessage.setToken(mUserData.mToken);

        sendToServer(listRoomMessage);
    }

    public void joinRoom(String roomName)
    {
        ClientMessage joinRoomMessage = new ClientMessage();
        joinRoomMessage.setActionNumber(4);
        joinRoomMessage.setToken(mUserData.mToken);
        joinRoomMessage.addBodyElem("Username", mUserData.mUsername);
        joinRoomMessage.addBodyElem("Room", roomName);

        sendToServer(joinRoomMessage);

        mUserData.mCurrentRoomName = roomName;
    }

    public void leaveRoom()
    {
        ClientMessage leaveRoomMessage = new ClientMessage();
        leaveRoomMessage.setActionNumber(5);
        leaveRoomMessage.setToken(mUserData.mToken);
        leaveRoomMessage.addBodyElem("Room", mUserData.mCurrentRoomName);

        sendToServer(leaveRoomMessage);
    }

    public void createRoom(String roomName)
    {
        ClientMessage createRoomMessage = new ClientMessage();
        createRoomMessage.setActionNumber(6);
        createRoomMessage.setToken(mUserData.mToken);
        createRoomMessage.addBodyElem("Room", roomName);

        sendToServer(createRoomMessage);
        // to update rooms in list
        listRooms();
    }

    public void sendMessage(String text)
    {
        ClientMessage sendMessageMessage = new ClientMessage();
        sendMessageMessage.setActionNumber(9);
        sendMessageMessage.setToken(mUserData.mToken);
        sendMessageMessage.addBodyElem("Room", mUserData.mCurrentRoomName);
        sendMessageMessage.addBodyElem("Username", mUserData.mUsername);
        sendMessageMessage.addBodyElem("Message", text);

        sendToServer(sendMessageMessage);
    }

}
