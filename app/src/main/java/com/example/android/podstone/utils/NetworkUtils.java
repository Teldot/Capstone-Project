package com.example.android.podstone.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.example.android.podstone.R;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static URL buildURL(Context context, int task, String[] uriParts) {
        Uri builtUri = null;
        String LIMIT;
        String LIMIT_VAL;
        String OFFSET;
        String OFFSET_VAL;
        String LANG;
        String LANG_VAL;
        switch (task) {
            case FetchDataTask.TASK_STARTUP_LIST:
                String STARTUP_LIST = context.getString(R.string.PODCAST_DE_API_STARTUP_LIST);
                LIMIT = context.getString(R.string.PODCAST_DE_API_LIMIT);
                LIMIT_VAL = context.getString(R.string.PODCAST_DE_API_LIMIT_VAL);
                LANG = context.getString(R.string.PODCAST_DE_API_LANG);
                LANG_VAL = PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString(context.getString(R.string.pref_key_lang), context.getString(R.string.pref_key_lang_default));

                builtUri = Uri.parse(STARTUP_LIST).buildUpon()
                        .appendQueryParameter(LANG, LANG_VAL)
                        .appendQueryParameter(LIMIT, LIMIT_VAL)
                        .build();
                break;
            case FetchDataTask.TASK_SEARCH_LIST:
                String SEARCH_URL = context.getString(R.string.PODCAST_DE_API_SEARCH_URL);
                String QUERY = context.getString(R.string.PODCAST_DE_API_QUERY);
                LIMIT = context.getString(R.string.PODCAST_DE_API_LIMIT);
                OFFSET = context.getString(R.string.PODCAST_DE_API_OFFSET);
                String QUERY_VAL = uriParts[0];
                LIMIT_VAL = context.getString(R.string.PODCAST_DE_API_LIMIT_VAL);
                OFFSET_VAL = uriParts[1];

                builtUri = Uri.parse(SEARCH_URL).buildUpon()
                        .appendQueryParameter(QUERY, QUERY_VAL)
                        .appendQueryParameter(LIMIT, LIMIT_VAL)
                        .appendQueryParameter(OFFSET, OFFSET_VAL)
                        .build();
                break;
            case FetchDataTask.TASK_SINGLE_CHANNEL:
                String CHANNEL_URL = context.getString(R.string.PODCAST_DE_API_CHANNEL_ULR);
                LIMIT = context.getString(R.string.PODCAST_DE_API_LIMIT);
                LIMIT_VAL = context.getString(R.string.PODCAST_DE_API_LIMIT_VAL);
                String CHANNEL_ID = uriParts[0];

                builtUri = Uri.parse(String.format(CHANNEL_URL, CHANNEL_ID)).buildUpon()
                        .appendQueryParameter(LIMIT, LIMIT_VAL)
                        .build();
                break;
            case FetchDataTask.TASK_SINGLE_SHOW:
                String SHOW_URL = context.getString(R.string.PODCAST_DE_API_SHOW_URL);
                String SHOW_ID = uriParts[0];

                builtUri = Uri.parse(String.format(SHOW_URL, SHOW_ID)).buildUpon().build();
                break;
            default:
                break;
        }

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "buildURL: " + url);

        return url;
    }

    public static StringBuilder getStringResponseFromHttpUrl(URL url) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            connection.disconnect();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    /**
     * Return stream result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return Stream Response.
     * @throws IOException On IO Exceptions
     */
    private static InputStream getStreamResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = null;
        try {
            inputStream = (InputStream) urlConnection.getContent();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            System.out.println("Error: " + e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
        return inputStream;
    }

    public static Bitmap loadImgFrom(URL url, Context context) {
        Bitmap bitmap = null;
        try {
            bitmap = Picasso.with(context).load(url.toString()).get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImageFromBytes(byte[] image) {
        if (image == null || image.length == 0) return null;
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
