package com.mecolab.memeticameandroid.Models;


import android.content.Context;

import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.Networking.NetworkingManager;
import com.mecolab.memeticameandroid.Persistence.DatabaseManager;

import java.util.ArrayList;

public class Conversation {

    public static final String SERVER_ID = "conversation_server_id";

    public final int mId;
    public final int mServerId;
    public final String mTitle;
    public final String mAdminPhone;
    public final String mCreatedAt;
    public final boolean mIsGroup;
    public boolean mHasNewMessage;

    public ArrayList<User> mParticipants;

    public Conversation(int id, int serverId, String title, String adminPhone, String createdAt,
                        boolean isGroup, ArrayList<User> participants){
        mId = id;
        mServerId = serverId;
        mTitle = title;
        mAdminPhone = adminPhone;
        mCreatedAt = createdAt;
        mIsGroup = isGroup;
        mParticipants = participants;
    }

    public boolean addParticipant(Context context, User user,
                                  Listeners.OnUserAddedListener listener) {

        if(mParticipants.contains(user))
            return false;
        NetworkingManager.getInstance(context).addParticipantToConversation
                (mServerId, user.mPhoneNumber, listener);
        mParticipants.add(user);
        return true;
    }

    public static void createNewGroupConversation(Context context, String title,
                                 ArrayList<User> participants,
                                                  Listeners.OnGetNewGroupConversationListener listener){

        NetworkingManager.getInstance(context).newGroupConversation(participants, title, listener);
    }

    public static void createNewTwoConversation(Context context, User me, User user,
                                                Listeners.OnGetNewTwoConversationListener listener){

        NetworkingManager.getInstance(context).newTwoConversation(me, user, listener);
    }

    public static Conversation getTwoConversation(Context context, User me, User user){
        ArrayList<Conversation> conversations = DatabaseManager.getInstance(context).
                getConversations();
        for(Conversation c : conversations){
            if(!c.mIsGroup && c.mParticipants.contains(user) && c.mParticipants.contains(me)){
                return c;
            }
        }
        return null;
    }

    public static Conversation getConversation(Context context, int serverId) {
        return DatabaseManager.getInstance(context).getConversation(serverId);
    }

    /*Intenta guardar si es que no existe*/
    public boolean save(Context context){
        return DatabaseManager.getInstance(context).insertConversation(this);
    }

    public static ArrayList<Conversation> getConversations(Context context, User user,
                                                           Listeners.OnGetConversationsListener listener) {

        ArrayList<Conversation> saved = DatabaseManager.getInstance(context).
                getConversations(/*user.mPhoneNumber*/);
        NetworkingManager.getInstance(context).getConversations(user.mPhoneNumber, saved, listener);
        return saved;
    }

}
