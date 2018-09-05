package com.teldot.android.playerservicelib.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.teldot.android.playerservicelib.data.MediaItem;
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


public class MediaPlaybackService extends Service implements Player.EventListener {
    private static final String TAG = MediaPlaybackService.class.getSimpleName();
    public static final int INITIAL_INDEX = -1;

    private MediaItem[] mediaItems;
    private int mediaItemIndex = INITIAL_INDEX;
    private ArrayList<MediaPlaybackServiceEventHandler> mediaPlaybackServiceEventHandlers;
    private SimpleExoPlayer mExoPlayer;
    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder = new MediaPlaybackBinder();      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used


    public SimpleExoPlayer getmExoPlayer() {
        return mExoPlayer;
    }

    public boolean isNowPlaying() {
        return mExoPlayer != null && mExoPlayer.getPlayWhenReady();
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

    private void initPlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);

        }

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
            ArrayList<MediaItem> arrayList = new ArrayList<>(Arrays.asList(mediaItems));

            if (!arrayList.contains(mediaItem)) {
                arrayList.add(mediaItem);
                mediaItems = arrayList.toArray(mediaItems);
//                mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
                if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
                    for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                        ev.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
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
//            mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
            if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
                for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                    ev.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
            return false;
        } else {
            List<MediaItem> arrayList = Arrays.asList(mediaItems);
            arrayList.remove(mediaItem);

            mediaItems = arrayList.toArray(mediaItems);
//            mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
            if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
                for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                    ev.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
            return true;
        }
    }

    public MediaItem[] getMediaItems() {
        return mediaItems;
    }

    public void clearMediaPlayList() {
        mediaItems = new MediaItem[0];
        mediaItemIndex = INITIAL_INDEX;
        //mediaPlaybackServiceEventHandler.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onPlayListItemsChanged(mediaItems.length, mediaItemIndex);
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
//    @Override
//    public void onTimelineChanged(Timeline timeline, Object manifest) {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onTimelineChanged(timeline, manifest);
//        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
//            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
//                ev.onTimelineChanged(timeline, manifest);
//    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        //        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onTimelineChanged(timeline, manifest);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onTimelineChanged(timeline, manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onTracksChanged(trackGroups, trackSelections);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onTracksChanged(trackGroups, trackSelections);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onLoadingChanged(isLoading);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onLoadingChanged(isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onPlayerStateChanged(playWhenReady, playbackState);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onPlayerStateChanged(playWhenReady, playbackState);

        //When finish playing media look for next media in the array to play it
        if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
            playNextMediaItem();
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onPlayerError(error);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onPlayerError(error);
    }

//    @Override
//    public void onPositionDiscontinuity() {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onPositionDiscontinuity();
//        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
//            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
//                ev.onPositionDiscontinuity();
    //}
    @Override
    public void onPositionDiscontinuity(int reason) {
//        if (mediaPlaybackServiceEventHandler != null)
//            mediaPlaybackServiceEventHandler.onPositionDiscontinuity();
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onPositionDiscontinuity();
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public int getCurrentMediaItemIndex() {
        if (mediaItemIndex == -1 && mediaItems != null && mediaItems.length > 0)
            mediaItemIndex = 0;
        return mediaItemIndex;
    }

    public void playPreviousMediaItem() {
        if (mediaItems == null || mediaItems.length == 0) return;
        if (mediaItemIndex == 0) {
            mExoPlayer.seekTo(0L);
        } else if (mediaItemIndex > 0) {
            mediaItemIndex--;
            playMediaItem();
        } else {
            mediaItemIndex = 0;
            playMediaItem();
        }
    }

    public void playMediaItem() {
        pauseMediaPlaying();
        if (mediaItemIndex == INITIAL_INDEX && mediaItems != null && mediaItems.length > 0)
            mediaItemIndex++;

        Log.i(TAG, mediaItems[mediaItemIndex].MediaUri);
        Uri uri = Uri.parse(mediaItems[mediaItemIndex].MediaUri).buildUpon().build();
        String userAgent = Util.getUserAgent(this, this.getApplicationInfo().name);
        MediaSource mediaSource = new ExtractorMediaSource(uri, new DefaultDataSourceFactory(
                this, userAgent), new DefaultExtractorsFactory(), null, null);
        mExoPlayer.prepare(mediaSource);
        mExoPlayer.seekTo(0);
        mExoPlayer.setPlayWhenReady(true);
        if (mediaPlaybackServiceEventHandlers != null && mediaPlaybackServiceEventHandlers.size() > 0)
            for (MediaPlaybackServiceEventHandler ev : mediaPlaybackServiceEventHandlers)
                ev.onTrackChanged(mediaItemIndex, mediaItems[mediaItemIndex]);

//        mediaPlaybackServiceEventHandler.onTrackChanged();
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
            if (mediaPlaybackServiceEventHandlers == null)
                mediaPlaybackServiceEventHandlers = new ArrayList<>();
            if (!mediaPlaybackServiceEventHandlers.contains(eventHandler))
                mediaPlaybackServiceEventHandlers.add(eventHandler);
            return MediaPlaybackService.this;
        }
    }

    public interface MediaPlaybackServiceEventHandler {
        void onPlayListItemsChanged(int mediaItemsCount, int position);

        void onTrackChanged(int position, MediaItem mediaItem);

        void onTimelineChanged(Timeline timeline, Object manifest);

        void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections);

        void onLoadingChanged(boolean isLoading);

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onPlayerError(ExoPlaybackException error);

        void onPositionDiscontinuity();
    }

}
