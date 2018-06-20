package com.example.android.podstone.ui.widget;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.android.playerservicelib.data.MediaItem;
import com.example.android.playerservicelib.service.MediaPlaybackService;
import com.example.android.podstone.R;
import com.example.android.podstone.ui.FavoritesActivity;
import com.example.android.podstone.ui.MainActivity;
import com.example.android.podstone.ui.ShowActivity;
import com.example.android.podstone.utils.NetworkUtils;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

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
    public static final String ACTION_NOW_PLAYING = ".ui.widget.NowPlayingWidget.ACTION_NOW_PLAYING";


    private static final String K_SHOW = "K_SHOW";
    private static MediaItem Show;
    private static final MediaPlaybackService[] mService = new MediaPlaybackService[1];

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

        intent = new Intent(context, NowPlayingWidget.class);
        intent.setAction(ACTION_NOW_PLAYING);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.ib_open_np_screen, actionPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_container, actionPendingIntent);

        initializePlayerService(context.getApplicationContext(), views, appWidgetManager, appWidgetId);

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        switch (action) {
            case ACTION_PREV:
                mService[0].playPreviousMediaItem();
                break;
            case ACTION_NEXT:
                mService[0].playNextMediaItem();
                break;
            case ACTION_PLAY:
                mService[0].resumeMediaPlaying();
                break;
            case ACTION_PAUSE:
                mService[0].pauseMediaPlaying();
                break;
            case ACTION_NOW_PLAYING:
                Intent openIntent;
                if (mService[0] != null && mService[0].getMediaItems() != null && mService[0].getMediaItems().length > 0) {
                    if (mService[0].getMediaItems().length == 1) {
                        int idx = 0;
                        openIntent = new Intent(context, ShowActivity.class);
                        MediaItem mediaItem = mService[0].getMediaItems()[idx];
                        openIntent.putExtra(K_SHOW, mediaItem);
                    } else {
                        openIntent = new Intent(context, FavoritesActivity.class);
                    }

                } else {
                    openIntent = new Intent(context, MainActivity.class);
                }
                context.startActivity(openIntent);
                break;
            default:

                break;
        }
        super.onReceive(context, intent);
    }

    private static void initializePlayerService(final Context mContext, final RemoteViews views, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        Intent intent = new Intent(mContext, MediaPlaybackService.class);
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
                        MediaItem[] mediaItems = mService[0].getMediaItems();
                        if (mediaItems != null && mediaItems.length > 0) {
                            int currentMediaItem = mService[0].getCurrentMediaItemIndex();
                            remoteViews[0] = loadData(mediaItems[currentMediaItem], remoteViews[0], mContext);
                        } else {
                            remoteViews[0] = loadData(null, remoteViews[0], mContext);
                        }
                        remoteViews[0] = setPlayPauseButtonsVisibility(remoteViews[0], mService[0]);
                        appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
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
                    if (mediaItems != null && mediaItems.length > 0) {
                        int currentMediaItem = mService[0].getCurrentMediaItemIndex();
                        remoteViews[0] = loadData(mediaItems[currentMediaItem], remoteViews[0], mContext);
                    } else {
                        remoteViews[0] = loadData(null, remoteViews[0], mContext);
                    }
                    remoteViews[0] = setPlayPauseButtonsVisibility(remoteViews[0], mService[0]);
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                remoteViews[0] = loadData(null, remoteViews[0], mContext);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
            }

            @Override
            public void onBindingDied(ComponentName name) {
                remoteViews[0] = loadData(null, remoteViews[0], mContext);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
            }
        };
        if (isMyServiceRunning(MediaPlaybackService.class, mContext)) {
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            remoteViews[0] = loadData(null, remoteViews[0], mContext);
            remoteViews[0] = setPlayPauseButtonsVisibility(remoteViews[0], null);
        }
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews[0]);
    }

    private static RemoteViews loadData(MediaItem mediaItem, RemoteViews views, Context mContext) {
        if (mediaItem == null) {
            views.setTextViewText(R.id.widget_tv_podcast_title, mContext.getText(R.string.app_name));
            views.setTextViewText(R.id.widget_tv_podcast_channel, "");
            views.setImageViewResource(R.id.widget_iv_podcast_image, R.drawable.ic_podcast);
            views.setTextViewText(R.id.tv_song_number_on_list, "");
            return views;
        }
        views.setTextViewText(R.id.widget_tv_podcast_title, mediaItem.Title);
        views.setTextViewText(R.id.widget_tv_podcast_channel, mediaItem.Channel);
        if (mService != null) {
            int mediaIndx = mService[0].getCurrentMediaItemIndex() + 1;
            int totalMedia = mService[0].getMediaItems().length;
            views.setTextViewText(R.id.tv_song_number_on_list,
                    String.format(mContext.getString(R.string.template_song_number_on_list), mediaIndx, totalMedia));
        }
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

    private static RemoteViews setPlayPauseButtonsVisibility(RemoteViews views, MediaPlaybackService service) {
        boolean playAction = true;
        if (service != null) {
            int playbackState = service.getmExoPlayer().getPlaybackState();
            boolean playWhenReady = service.getmExoPlayer().getPlayWhenReady();
            playAction = !(playWhenReady && playbackState == ExoPlayer.STATE_READY);
        }
        views.setViewVisibility(R.id.exo_play_button, playAction ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.exo_pause_button, playAction ? View.GONE : View.VISIBLE);
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
        updatePlayingWidgets(context, appWidgetManager, appWidgetIds);
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

