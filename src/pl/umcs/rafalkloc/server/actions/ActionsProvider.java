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
            mActionsMapper.put(action.getActionNumber(), action);
        }
        {
            LoginAction action = new LoginAction();
            mActionsMapper.put(action.getActionNumber(), action);
        }
        {
            LogoutAction action = new LogoutAction();
            mActionsMapper.put(action.getActionNumber(), action);
        }
    }
}
