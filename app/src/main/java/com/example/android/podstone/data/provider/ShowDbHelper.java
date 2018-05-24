package com.example.android.podstone.data.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ShowDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "podstone.db";
    private static final int DATABASE_VERSION = 1;

    public ShowDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SHOW_TABLE = "CREATE TABLE " + ShowContract.ShowEntry.TABLE_NAME + "(" +
                ShowContract.ShowEntry._ID + " INTEGER PRIMARY KEY, " +
                ShowContract.ShowEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ShowContract.ShowEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                ShowContract.ShowEntry.COLUMN_AUTHOR + " TEXT NULL, " +
                ShowContract.ShowEntry.COLUMN_DATE + " TEXT NULL, " +
                ShowContract.ShowEntry.COLUMN_IMAGE + " BLOB NULL, " +
                ShowContract.ShowEntry.COLUMN_MEDIA_LINK + " TEXT NOT NULL, " +
                ShowContract.ShowEntry.COLUMN_RATING + " REAL NULL, " +
                ShowContract.ShowEntry.COLUMN_SIZE + " REAL NULL, " +
                ShowContract.ShowEntry.COLUMN_CHANNEL + " TEXT NULL, " +
                ShowContract.ShowEntry.COLUMN_COPYRIGHT + " TEXT NULL, " +
                ShowContract.ShowEntry.COLUMN_VOTES + " REAL NULL," +
                ShowContract.ShowEntry.COLUMN_PLAYER_POS + " REAL NULL);";

        db.execSQL(SQL_CREATE_SHOW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            final String SQL_DELETE_SHOW_TABLE = "DROP TABLE " + ShowContract.ShowEntry.TABLE_NAME + ";";
            db.execSQL(SQL_DELETE_SHOW_TABLE);
            onCreate(db);
        }
    }
}
