package com.mecolab.memeticameandroid.Models;


import android.content.Context;

import com.mecolab.memeticameandroid.Networking.Listeners;
import com.mecolab.memeticameandroid.Networking.NetworkingManager;
import com.mecolab.memeticameandroid.Persistence.DatabaseManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Message {
    public enum MessageType{
        TEXT, IMAGE, VIDEO, AUDIO, OTHER, NOT_SET
    }

    private static String[] ImageMimeTypes = { "image/jpeg", "image/jpg", "image/png" };
    private static String[] VideoMimeTypes = { "video/x-mpeg", "video/quicktime", "video/mp4" };
    private static String[] AudioMimeTypes = { "audio/mpeg3", "audio/x-mpeg-3", "audio/mpeg",
            "audio/mp3","audio/mp4","audio/ogg", "audio/wav"};
    private static String TextMimeType = "plain/text";

    public int mId;
    public final int mServerId;
    public final String mSender;
    public final String mContent;
    public final MessageType mType;
    public final String mMimeType;
    public final int mConversationId;
    public final String mDate;

    protected Message(int id, int serverId, String sender, String content, MessageType type,
                   String mime_type, int conversationId, String date) {
        mId = id;
        mServerId = serverId;
        mSender = sender;
        mContent = content;
        mType = type;
        mMimeType = mime_type;
        mConversationId = conversationId;
        mDate = date;
    }

    public static class Builder {
        private int mId = -1;
        private int mServerId = -1;
        private MessageType mType = MessageType.NOT_SET;
        private String mMimeType = "plain/text";
        private String mDate = "";
        private String mContent = "";
        private String mSender = "";
        private int mConversationId = -1;

        public Message build() {
            if(mDate.equals("")) {
                /*TODO: */
                mDate = "";
            }
            if(mConversationId == -1 || mSender.equals("") || mType == MessageType.NOT_SET)
                return null;
            return new Message(mId, mServerId, mSender, mContent, mType, mMimeType,
                        mConversationId, mDate);
        }

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Builder setSender(String sender) {
            mSender = sender;
            return this;
        }

        public Builder setConversationId(int conversationId) {
            mConversationId = conversationId;
            return this;
        }

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setServerId(int serverId) {
            mServerId = serverId;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            mMimeType = mimeType;
            if (Arrays.asList(ImageMimeTypes).contains(mMimeType)) {
                mType = MessageType.IMAGE;
            }
            else if (Arrays.asList(VideoMimeTypes).contains(mMimeType)) {
                mType = MessageType.VIDEO;
            }
            else if (Arrays.asList(AudioMimeTypes).contains(mMimeType)) {
                mType = MessageType.AUDIO;
            }
            else if (TextMimeType.equals(mimeType)) {
                mType = MessageType.TEXT;
            }
            else {
                mType = MessageType.OTHER;
            }
            return this;
        }

        public Builder setDate(String date) {
            mDate = date;
            return this;
        }
    }

    public static ArrayList<Message> getMessages(Context context, Conversation conversation,
                                                 Listeners.OnGetMessagesListener listener){
        ArrayList<Message> savedMessages = DatabaseManager.getInstance(context).getMessages(conversation.mServerId);
        int index = savedMessages.size() - 1;
        int id = -1;
        if (index > 0) {
            id = savedMessages.get(index).mServerId;
        }
        NetworkingManager.getInstance(context).getMessages(conversation.mServerId,
                id, savedMessages, listener);
        return savedMessages;
    }

    public void sendMessage(Context context, Listeners.SendMessageListener listener) {
        NetworkingManager.getInstance(context).sendMessage(this, listener);
    }

    public boolean save(Context context){
        return DatabaseManager.getInstance(context).insertMessage(this) > 0;
    }

    public static Message getMessage(Context context, int serverId) {
        return DatabaseManager.getInstance(context).getMessage(serverId);
    }
}
