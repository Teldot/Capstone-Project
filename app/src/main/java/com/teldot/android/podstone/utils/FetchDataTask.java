package com.teldot.android.podstone.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teldot.android.playerservicelib.data.MediaItem;
import com.teldot.android.podstone.data.entities.Channel;
import com.teldot.android.podstone.data.entities.SearchResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class FetchDataTask extends AsyncTask {
    private final Context mContext;
    private final AsyncTaskCompleteListener<Object> listener;
    private int Task;

    private final String TAG = this.getClass().getName();
    public static final int TASK_STARTUP_LIST = 635;
    public static final int TASK_SEARCH_LIST = 222;
    public static final int TASK_SINGLE_CHANNEL = 347;
    public static final int TASK_SINGLE_SHOW = 91;
    public static final int TASK_CHECK_URL = 207;

    private static final String SHOW_OBJ = "show";
    private static final String SHOW_AUTHOR = "author";
    private static final String SHOW_SHOW_ID = "show_id";
    private static final String SHOW_TITLE = "title";
    private static final String SHOW_DESCRIPTION = "description";
    private static final String SHOW_COPYRIGHT = "copyright";
    private static final String SHOW_CHANNEL = "channel_title";
    private static final String SHOW_DATE = "date";
    private static final String SHOW_MEDIA_LINK = "media_link";
    private static final String SHOW_RATING = "rating";
    private static final String SHOW_VOTES = "votes";
    private static final String SHOW_LENGTH = "length";
    private static final String SHOW_SIZE = "size";
    private static final String SHOW_IMAGE = "image";

    private static final String CHAN_OBJ = "channel";
    private static final String CHAN_CHANNEL_ID = "channel_id";
    private static final String CHAN_TITLE = "title";
    private static final String CHAN_DESCRIPTION = "description";
    private static final String CHAN_IMAGE_URL = "image";
    private static final String CHAN_RATING = "rating";
    private static final String CHAN_VOTES = "votes";
    private static final String CHAN_COPYRIGHT = "copyright";
    private static final String CHAN_DATE = "date";
    private static final String CHAN_EPISODES = "episodes";

    private static final String SRCH_HEAD = "head";
    private static final String SRCH_HEAD_LIMIT = "limit";
    private static final String SRCH_HEAD_OFFSET = "offset";
    private static final String SRCH_HEAD_COUNT = "count";
    private static final String SRCH_CHANNELS = "channels";


    public FetchDataTask(Context _context, AsyncTaskCompleteListener<Object> _listener, int task) {
        mContext = _context;
        listener = _listener;
        Task = task;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String res;
        URL url;
        switch (Task) {
            case TASK_STARTUP_LIST:
                url = NetworkUtils.buildURL(mContext, Task, null);
                res = NetworkUtils.getStringResponseFromHttpUrl(url).toString();
                return getSearchFromJson(res);
            case TASK_SEARCH_LIST:
                String qString = objects[0].toString();
                String offSet = objects[1].toString();
                url = NetworkUtils.buildURL(mContext, Task, new String[]{qString, offSet});
                res = NetworkUtils.getStringResponseFromHttpUrl(url).toString();
                return getSearchFromJson(res);
            case TASK_SINGLE_CHANNEL:
                String qChannel = objects[0].toString();
                url = NetworkUtils.buildURL(mContext, Task, new String[]{qChannel});
                res = NetworkUtils.getStringResponseFromHttpUrl(url).toString();
                return getChannelInfoFromJson(res);
            case TASK_SINGLE_SHOW:
                String qShow = objects[0].toString();
                url = NetworkUtils.buildURL(mContext, Task, new String[]{qShow});
                res = NetworkUtils.getStringResponseFromHttpUrl(url).toString();
                return getShowInfoFromJson(res);
            case TASK_CHECK_URL:
                String qShowUrl = objects[0].toString();
                String showIndex = objects[1].toString();
                return showIndex + "-" + NetworkUtils.checkurl(qShowUrl);
            default:
                return null;
        }
    }

    private Channel getChannelInfoFromJson(String res) {
        if (res == null || res.length() == 0) return null;

        Channel channel;
        try {
            JSONObject obj = new JSONObject(res);
            JSONObject chanObject = obj.getJSONObject(CHAN_OBJ);
            channel = getChannelInfoFromJson(chanObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return channel;
    }

    private Channel getChannelInfoFromJson(JSONObject chanObject) {
        if (chanObject == null) return null;

        Channel channel = new Channel();
        try {
            channel.ChannelId = chanObject.getLong(CHAN_CHANNEL_ID);
            channel.Description = chanObject.getString(CHAN_DESCRIPTION);
            channel.ImageUrl = chanObject.getString(CHAN_IMAGE_URL);
            if (channel.ImageUrl != null && channel.ImageUrl.length() > 0) {
                URL url = new URL(channel.ImageUrl);
                channel.Image = NetworkUtils.getBytesFromBitmap(NetworkUtils.loadImgFrom(url, mContext));
            }

            channel.Rating = chanObject.getDouble(CHAN_RATING);
            channel.Title = chanObject.getString(CHAN_TITLE);
            channel.Vote = chanObject.getLong(CHAN_VOTES);
            if (chanObject.has(CHAN_COPYRIGHT))
                channel.Copyright = chanObject.getString(CHAN_COPYRIGHT);
            if (chanObject.has(CHAN_DATE))
                channel.ChanDate = getFormatDate(chanObject.getString(CHAN_DATE));
            if (chanObject.has(CHAN_EPISODES)) {
                JSONArray episArray = chanObject.getJSONArray(CHAN_EPISODES);
                if (episArray != null && episArray.length() > 0) {
                    channel.Shows = new MediaItem[episArray.length()];
                    for (int i = 0; i < episArray.length(); i++) {
                        channel.Shows[i] = getShowInfoFromJson(episArray.getJSONObject(i));
                        channel.Shows[i].Channel = chanObject.getString(CHAN_TITLE);
                        if (chanObject.has(CHAN_COPYRIGHT))
                            channel.Shows[i].Copyright = chanObject.getString(CHAN_COPYRIGHT);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return channel;
    }

    private MediaItem getShowInfoFromJson(String res) {
        if (res == null || res.length() == 0) return null;

        MediaItem show;
        try {
            JSONObject obj = new JSONObject(res);
            JSONObject showObject = obj.getJSONObject(SHOW_OBJ);
            show = getShowInfoFromJson(showObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return show;
    }

    private MediaItem getShowInfoFromJson(JSONObject showObject) {
        if (showObject == null) return null;

        MediaItem show = new MediaItem();
        try {
            String author = showObject.getString(SHOW_AUTHOR);
            show.Author = (author == null || author.equals("null")) ? "" : author;
            if (showObject.has(SHOW_COPYRIGHT)) {
                String copyRight = showObject.getString(SHOW_COPYRIGHT);
                show.Copyright = (copyRight == null || copyRight.equals("null")) ? "" : copyRight;
            }
            show.Description = showObject.getString(SHOW_DESCRIPTION);
            if (showObject.has(SHOW_IMAGE)) {
                Log.i(TAG, "Has Image");
                show.ImgUri = showObject.getString(SHOW_IMAGE);
                if (show.ImgUri != null && show.ImgUri.length() > 0) {
                    URL url = new URL(show.ImgUri);
                    show.Image = NetworkUtils.getBytesFromBitmap(NetworkUtils.loadImgFrom(url, mContext));
                }
            }
            show.MediaUri = showObject.getString(SHOW_MEDIA_LINK);
            show.Rating = showObject.getDouble(SHOW_RATING);
            show.ShowDate = getFormatDate(showObject.getString(SHOW_DATE));
            show.ShowId = showObject.getLong(SHOW_SHOW_ID);
            if (showObject.has(SHOW_LENGTH))
                show.Size = showObject.getDouble(SHOW_LENGTH);
            if (showObject.has(SHOW_SIZE))
                show.Size = showObject.getDouble(SHOW_SIZE);
            show.Title = showObject.getString(SHOW_TITLE);
            if (showObject.has(SHOW_CHANNEL))
                show.Channel = showObject.getString(SHOW_CHANNEL);
            show.Votes = showObject.getDouble(SHOW_VOTES);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return show;
    }

    private SearchResult getSearchFromJson(String json) {
        if (json == null || json.length() == 0) return null;

        SearchResult searchResult = new SearchResult();
        try {
            JSONObject obj = new JSONObject(json);
            searchResult.Count = obj.getJSONArray(SRCH_HEAD).getJSONObject(0).getInt(SRCH_HEAD_COUNT);
            searchResult.Limit = obj.getJSONArray(SRCH_HEAD).getJSONObject(0).getInt(SRCH_HEAD_LIMIT);
            searchResult.Offset = obj.getJSONArray(SRCH_HEAD).getJSONObject(0).getInt(SRCH_HEAD_OFFSET);
            JSONArray channels = obj.getJSONArray(SRCH_CHANNELS);
            if (channels != null && channels.length() > 0) {
                searchResult.Channels = new Channel[channels.length()];
                for (int i = 0; i < channels.length(); i++) {
                    searchResult.Channels[i] = getChannelInfoFromJson(channels.getJSONObject(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return searchResult;
    }

    private String getFormatDate(String oDate) {
        if (oDate == null || oDate.length() == 0) return "";

        String[] dateParts = oDate.split(" ");
        if (dateParts.length == 0) return "";
        return dateParts[0];
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        listener.onTaskComplete(o, Task);
    }

    public interface AsyncTaskCompleteListener<Object> {
        void onTaskComplete(java.lang.Object result, int task);

        void onPreExecute();
    }
}
