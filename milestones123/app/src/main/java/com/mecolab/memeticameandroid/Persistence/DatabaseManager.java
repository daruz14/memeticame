package com.mecolab.memeticameandroid.Persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mecolab.memeticameandroid.Models.Conversation;
import com.mecolab.memeticameandroid.Models.Message;
import com.mecolab.memeticameandroid.Models.User;

import java.util.ArrayList;



public class DatabaseManager {

    private static DataSource dataSource;
    private static DatabaseManager sInstance;
    private static Context mContext;

    private DatabaseManager(Context context){
        dataSource = new DataSource(context);
        mContext = context;
    }

    public static synchronized DatabaseManager getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DatabaseManager(context.getApplicationContext());
        }
        else if (mContext == null)
            mContext = context;
        return sInstance;
    }

    public void insertContact(User contact){
        ContentValues values = new ContentValues();
        User existing = getContact(contact.mPhoneNumber);
        if(existing != null)
            return;
        values.put(DataSource.ColumnUser.USER_COLUMN_PHONE_NUMBER, contact.mPhoneNumber);
        values.put(DataSource.ColumnUser.USER_COLUMN_NAME, contact.mName);
        values.put(DataSource.ColumnUser.USER_COLUMN_SERVER_ID, contact.mServerId);
        dataSource.database.insert(DataSource.USER_TABLE_NAME, null, values);
    }

    public ArrayList<User> getContacts(){
        Cursor c = dataSource.database.query(
                DataSource.USER_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        User me = User.getLoggedUser(mContext);
        ArrayList<User> users = new ArrayList<>();
        if(c.getCount() == 0){
            c.close();
            return users;
        }
        c.moveToFirst();
        do{
            String phoneNumber = c.getString(c.getColumnIndexOrThrow(DataSource.ColumnUser.USER_COLUMN_PHONE_NUMBER));
            if(me == null){
                Log.e("DB-Manager.getContacts", "detLoggedUser returned null.");
                return null;
            }
            if (!me.mPhoneNumber.equals(phoneNumber))
                users.add(new User(
                        0,
                        c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnUser.USER_COLUMN_SERVER_ID)),
                        phoneNumber,
                        c.getString(c.getColumnIndexOrThrow(DataSource.ColumnUser.USER_COLUMN_NAME))
                ));
        } while(c.moveToNext());
        c.close();
        return users;
    }

    public User getContact(String phone){
        Cursor c = dataSource.database.query(
                DataSource.USER_TABLE_NAME,
                null,
                DataSource.ColumnUser.USER_COLUMN_PHONE_NUMBER + "=?",
                new String[] { phone },
                null,
                null,
                null
        );
        c.moveToFirst();
        if (c.getCount() == 0){
            c.close();
            return null;
        }
        User user = new User(
                c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnUser.ID_USER)),
                c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnUser.USER_COLUMN_SERVER_ID)),
                c.getString(c.getColumnIndexOrThrow(DataSource.ColumnUser.USER_COLUMN_PHONE_NUMBER)),
                c.getString(c.getColumnIndexOrThrow(DataSource.ColumnUser.USER_COLUMN_NAME)));
        c.close();
        return user;
    }

    public ArrayList<Message> getMessages(int conversation_id){
        Cursor c = dataSource.database.query(
                DataSource.MESSAGE_TABLE_NAME,
                null,
                DataSource.ColumnMessage.MESSAGE_COLUMN_CONVERSATION_ID + "=?",
                new String[] { Integer.toString(conversation_id) },
                null,
                null,
                DataSource.ColumnMessage.MESSAGE_COLUMN_SERVER_ID + " ASC");

        c.moveToFirst();
        ArrayList<Message> messages = new ArrayList<>();
        if(c.getCount() != 0)
            do{
                Message.Builder builder = new Message.Builder();
                builder.setId(c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_ID)));
                builder.setServerId(c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_SERVER_ID)));
                builder.setDate(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_DATE)));
                builder.setMimeType(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_MIME_TYPE)));
                builder.setContent(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_CONTENT)));
                builder.setSender(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_SENDER)));
                builder.setConversationId(c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_CONVERSATION_ID)));
                Message msg = builder.build();
                messages.add(msg);
            } while(c.moveToNext());
        c.close();
        return messages;
    }

    public Message getMessage(int serverId){
        Cursor c = dataSource.database.query(
                DataSource.MESSAGE_TABLE_NAME,
                null,
                DataSource.ColumnMessage.MESSAGE_COLUMN_SERVER_ID + "=?",
                new String[] { Integer.toString(serverId) },
                null,
                null,
                DataSource.ColumnMessage.MESSAGE_COLUMN_SERVER_ID + " ASC"
        );
        c.moveToFirst();
        if(c.getCount() == 0) {
            c.close();
            return null;
        }
        Message msg = new Message.Builder()
                    .setId(c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_ID)))
                    .setServerId(c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_SERVER_ID)))
                    .setDate(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_DATE)))
                    .setMimeType(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_MIME_TYPE)))
                    .setContent(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_CONTENT)))
                    .setSender(c.getString(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_SENDER)))
                    .setConversationId(c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnMessage.MESSAGE_COLUMN_CONVERSATION_ID)))
                    .build();
        c.close();
        return msg;
    }

    public long insertMessage(Message msg){
        ContentValues values = new ContentValues();
        if (getMessage(msg.mServerId) != null)
            return -1;
        values.put(DataSource.ColumnMessage.MESSAGE_COLUMN_SERVER_ID, msg.mServerId);
        values.put(DataSource.ColumnMessage.MESSAGE_COLUMN_MIME_TYPE, msg.mMimeType);
        values.put(DataSource.ColumnMessage.MESSAGE_COLUMN_SENDER, msg.mSender);
        values.put(DataSource.ColumnMessage.MESSAGE_COLUMN_CONTENT, msg.mContent);
        values.put(DataSource.ColumnMessage.MESSAGE_COLUMN_CONVERSATION_ID, msg.mConversationId);
        values.put(DataSource.ColumnMessage.MESSAGE_COLUMN_DATE, msg.mDate);
        return dataSource.database.insert(DataSource.MESSAGE_TABLE_NAME, null, values);
    }

    public Conversation getConversation(int serverId){
        Cursor c = dataSource.database.query(
                DataSource.CONVERSATION_TABLE_NAME,
                null,
                DataSource.ColumnConversation.CONVERSATION_COLUMN_SERVER_ID + "=?",
                new String[]{Integer.toString(serverId)},
                null,
                null,
                null);
        c.moveToFirst();
        if (c.getCount() == 0){
            c.close();
            return null;
        }
        boolean group = c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_GROUP)) == 1;
        ArrayList<User> participants = getConversationParticipants(serverId);

        Conversation conversation = new Conversation(
                c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_ID)),
                serverId,
                c.getString(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_TITLE)),
                c.getString(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_ADMIN_PHONE)),
                c.getString(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_CREATED_AT)),
                group,
                participants);
        c.close();
        return conversation;
    }

    public ArrayList<Conversation> getConversations(/*String userPhone*/){
         ArrayList<Conversation> conversations = new ArrayList<>();
         Cursor c = dataSource.database.query(
                 DataSource.CONVERSATION_TABLE_NAME,
                 null,
                 null, // DataSource.ColumnConversation.CONVERSATION_COLUMN_ADMIN_PHONE + "=?",
                 null, // new String[]{userPhone},
                 null,
                 null,
                 null
         );
        c.moveToFirst();
        if(c.getCount() == 0){
            c.close();
            return conversations;
        }

        do{
            boolean group = c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_GROUP)) == 1;
            int serverId = c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_SERVER_ID));
            Conversation conversation = new Conversation(
                    c.getInt(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_ID)),
                    serverId,
                    c.getString(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_TITLE)),
                    c.getString(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_ADMIN_PHONE)),
                    c.getString(c.getColumnIndexOrThrow(DataSource.ColumnConversation.CONVERSATION_COLUMN_CREATED_AT)),
                    group,
                    getConversationParticipants(serverId));
            conversations.add(conversation);
        } while(c.moveToNext());
        c.close();
        return conversations;
    }

    public void insertConversationParticipant(int ConversationServerId, String userPhone){
        ContentValues values = new ContentValues();
        values.put(DataSource.ColumnUserConversations.USER_CONVERSATION_CONVERSATION_ID, ConversationServerId);
        values.put(DataSource.ColumnUserConversations.USER_CONVERSATION_USER_PHONE, userPhone);
        dataSource.database.insert(DataSource.USER_CONVERSATION_TABLE_NAME, null, values);
    }

    public ArrayList<User> getConversationParticipants(int ConversationServerId){
        ArrayList<User> participants = new ArrayList<>();
        Cursor c = dataSource.database.query(
                 DataSource.USER_CONVERSATION_TABLE_NAME,
                 null,
                 DataSource.ColumnUserConversations.USER_CONVERSATION_CONVERSATION_ID + "=?",
                 new String[]{Integer.toString(ConversationServerId)},
                 null,
                 null,
                 null
         );
        c.moveToFirst();
        if(c.getCount() == 0){
            c.close();
            return participants;
        }

        do{
            User user = User.getUserOrCreate(mContext,
                            c.getString(c.getColumnIndexOrThrow(DataSource.ColumnUserConversations
                            .USER_CONVERSATION_USER_PHONE)));
            participants.add(user);

        } while(c.moveToNext());
        c.close();
        return participants;
    }

    public boolean insertConversation(Conversation conversation){
        Conversation existing = Conversation.getConversation(mContext, conversation.mServerId);
        if(existing != null)
            return false;
        ContentValues values = new ContentValues();
        values.put(DataSource.ColumnConversation.CONVERSATION_COLUMN_SERVER_ID, conversation.mServerId);
        values.put(DataSource.ColumnConversation.CONVERSATION_COLUMN_TITLE, conversation.mTitle);
        values.put(DataSource.ColumnConversation.CONVERSATION_COLUMN_ADMIN_PHONE, conversation.mAdminPhone);
        values.put(DataSource.ColumnConversation.CONVERSATION_COLUMN_CREATED_AT, conversation.mCreatedAt);
        values.put(DataSource.ColumnConversation.CONVERSATION_COLUMN_GROUP, conversation.mIsGroup);
        dataSource.database.insert(DataSource.CONVERSATION_TABLE_NAME, null, values);

        for(User user : conversation.mParticipants){
            insertConversationParticipant(conversation.mServerId, user.mPhoneNumber);
        }
        return true;
    }
}
