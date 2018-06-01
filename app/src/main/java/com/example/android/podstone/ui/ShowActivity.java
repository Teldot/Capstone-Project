package com.example.android.podstone.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.playerservicelib.data.MediaItem;
import com.example.android.playerservicelib.service.MediaPlaybackService;
import com.example.android.playerservicelib.ui.PlaybackViewFragment;
import com.example.android.podstone.R;
import com.example.android.podstone.data.provider.ShowContentProvider;
import com.example.android.podstone.data.provider.ShowContract;
import com.example.android.podstone.ui.widget.PlayerWidgetService;
import com.example.android.podstone.utils.NetworkUtils;
import com.google.android.exoplayer2.util.Util;

public class ShowActivity extends AppCompatActivity implements PlaybackViewFragment.OnFragmentInteractionListener {

    //private static final String K_SHOW_DATA = "K_SHOW_DATA";
    private static final String K_SHOW = "K_SHOW";
    private static final String K_FORCE_NEW_PLAYING = "K_FORCE_NEW_PLAYING";
    private static final String K_PLAYER_POSITION = "K_PLAYER_POSITION";
    private static final String K_PLAY_WHEN_READY = "K_PLAY_WHEN_READY";
    private static final int NOTIFICATION_ID = 906;
    private static final String TAG = ShowActivity.class.getName();

    private MediaItem mShow;
    private long mShowId;

    private TextView tvChannel;
    private TextView tvShowName;
    private TextView tvAuthor;
    private TextView tvCopyright;
    private TextView tvDescription;
    private ImageButton ibFavIcon;
    private PlaybackViewFragment playbackViewFragment;
    private MediaPlaybackService mService;

    private NotificationManager mNotificationManager;
    //    private PlaybackStateCompat.Builder mStateBuilder;
    private long playerPosition = 0;
    private boolean playWReady;
    private boolean forceNewPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        tvChannel = findViewById(R.id.tv_show_channel);
        tvAuthor = findViewById(R.id.tv_show_author);
        tvShowName = findViewById(R.id.tv_show_name);
        tvCopyright = findViewById(R.id.tv_show_copyright);
        tvDescription = findViewById(R.id.tv_show_description);
        //showPlayerView = findViewById(R.id.show_player);
        ibFavIcon = findViewById(R.id.favorite_icon);


        if (savedInstanceState != null) {
            mShow = (MediaItem) savedInstanceState.getSerializable(K_SHOW);
            playWReady = savedInstanceState.getBoolean(K_PLAY_WHEN_READY);
            playerPosition = savedInstanceState.getLong(K_PLAYER_POSITION);
            forceNewPlaying = savedInstanceState.getBoolean(K_FORCE_NEW_PLAYING);

            playbackViewFragment = (PlaybackViewFragment) getSupportFragmentManager().findFragmentByTag(PlaybackViewFragment.class.getName());
        } else {
            //mShowId = getIntent().getLongExtra(K_SHOW_ID, 0);
            mShow = (MediaItem) getIntent().getSerializableExtra(K_SHOW);
            forceNewPlaying = getIntent().getBooleanExtra(K_FORCE_NEW_PLAYING, false);
            //loadShowData();
            ViewGroup playbackControlContainer = findViewById(R.id.player_frame);
            playbackViewFragment = new PlaybackViewFragment();
            Bundle bundle = new Bundle();
            playbackViewFragment.setArguments(bundle);
            playbackViewFragment.setShowAlways(true);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(playbackControlContainer.getId(), playbackViewFragment, playbackViewFragment.getClass().getName())
                    .commit();
        }
    }

    private void loadData() {
        if (mShow == null) return;
        tvChannel.setText(mShow.Channel);
        tvAuthor.setText(mShow.Author);
        tvShowName.setText(mShow.Title);
        tvCopyright.setText(mShow.Copyright);
        tvDescription.setText(mShow.Description);
        if (mShow.Image != null && mShow.Image.length > 0) {
            Bitmap img = NetworkUtils.getImageFromBytes(mShow.Image);
            //showPlayerView.setDefaultArtwork(img);
        }
        if (mShow.IsInDb)
            ibFavIcon.setImageResource(R.drawable.ic_star_black_24dp);
        else
            ibFavIcon.setImageResource(R.drawable.ic_star_border_black_24dp);
        Uri showUri = null;
        if (mShow.MediaUri != null && mShow.MediaUri.length() > 0) {
            showUri = Uri.parse(mShow.MediaUri);
        }
        if (forceNewPlaying)
            playbackViewFragment.playMediaItem(mShow);
        PlayerWidgetService.startActionWidgetPlaying(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        if (mExoPlayer != null)
//            playerPosition = mExoPlayer.getCurrentPosition();
        outState.putLong(K_PLAYER_POSITION, playerPosition);
        outState.putBoolean(K_PLAY_WHEN_READY, playWReady);
        outState.putSerializable(K_SHOW, mShow);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
//            loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23)) {
//            loadData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mMediaSession != null)
//            mMediaSession.setActive(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
//            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
//            releasePlayer();
        }
    }

//    private void initializeMediaSession() {
//        mMediaSession = new MediaSessionCompat(this, TAG);
//        mMediaSession.setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//        mMediaSession.setMediaButtonReceiver(null);
//        mStateBuilder = new PlaybackStateCompat.Builder()
//                .setActions(
//                        PlaybackStateCompat.ACTION_PLAY |
//                                PlaybackStateCompat.ACTION_PAUSE |
//                                PlaybackStateCompat.ACTION_REWIND |
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
//
//        mMediaSession.setPlaybackState(mStateBuilder.build());
//        mMediaSession.setCallback(new SessionCallback());
//        mMediaSession.setActive(true);
//    }

//    private void initializePlayer(Uri mediaUri) {
//        if (mediaUri != null) {
//            if (mExoPlayer == null) {
//                // Create an instance of the ExoPlayer.
//                TrackSelector trackSelector = new DefaultTrackSelector();
//                LoadControl loadControl = new DefaultLoadControl();
//                mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
//                showPlayerView.setPlayer(mExoPlayer);
//
//                // Set the ExoPlayer.EventListener to this activity.
//                mExoPlayer.addListener(new ExoPlayer.EventListener() {
//                    @Override
//                    public void onTimelineChanged(Timeline timeline, Object manifest) {
//
//                    }
//
//                    @Override
//                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//
//                    }
//
//                    @Override
//                    public void onLoadingChanged(boolean isLoading) {
//
//                    }
//
//                    @Override
//                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
//                            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
//                                    mExoPlayer.getCurrentPosition(), 1f);
//                        } else if ((playbackState == ExoPlayer.STATE_READY)) {
//                            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
//                                    mExoPlayer.getCurrentPosition(), 1f);
//                        }
//                        playWReady = playWhenReady;
//                        mMediaSession.setPlaybackState(mStateBuilder.build());
//                        showNotification(mStateBuilder.build());
//                    }
//
//                    @Override
//                    public void onPlayerError(ExoPlaybackException error) {
//
//                    }
//
//                    @Override
//                    public void onPositionDiscontinuity() {
//
//                    }
//                });
//
//                // Prepare the MediaSource.
//                String userAgent = Util.getUserAgent(this, getApplicationInfo().name);
//                MediaSource mediaSource = new ExtractorMediaSource(
//                        mediaUri,
//                        new DefaultDataSourceFactory(this, userAgent),
//                        new DefaultExtractorsFactory(),
//                        null,
//                        null);
//                mExoPlayer.prepare(mediaSource);
//                mExoPlayer.seekTo(playerPosition);
//                mExoPlayer.setPlayWhenReady(playWReady);
//            }
//        }
//    }

//    private void releasePlayer() {
//        if (mNotificationManager != null) mNotificationManager.cancelAll();
//        if (mExoPlayer != null) {
//            mExoPlayer.stop();
//            mExoPlayer.release();
//            mExoPlayer = null;
//        }
//    }

    private void showNotification(PlaybackStateCompat state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        int icon;
        String play_pause;
        long action;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = getString(R.string.exo_controls_pause_description);
            action = PlaybackStateCompat.ACTION_PAUSE;
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = getString(R.string.exo_controls_play_description);
            action = PlaybackStateCompat.ACTION_PLAY;
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, action));

        NotificationCompat.Action rewindAction = new NotificationCompat.Action(
                R.drawable.exo_controls_rewind, getString(R.string.exo_controls_rewind_description),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (this, PlaybackStateCompat.ACTION_REWIND));

        NotificationCompat.Action fastforwardAction = new NotificationCompat.Action(
                R.drawable.exo_controls_fastforward, getString(R.string.exo_controls_fastforward_description),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (this, PlaybackStateCompat.ACTION_FAST_FORWARD));


        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, 0, new Intent(this, ShowActivity.class), 0);

        builder.setContentTitle(mShow.Title)
                .setContentText(mShow.Channel)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_podcast_not)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(rewindAction)
                .addAction(playPauseAction)
                .addAction(fastforwardAction);
//                .setStyle(new MediaStyle()
//                        .setMediaSession(mMediaSession.getSessionToken())
//                        .setShowActionsInCompactView(0, 1));


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

//    @Override
//    public void onTaskComplete(Object result, int task) {
//        mShow = (Show) result;
//        loadData();
//    }
//
//    @Override
//    public void onPreExecute() {
//
//    }

    public void onFabButClick(View view) {
        ImageButton b = (ImageButton) view;
        if (mShow.IsInDb) {
            if (deleteShow(mShow.ShowId)) {
                b.setImageResource(R.drawable.ic_star_border_black_24dp);
                mShow.IsInDb = false;
            }
        } else {
            deleteShow(mShow.ShowId);
            if (saveFavorite()) {
                b.setImageResource(R.drawable.ic_star_black_24dp);
                mShow.IsInDb = true;
            }
        }
    }

    private boolean deleteShow(long showId) {
        Uri uri = ShowContentProvider.getUri(ShowContentProvider.SHOW_ID, String.valueOf(showId));
        int res = getContentResolver().delete(uri, null, null);
        if (res > 0) {
            Toast.makeText(this, getString(R.string.favorite_show_removed), Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, getString(R.string.favorite_show_remove_error), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean saveFavorite() {
        Uri uri = ShowContentProvider.getUri(ShowContentProvider.SHOWS, String.valueOf(ShowContentProvider.NO_SHOW_ID));
        //Uri uri = ShowContract.ShowEntry.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(ShowContract.ShowEntry._ID, mShow.ShowId);
        values.put(ShowContract.ShowEntry.COLUMN_TITLE, mShow.Title);
        values.put(ShowContract.ShowEntry.COLUMN_AUTHOR, mShow.Author);
        values.put(ShowContract.ShowEntry.COLUMN_CHANNEL, mShow.Channel);
        values.put(ShowContract.ShowEntry.COLUMN_COPYRIGHT, mShow.Copyright);
        values.put(ShowContract.ShowEntry.COLUMN_DATE, mShow.ShowDate);
        values.put(ShowContract.ShowEntry.COLUMN_DESCRIPTION, mShow.Description);
        values.put(ShowContract.ShowEntry.COLUMN_IMAGE, mShow.Image);
        values.put(ShowContract.ShowEntry.COLUMN_MEDIA_LINK, mShow.MediaUri);
        values.put(ShowContract.ShowEntry.COLUMN_PLAYER_POS, 0);
        values.put(ShowContract.ShowEntry.COLUMN_RATING, mShow.Rating);
        values.put(ShowContract.ShowEntry.COLUMN_SIZE, mShow.Size);
        values.put(ShowContract.ShowEntry.COLUMN_VOTES, mShow.Votes);

        Uri res = getContentResolver().insert(uri, values);
        if (res != null) {
            Toast.makeText(this, String.format(getString(R.string.favorite_show_saved), mShow.Title), Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, getString(R.string.favorite_show_save_error), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onBindService(MediaPlaybackService service) {
        mService = service;
        loadData();
    }

//    private class SessionCallback extends MediaSessionCompat.Callback {
//        @Override
//        public void onPlay() {
//            mExoPlayer.setPlayWhenReady(true);
//        }
//
//        @Override
//        public void onPause() {
//            mExoPlayer.setPlayWhenReady(false);
//        }
//
//        @Override
//        public void onFastForward() {
//            long duration = mExoPlayer.getDuration();
//            long seekTime = getResources().getInteger(R.integer.seek_time_in_secs);
//            long currentPos = mExoPlayer.getCurrentPosition();
//            if (duration >= currentPos + seekTime)
//                mExoPlayer.seekTo(currentPos + seekTime);
//            else
//                mExoPlayer.seekTo(duration);
//        }
//
//        @Override
//        public void onRewind() {
//            long seekTime = getResources().getInteger(R.integer.seek_time_in_secs);
//            long currentPos = mExoPlayer.getCurrentPosition();
//            if (0 <= currentPos - seekTime)
//                mExoPlayer.seekTo(currentPos - seekTime);
//            else
//                mExoPlayer.seekTo(0);
//        }
//    }
//
//    public static class MediaReceiver extends BroadcastReceiver {
//        public MediaReceiver() {
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            MediaButtonReceiver.handleIntent(mMediaSession, intent);
//        }
//    }
}
