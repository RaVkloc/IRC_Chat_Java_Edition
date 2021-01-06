package pl.umcs.rafalkloc.server.actions;

import pl.umcs.rafalkloc.common.ClientMessage;

import java.util.HashMap;
import java.util.Map;

public class ActionsProvider {
    private Map<Integer, ActionBase> mActionsMapper;
    private UnsupportedAction mUnsupportedAction;

    public ActionsProvider()
    {
        mActionsMapper = new HashMap<>();
        mUnsupportedAction = new UnsupportedAction();

        registerActions();
    }

    public ActionBase getAction(ClientMessage msg)
    {
        if (msg == null) {
            return mUnsupportedAction;
        }
        return mActionsMapper.getOrDefault(msg.getActionNumber(), mUnsupportedAction);
    }

    private void registerActions()
    {
        {
            RegisterAction action = new RegisterAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 1
        }
        {
            LoginAction action = new LoginAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 2
        }
        {
            LogoutAction action = new LogoutAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 3
        }
        {
            CreateRoomAction action = new CreateRoomAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 6
        }
        {
            JoinRoomAction action = new JoinRoomAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 4
        }
        {
            LeaveRoomAction action = new LeaveRoomAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 5
        }
        {
            ListRoomAction action = new ListRoomAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 7
        }
        {
            ListUsersInRoomAction action = new ListUsersInRoomAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 8
        }
        {
            SendMessageAction action = new SendMessageAction();
            mActionsMapper.put(action.getActionNumber(), action);   // 9
        }
    }
}
