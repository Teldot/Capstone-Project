package com.example.android.playerservicelib.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.playerservicelib.data.MediaItem;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MediaPlaybackService extends Service implements ExoPlayer.EventListener {
    private static final String TAG = MediaPlaybackService.class.getSimpleName();
    public static final int INITIAL_INDEX = -1;

    private MediaItem[] mediaItems;
    private int mediaItemIndex = INITIAL_INDEX;
    private MediaPlaybackServiceEventHandler mediaPlaybackServiceEventHandler;
    private SimpleExoPlayer mExoPlayer;
    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder = new MediaPlaybackBinder();      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used


    private String userAgent;

    public SimpleExoPlayer getmExoPlayer() {
        return mExoPlayer;
    }

    public boolean isNowPlaying() {
        if (mExoPlayer == null) return false;
        return mExoPlayer.getPlayWhenReady();
    }

    @Override
    public void onCreate() {
        // The service is being created
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initPlayer();
        // The service is starting, due to a call to startService()
        return mStartMode;
    }

    private boolean initPlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);
        }
        return true;

    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    //***************************************************
    // LIST METHODS
    //***************************************************
    public boolean addMediaToPlayList(MediaItem mediaItem) {
        try {
            if (mediaItems == null) mediaItems = new MediaItem[0];
            ArrayList<MediaItem> arrayList = new ArrayList<MediaItem>();
            arrayList.addAll(Arrays.asList(mediaItems));

            if (!arrayList.contains(mediaItem)) {
                arrayList.add(mediaItem);
                mediaItems = arrayList.toArray(mediaItems);
                mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeMediaToPlayList(MediaItem mediaItem) {
        if (mediaItems == null) {
            mediaItems = new MediaItem[0];
            mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
            return false;
        } else {
            List<MediaItem> arrayList = Arrays.asList(mediaItems);
            arrayList.remove(mediaItem);

            mediaItems = arrayList.toArray(mediaItems);
            mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
            return true;
        }
    }

    public MediaItem[] getMediaItems() {
        return mediaItems;
    }

    public void clearMediaPlayList() {
        mediaItems = new MediaItem[0];
        mediaItemIndex = INITIAL_INDEX;
        mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
    }
    //***************************************************

    //***************************************************
    //    SERVICE EVENTS
    //***************************************************

    @Override
    public IBinder onBind(Intent intent) {
        initPlayer();
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        releasePlayer();
    }
    //***************************************************
    //***************************************************


    //***************************************************
    //    EXOPLAYER EVENTS
    //***************************************************
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        if (mediaPlaybackServiceEventHandler != null)
            mediaPlaybackServiceEventHandler.onTimelineChanged(timeline, manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (mediaPlaybackServiceEventHandler != null)
            mediaPlaybackServiceEventHandler.onTracksChanged(trackGroups, trackSelections);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (mediaPlaybackServiceEventHandler != null)
            mediaPlaybackServiceEventHandler.onLoadingChanged(isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (mediaPlaybackServiceEventHandler != null)
            mediaPlaybackServiceEventHandler.onPlayerStateChanged(playWhenReady, playbackState);

        //When finish playing media look for next media in the array to play it
        if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
            playNextMediaItem();
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (mediaPlaybackServiceEventHandler != null)
            mediaPlaybackServiceEventHandler.onPlayerError(error);
    }

    @Override
    public void onPositionDiscontinuity() {
        if (mediaPlaybackServiceEventHandler != null)
            mediaPlaybackServiceEventHandler.onPositionDiscontinuity();
    }

    public int getCurrentMediaItemIndex() {
        return mediaItemIndex;
    }

    public void playPreviousMediaItem() {
        if (mediaItems == null || mediaItems.length == 0) return;
        if (mediaItemIndex == 0) {
            mExoPlayer.seekTo(0l);
        } else {
            mediaItemIndex--;
            playMediaItem();
        }
    }

    public void playMediaItem() {
        pauseMediaPlaying();
        if (mediaItemIndex == INITIAL_INDEX && mediaItems != null && mediaItems.length > 0)
            mediaItemIndex++;

        Log.i(TAG, mediaItems[mediaItemIndex].MediaUri);
        Uri uri = Uri.parse(mediaItems[mediaItemIndex].MediaUri).buildUpon().build();
        userAgent = Util.getUserAgent(this, this.getApplicationInfo().name);
        MediaSource mediaSource = new ExtractorMediaSource(uri, new DefaultDataSourceFactory(
                this, userAgent), new DefaultExtractorsFactory(), null, null);
        mExoPlayer.prepare(mediaSource);
        mExoPlayer.seekTo(0);
        mExoPlayer.setPlayWhenReady(true);

        mediaPlaybackServiceEventHandler.onTrackChanged(mediaItemIndex, mediaItems[mediaItemIndex]);
    }

    public void playMediaItem(MediaItem mediaItem) {
        clearMediaPlayList();
        addMediaToPlayList(mediaItem);
        playMediaItem();
    }

    public void pauseMediaPlaying() {
        if (mediaItems == null || mediaItems.length == 0) return;

        if (mExoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    public void resumeMediaPlaying() {
        if (mediaItems == null || mediaItems.length == 0) return;

        if (mExoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    public void playNextMediaItem() {
        if (mediaItems == null || mediaItems.length == 0) return;

        if (mediaItemIndex >= mediaItems.length - 1) {
            mediaItemIndex = INITIAL_INDEX;
            return;
        }
        mediaItemIndex++;
        playMediaItem();
    }

    //***************************************************
    //***************************************************

    public class MediaPlaybackBinder extends Binder {
        public MediaPlaybackService getService(@NonNull MediaPlaybackServiceEventHandler eventHandler) {
            // Return this instance of LocalService so clients can call public methods
            mediaPlaybackServiceEventHandler = eventHandler;
            return MediaPlaybackService.this;
        }
    }

    public interface MediaPlaybackServiceEventHandler {
        public void onPlayListItemsChanged(int mediaItemsCount, int position);

        public void onTrackChanged(int position, MediaItem mediaItem);

        public void onTimelineChanged(Timeline timeline, Object manifest);

        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections);

        public void onLoadingChanged(boolean isLoading);

        public void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        public void onPlayerError(ExoPlaybackException error);

        public void onPositionDiscontinuity();
    }

}
