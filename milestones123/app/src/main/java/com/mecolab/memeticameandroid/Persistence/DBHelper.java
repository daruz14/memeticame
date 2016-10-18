package com.mecolab.memeticameandroid.Persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by crojas on 27-10-15.
 */
public class DBHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "memeticame.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataSource.SQL_CREATE_USERS);
        db.execSQL(DataSource.SQL_CREATE_MESSAGES);
        db.execSQL(DataSource.SQL_CREATE_CONVERSATIONS);
        db.execSQL(DataSource.SQL_CREATE_USER_CONVERSATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DataSource.SQL_DELETE_CONVERSATIONS);
        db.execSQL(DataSource.SQL_CREATE_CONVERSATIONS);
        db.execSQL(DataSource.SQL_DELETE_MESSAGES);
        db.execSQL(DataSource.SQL_CREATE_MESSAGES);
        db.execSQL(DataSource.SQL_DELETE_USERS);
        db.execSQL(DataSource.SQL_CREATE_USERS);
        db.execSQL(DataSource.SQL_DELETE_USER_CONVERSATIONS);
        db.execSQL(DataSource.SQL_CREATE_USER_CONVERSATIONS);
    }
}