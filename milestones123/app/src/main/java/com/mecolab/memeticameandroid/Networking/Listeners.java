package com.mecolab.memeticameandroid.Networking;

import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;

import java.util.ArrayList;

/**
 * Created by crojas on 07-12-15.
 */
public abstract class Listeners {
    public interface OnAuthListener{
        void onUserReceived(User user);
    }
    public interface ServerGetContactsListener{
        void onContactsReceived(ArrayList<User> users);
    }
    public interface SendMessageListener{
        void onMessageSent(Message msg);
    }
    public interface OnGetConversationsListener {
        void onConversationsReceived(ArrayList<Conversation> conversations);
    }
    public interface OnGetMessagesListener {
        void onMessagesReceived(ArrayList<Message> messages);
    }
    public interface OnGetNewTwoConversationListener{
        void onConversationReceived(Conversation conversation);
    }
    public interface OnGetNewGroupConversationListener{
        void onConversationReceived(Conversation conversation);
    }
    public interface OnUserAddedListener{
        void onUserAdded(boolean successful);
    }
}
