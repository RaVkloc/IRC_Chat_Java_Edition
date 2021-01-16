package pl.umcs.rafalkloc.client;

import pl.umcs.rafalkloc.common.ClientMessage;
import pl.umcs.rafalkloc.common.ClientMessageCH;
import pl.umcs.rafalkloc.common.ServerMessage;
import pl.umcs.rafalkloc.common.ServerMessageCH;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client implements Runnable {
    private static class UserData {
        String token = "";
        String username = "";
        String currentRoomName = "";
    }

    private final Map<Integer, Set<IrcEventHandler>> mReceivers;

    private Scanner mInput;
    private PrintWriter mOutput;
    private final UserData mUserData;


    Client()
    {
        try {
            Socket mSocket = new Socket("localhost", 7698);
            mInput = new Scanner(mSocket.getInputStream());
            mOutput = new PrintWriter(mSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mUserData = new UserData();
        mReceivers = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            mReceivers.put(i, new HashSet<>());
        }
    }

    public boolean isLoggedIn()
    {
        return !mUserData.username.isEmpty() && !mUserData.token.isEmpty();
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

            assert response != null;
            for (IrcEventHandler receiver : mReceivers.get(response.getActionNumber())) {
                receiver.handleEvent(response);
            }
        }
    }

    public void setCurrentRoom(final String currentRoom)
    {
        mUserData.currentRoomName = currentRoom;
    }

    private void sendToServer(ClientMessage message)
    {
        mOutput.println(ClientMessageCH.serializeToString(message));
    }

    private ClientMessage getDefaultClientMessage()
    {
        ClientMessage defaultMessage = new ClientMessage();
        defaultMessage.setToken(mUserData.token);
        defaultMessage.addBodyElem("Username", mUserData.username);

        return defaultMessage;
    }

    public boolean login(String username, String password)
    {
        ClientMessage loginMessage = new ClientMessage();
        loginMessage.setActionNumber(2);
        loginMessage.addBodyElem("Username", username);
        loginMessage.addBodyElem("Password", password);

        sendToServer(loginMessage);
        mUserData.username = username;

        ServerMessage response = ServerMessageCH.deserializeFromString(mInput.nextLine());
        assert response != null;
        if (response.getBodyElem("Error").isEmpty()) {
            mUserData.token = response.getBodyElem("Token");
            return true;
        }

        return false;
    }

    public void logout()
    {
        ClientMessage logoutMessage = getDefaultClientMessage();
        logoutMessage.setActionNumber(3);

        sendToServer(logoutMessage);
    }

    public boolean register(String username, String password)
    {
        ClientMessage registerMessage = new ClientMessage();
        registerMessage.setActionNumber(1);
        registerMessage.addBodyElem("Username", username);
        registerMessage.addBodyElem("Password", password);

        sendToServer(registerMessage);

        ServerMessage response = ServerMessageCH.deserializeFromString(mInput.nextLine());
        assert response != null;
        return response.getBodyElem("Error").isEmpty();
    }

    public void listRooms()
    {
        ClientMessage listRoomMessage = getDefaultClientMessage();
        listRoomMessage.setActionNumber(7);

        sendToServer(listRoomMessage);
    }

    public void listUsers(String roomName)
    {
        ClientMessage listUsersInRoom = getDefaultClientMessage();
        listUsersInRoom.setActionNumber(8);
        listUsersInRoom.addBodyElem("Room", roomName);

        sendToServer(listUsersInRoom);
    }

    public void joinRoom(String roomName)
    {
        ClientMessage joinRoomMessage = getDefaultClientMessage();
        joinRoomMessage.setActionNumber(4);
        joinRoomMessage.addBodyElem("Room", roomName);

        sendToServer(joinRoomMessage);

        mUserData.currentRoomName = roomName;
    }

    public void leaveRoom()
    {
        ClientMessage leaveRoomMessage = getDefaultClientMessage();
        leaveRoomMessage.setActionNumber(5);
        leaveRoomMessage.addBodyElem("Room", mUserData.currentRoomName);

        sendToServer(leaveRoomMessage);
    }

    public void createRoom(String roomName)
    {
        ClientMessage createRoomMessage = getDefaultClientMessage();
        createRoomMessage.setActionNumber(6);
        createRoomMessage.addBodyElem("Room", roomName);

        sendToServer(createRoomMessage);
        // to update rooms in list
        listRooms();
    }

    public void sendMessage(String text)
    {
        ClientMessage sendMessageMessage = getDefaultClientMessage();
        sendMessageMessage.setActionNumber(9);
        sendMessageMessage.addBodyElem("Room", mUserData.currentRoomName);
        sendMessageMessage.addBodyElem("Message", text);

        sendToServer(sendMessageMessage);
    }

    public void archiveMessages(final Date from, final Date till)
    {
        ClientMessage archive = getDefaultClientMessage();
        archive.setActionNumber(10);
        archive.addBodyElem("Room", mUserData.currentRoomName);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        archive.addBodyElem("DateFrom", formatter.format(from));
        archive.addBodyElem("DateTill", formatter.format(till));

        sendToServer(archive);
    }

}
