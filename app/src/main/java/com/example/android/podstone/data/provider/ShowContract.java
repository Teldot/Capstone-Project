package com.example.android.podstone.data.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ShowContract {

    public static final String AUTHORITY = "com.example.android.podstone";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    static final String PATH_SHOWS = "shows";
    static final String PATH_SHOW_IN_DB = "is_in_db";

    public static final class ShowEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOWS).build();
        public static final Uri CONTENT_FILTERED_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOWS).appendPath(PATH_SHOW_IN_DB).build();

        static final String TABLE_NAME = "show";
        public static final String COLUMN_TITLE = "tilte";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_MEDIA_LINK = "media_link";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_CHANNEL = "channel";
        public static final String COLUMN_COPYRIGHT = "copyright";
        public static final String COLUMN_VOTES = "votes";
        public static final String COLUMN_PLAYER_POS = "player_pos";
    }


}
