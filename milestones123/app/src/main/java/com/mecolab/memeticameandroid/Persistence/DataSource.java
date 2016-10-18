package com.mecolab.memeticameandroid.Persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by crojas on 27-10-15.
 */
public class DataSource {

    private DBHelper openHelper;
    public final SQLiteDatabase database;

    //Metainformación de la base de datos
    public static final String USER_TABLE_NAME = "Users";
    public static final String MESSAGE_TABLE_NAME = "Messages";
    public static final String UNSENT_MESSAGE_TABLE_NAME = "UnsentMessages";
    public static final String CONVERSATION_TABLE_NAME = "Conversations";
    public static final String USER_CONVERSATION_TABLE_NAME = "UserConversations";
    public static final String STRING_TYPE = " text ";
    public static final String INT_TYPE = " integer ";
    public static final String COMMA_SEP = ",";

    //Campos de la tabla Quotes
    public static class ColumnUser{
        public static final String ID_USER = BaseColumns._ID;
        public static final String USER_COLUMN_PHONE_NUMBER = "phone";
        public static final String USER_COLUMN_NAME = "name";
        public static final String USER_COLUMN_SERVER_ID = "server_id";
    }

    public static class ColumnMessage{
        public static final String MESSAGE_COLUMN_ID = BaseColumns._ID;
        public static final String MESSAGE_COLUMN_SENDER = "sender";
        public static final String MESSAGE_COLUMN_CONTENT = "content";
        //public static final String MESSAGE_COLUMN_TYPE = "type";
        public static final String MESSAGE_COLUMN_MIME_TYPE = "mime_type";
        public static final String MESSAGE_COLUMN_CONVERSATION_ID = "conversation_id";
        public static final String MESSAGE_COLUMN_DATE = "date";
        public static final String MESSAGE_COLUMN_SERVER_ID = "server_id";
    }

    public static class ColumnConversation{
        public static final String CONVERSATION_COLUMN_ID = BaseColumns._ID;
        public static final String CONVERSATION_COLUMN_SERVER_ID = "server_id";
        public static final String CONVERSATION_COLUMN_TITLE = "title";
        public static final String CONVERSATION_COLUMN_CREATED_AT = "created_at";
        public static final String CONVERSATION_COLUMN_ADMIN_PHONE = "admin_phone";
        public static final String CONVERSATION_COLUMN_GROUP = "is_group";
    }

    public static class ColumnUserConversations{
        public static String USER_CONVERSATION_USER_PHONE = "user_phone";
        public static String USER_CONVERSATION_CONVERSATION_ID = "conversation_id";
    }

    public static final String SQL_CREATE_USERS = "create table "+ USER_TABLE_NAME + "(" +
            ColumnUser.ID_USER + INT_TYPE + "primary key autoincrement," +
            ColumnUser.USER_COLUMN_SERVER_ID + INT_TYPE + "not null," +
            ColumnUser.USER_COLUMN_PHONE_NUMBER + STRING_TYPE + "not null," +
            ColumnUser.USER_COLUMN_NAME + STRING_TYPE + ")";

    public static final String SQL_DELETE_USERS = "DROP TABLE IF EXISTS " + USER_TABLE_NAME;

    public static final String SQL_CREATE_MESSAGES = "create table " + MESSAGE_TABLE_NAME + " (" +
            ColumnMessage.MESSAGE_COLUMN_ID + INT_TYPE + "primary key autoincrement," +
            ColumnMessage.MESSAGE_COLUMN_SERVER_ID + INT_TYPE  + "unique," +
            ColumnMessage.MESSAGE_COLUMN_SENDER + STRING_TYPE  + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_CONTENT + STRING_TYPE  + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_MIME_TYPE + STRING_TYPE  + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_DATE + STRING_TYPE  + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_CONVERSATION_ID + INT_TYPE  + ")";

    public static final String SQL_DELETE_MESSAGES = "DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME;

    public static final String SQL_CREATE_UNSENT_MESSAGES = "create table " + UNSENT_MESSAGE_TABLE_NAME + " (" +
            ColumnMessage.MESSAGE_COLUMN_ID + INT_TYPE + "primary key autoincrement," +
            ColumnMessage.MESSAGE_COLUMN_SENDER + STRING_TYPE + "NOT NULL," +
            ColumnMessage.MESSAGE_COLUMN_CONTENT + STRING_TYPE + COMMA_SEP +
            //ColumnMessage.MESSAGE_COLUMN_TYPE + STRING_TYPE + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_MIME_TYPE + STRING_TYPE  + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_DATE + STRING_TYPE  + COMMA_SEP +
            ColumnMessage.MESSAGE_COLUMN_CONVERSATION_ID + INT_TYPE + ")";

    public static final String SQL_DELETE_UNSENT_MESSAGES = "DROP TABLE IF EXISTS " + UNSENT_MESSAGE_TABLE_NAME;

    public static final String SQL_CREATE_CONVERSATIONS = "CREATE TABLE " + CONVERSATION_TABLE_NAME + " (" +
            ColumnConversation.CONVERSATION_COLUMN_ID  + INT_TYPE + "primary key autoincrement," +
            ColumnConversation.CONVERSATION_COLUMN_SERVER_ID + INT_TYPE + "not null," +
            ColumnConversation.CONVERSATION_COLUMN_TITLE + STRING_TYPE + COMMA_SEP +
            ColumnConversation.CONVERSATION_COLUMN_CREATED_AT + STRING_TYPE + COMMA_SEP +
            ColumnConversation.CONVERSATION_COLUMN_ADMIN_PHONE + STRING_TYPE + COMMA_SEP +
            ColumnConversation.CONVERSATION_COLUMN_GROUP + INT_TYPE + " )";

    public static final String SQL_DELETE_CONVERSATIONS = "DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME;

    public static final String SQL_CREATE_USER_CONVERSATIONS = "CREATE TABLE " + USER_CONVERSATION_TABLE_NAME + " (" +
            ColumnUserConversations.USER_CONVERSATION_USER_PHONE + STRING_TYPE + "not null," +
            ColumnUserConversations.USER_CONVERSATION_CONVERSATION_ID + INT_TYPE + "not null )";

    public static final String SQL_DELETE_USER_CONVERSATIONS = "DROP TABLE IF EXISTS " + USER_CONVERSATION_TABLE_NAME;

    public DataSource(Context context) {
        //Creando una instancia hacia la base de datos
        openHelper = new DBHelper(context);
        database = openHelper.getWritableDatabase();
    }
}