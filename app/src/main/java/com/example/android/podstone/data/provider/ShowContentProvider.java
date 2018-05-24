package com.example.android.podstone.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class ShowContentProvider extends ContentProvider {

    private static final String[] SHOWS_PROJECTION = new String[]{
            ShowContract.ShowEntry._ID,
            ShowContract.ShowEntry.COLUMN_TITLE,
            ShowContract.ShowEntry.COLUMN_DESCRIPTION,
            ShowContract.ShowEntry.COLUMN_AUTHOR,
            ShowContract.ShowEntry.COLUMN_DATE,
            ShowContract.ShowEntry.COLUMN_IMAGE,
            ShowContract.ShowEntry.COLUMN_MEDIA_LINK,
            ShowContract.ShowEntry.COLUMN_RATING,
            ShowContract.ShowEntry.COLUMN_SIZE,
            ShowContract.ShowEntry.COLUMN_CHANNEL,
            ShowContract.ShowEntry.COLUMN_COPYRIGHT,
            ShowContract.ShowEntry.COLUMN_VOTES,
            ShowContract.ShowEntry.COLUMN_PLAYER_POS
    };

    public static final int SHOWS = 100;
    public static final int SHOW_ID = 101;
    public static final int IS_SHOW_IN_DB = 102;
    public static final int NO_SHOW_ID = -1;

    private ShowDbHelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(ShowContract.AUTHORITY, ShowContract.PATH_SHOWS, SHOWS);
        uriMatcher.addURI(ShowContract.AUTHORITY, ShowContract.PATH_SHOWS + "/#", SHOW_ID);
        uriMatcher.addURI(ShowContract.AUTHORITY, ShowContract.PATH_SHOWS + "/" + ShowContract.PATH_SHOW_IN_DB, IS_SHOW_IN_DB);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new ShowDbHelper(context);
        return true;
    }

    public static Uri getUri(int code, String id) {
        switch (code) {
            case IS_SHOW_IN_DB:
                return ShowContract.ShowEntry.CONTENT_FILTERED_URI;
            case SHOW_ID:
                return Uri.parse(ShowContract.ShowEntry.CONTENT_URI.toString()).buildUpon().appendPath(id).build();
            case SHOWS:
                return ShowContract.ShowEntry.CONTENT_URI;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor retCursor = null;
        int match = sUriMatcher.match(uri);
        String id;
        switch (match) {
            case SHOWS:
                retCursor = db.query(ShowContract.ShowEntry.TABLE_NAME,
                        SHOWS_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SHOW_ID:
                id = uri.getLastPathSegment();
                retCursor = db.query(ShowContract.ShowEntry.TABLE_NAME,
                        SHOWS_PROJECTION,
                        ShowContract.ShowEntry._ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;
            case IS_SHOW_IN_DB:
                retCursor = db.query(ShowContract.ShowEntry.TABLE_NAME,
                        new String[]{ShowContract.ShowEntry._ID},
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                break;
        }
        if (retCursor != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Uri returnUri;
        long id;
        switch (sUriMatcher.match(uri)) {
            case SHOWS:
                id = db.insert(ShowContract.ShowEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = ContentUris.withAppendedId(ShowContract.ShowEntry.CONTENT_URI, id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int showsDeleted = 0;
        String id = uri.getLastPathSegment();
        switch (sUriMatcher.match(uri)) {
            case SHOW_ID:
                showsDeleted = db.delete(ShowContract.ShowEntry.TABLE_NAME,
                        ShowContract.ShowEntry._ID + "=?",
                        new String[]{id}
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return showsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
