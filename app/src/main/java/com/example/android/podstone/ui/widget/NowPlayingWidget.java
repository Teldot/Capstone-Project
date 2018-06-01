package com.example.android.podstone.ui.widget;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.android.playerservicelib.data.MediaItem;
import com.example.android.playerservicelib.service.MediaPlaybackService;
import com.example.android.playerservicelib.ui.PlaybackViewFragment;
import com.example.android.podstone.R;
import com.example.android.podstone.ui.ShowActivity;
import com.example.android.podstone.utils.NetworkUtils;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of App Widget functionality.
 */
public class NowPlayingWidget extends AppWidgetProvider {
    public static final String ACTION_PREV = ".ui.widget.NowPlayingWidget.ACTION_PREV";
    public static final String ACTION_NEXT = ".ui.widget.NowPlayingWidget.ACTION_NEXT";
    public static final String ACTION_PLAY = ".ui.widget.NowPlayingWidget.ACTION_PLAY";
    public static final String ACTION_PAUSE = ".ui.widget.NowPlayingWidget.ACTION_PAUSE";
    //public static final String APPWIDGET_UPDATE = "APPWIDGET_UPDATE";


    private static final String K_SHOW = "K_SHOW";
    private static MediaItem Show;

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.now_playing_widget);

        Intent intent = new Intent(context, NowPlayingWidget.class);
        intent.setAction(ACTION_PREV);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.exo_prev_button, actionPendingIntent);

        intent = new Intent(context, NowPlayingWidget.class);
        intent.setAction(ACTION_NEXT);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.exo_next_button, actionPendingIntent);

        intent = new Intent(context, NowPlayingWidget.class);
        intent.setAction(ACTION_PAUSE);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.exo_pause_button, actionPendingIntent);

        intent = new Intent(context, NowPlayingWidget.class);
        intent.setAction(ACTION_PLAY);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.exo_play_button, actionPendingIntent);

//        intent = new Intent(context, NowPlayingWidget.class);
//        intent.setAction(APPWIDGET_UPDATE);
//        actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        views.setOnClickPendingIntent(R.id.widget_container, actionPendingIntent);

        initializePlayerService(context.getApplicationContext(), views, appWidgetManager, appWidgetId);

        // Instruct the widget manager to update the widget
        //appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        switch (action) {
            case ACTION_PREV:
                Toast.makeText(context, action, Toast.LENGTH_LONG).show();
                break;
            case ACTION_NEXT:
                Toast.makeText(context, action, Toast.LENGTH_LONG).show();

                break;
            case ACTION_PLAY:
                Toast.makeText(context, action, Toast.LENGTH_LONG).show();
                break;
            case ACTION_PAUSE:
                Toast.makeText(context, action, Toast.LENGTH_LONG).show();
                break;
            default:
                super.onReceive(context, intent);
                Toast.makeText(context, "No valid action", Toast.LENGTH_LONG).show();

                break;
        }
    }

    private static void initializePlayerService(final Context mContext, RemoteViews views, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        Intent intent = new Intent(mContext, MediaPlaybackService.class);
        final MediaPlaybackService[] mService = new MediaPlaybackService[1];
        final RemoteViews[] remoteViews = new RemoteViews[1];
        remoteViews[0] = views;
        ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
                mService[0] = binder.getService(new MediaPlaybackService.MediaPlaybackServiceEventHandler() {
                    @Override
                    public void onPlayListItemsChanged(int mediaItemsCount, int position) {

                    }

                    @Override
                    public void onTrackChanged(int position, MediaItem mediaItem) {

                    }

                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest) {

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {

                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {

                    }

                    @Override
                    public void onPositionDiscontinuity() {

                    }
                });

                if (mService[0] != null) {
                    MediaItem[] mediaItems = mService[0].getMediaItems();
                    if (mediaItems.length > 0) {
                        int currentMediaItem = mService[0].getCurrentMediaItemIndex();
                        remoteViews[0] = loadData(mediaItems[currentMediaItem], remoteViews[0], mContext);
                    } else {
                        remoteViews[0] = loadData(null, remoteViews[0], mContext);
                    }
//                mBound = true;
                    //mListener.onBindService(mService[0]);
//                setShowAlways(mShowAlways);
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                remoteViews[0] = loadData(null, remoteViews[0], mContext);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
            }
        };
        if (isMyServiceRunning(MediaPlaybackService.class, mContext)) {
            //mContext.startService(intent);
//            mContext.bindService(intent, mConnection, Context.BIND_WAIVE_PRIORITY);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else
            remoteViews[0] = loadData(null, remoteViews[0], mContext);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
    }

    private static RemoteViews loadData(MediaItem mediaItem, RemoteViews views, Context mContext) {
        if (mediaItem == null) {
            views.setTextViewText(R.id.widget_tv_podcast_title, mContext.getText(R.string.app_name));
            views.setTextViewText(R.id.widget_tv_podcast_channel, "");
            views.setImageViewResource(R.id.widget_iv_podcast_image, R.drawable.ic_podcast);
            return views;
        }
        views.setTextViewText(R.id.widget_tv_podcast_title, mediaItem.Title);
        views.setTextViewText(R.id.widget_tv_podcast_channel, mediaItem.Channel);
        if (mediaItem.ImgUri != null) {
            try {
                URL url = new URL(mediaItem.ImgUri);
                Bitmap bitmap = NetworkUtils.loadImgFrom(url, mContext);
                views.setImageViewBitmap(R.id.widget_iv_podcast_image, bitmap);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else views.setImageViewResource(R.id.widget_iv_podcast_image, R.drawable.ic_podcast);

        return views;
    }


    private static boolean isMyServiceRunning(Class<?> serviceClass, Context mContext) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void updatePlayingWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

