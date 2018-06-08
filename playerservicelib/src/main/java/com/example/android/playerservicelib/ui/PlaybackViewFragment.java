package com.example.android.playerservicelib.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.playerservicelib.R;
import com.example.android.playerservicelib.data.MediaItem;
import com.example.android.playerservicelib.service.MediaPlaybackService;
import com.example.android.playerservicelib.utils.NetUtils;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class PlaybackViewFragment extends Fragment implements MediaPlaybackService.MediaPlaybackServiceEventHandler, SimpleExoPlayerView.OnClickListener {
    private static final String TAG = PlaybackViewFragment.class.getSimpleName();

    public static final String K_SHOW_TITLE = "K_SHOW_TITLE";
    public static final String K_SHOW_CHANNEL = "K_SHOW_CHANNEL";
    public static final String K_SHOW_URL = "K_SHOW_URL";
    private static final String K_PLAYER_POS = "K_PLAYER_POS";
    private static final String K_SHOW_ALWAYS = "K_SHOW_ALWAYS";
    private static final String K_IS_BOUND = "IS_BOUND";
    private static final String K_SHOW = "K_SHOW";


    private MediaItem mMediaItem;
    private int mMediaItemPos;

    private long mPlayerPos = 0;
    private Context mContext;
    private MediaPlaybackService mService;
    boolean mBound;
    private boolean mShowAlways = false;

    public SimpleExoPlayerView playerView;

    private ImageButton buttonPrevious;
    private ImageButton buttonNext;
    private TextView tvTitle;
    private TextView tvAuthor;
    private ImageView ivShowImage;
    private View generalView;
    private Intent openIntent;
    private LinearLayout mainPlayerControlsContainer;

    private OnFragmentInteractionListener mListener;

    public PlaybackViewFragment() {
        // Required empty public constructor
    }


    public static PlaybackViewFragment newInstance(Bundle args) {
        PlaybackViewFragment fragment = new PlaybackViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        generalView = inflater.inflate(R.layout.fragment_playback_view, container, false);
        playerView = generalView.findViewById(R.id.show_player);
        //
        mainPlayerControlsContainer = playerView.findViewById(R.id.main_container);
        mainPlayerControlsContainer.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility != View.VISIBLE)
                    mainPlayerControlsContainer.setVisibility(View.VISIBLE);
            }
        });
        //
        buttonNext = playerView.findViewById(R.id.exo_next_button);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackViewFragment.this.onClick(v);
            }
        });
        buttonPrevious = playerView.findViewById(R.id.exo_prev_button);
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackViewFragment.this.onClick(v);
            }
        });
        generalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackViewFragment.this.onClick(v);
            }
        });
        playerView.setControllerShowTimeoutMs(0);
        playerView.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility != PlaybackControlView.VISIBLE) {
//                        playerView.setUseController(true);
                    playerView.showController();
//                    playerView.refreshDrawableState();
                    playerView.setVisibility(View.VISIBLE);
                }
            }
        });
        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackViewFragment.this.onClick(v);
            }
        });
        tvTitle = playerView.findViewById(R.id.text_title);
        tvAuthor = playerView.findViewById(R.id.text_author);
        ivShowImage = playerView.findViewById(R.id.track_image);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(K_PLAYER_POS))
                mPlayerPos = savedInstanceState.getLong(K_PLAYER_POS);
            if (savedInstanceState.containsKey(K_SHOW_ALWAYS))
                mShowAlways = savedInstanceState.getBoolean(K_SHOW_ALWAYS);
            if (savedInstanceState.containsKey(K_IS_BOUND))
                mBound = savedInstanceState.getBoolean(K_IS_BOUND);
        }
        initializePlayerService();
        setShowAlways(mShowAlways);
        return generalView;
    }

//    private void setOnClickListener(ArrayList<View> children) {
//        for (View v : children) {
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PlaybackViewFragment.this.onClick(v);
//                }
//            });
//            if (v.getTouchables().size() > 0)
//                setOnClickListener(v.getTouchables());
//        }
//    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(K_PLAYER_POS, mPlayerPos);
        outState.putBoolean(K_SHOW_ALWAYS, mShowAlways);
        outState.putBoolean(K_IS_BOUND, mBound);

        super.onSaveInstanceState(outState);
    }

    public void setShowAlways(boolean showAlways) {
        mShowAlways = showAlways;
        if (generalView != null && !mShowAlways)
//            generalView.setVisibility(View.VISIBLE);
            generalView.setVisibility(isNowPlaying() ? View.VISIBLE : View.GONE);
    }

    public boolean isNowPlaying() {
        if (mService == null)
            return false;
        else
            return mService.isNowPlaying();
    }

    public void setOpenIntent(Intent intent) {
        openIntent = intent;
    }

    private void loadData() {
        if (mService != null) {
            mMediaItemPos = mService.getCurrentMediaItemIndex();
            if (mMediaItemPos != MediaPlaybackService.INITIAL_INDEX) {
                mMediaItem = mService.getMediaItems()[mMediaItemPos];
                tvTitle.setText(mMediaItem.Title);
                tvAuthor.setText(mMediaItem.Channel);
                if (mMediaItem.ImgUri != null && mMediaItem.ImgUri.length() > 0) {
                    Log.i(TAG, mMediaItem.ImgUri);
                    try {
                        URL url = new URL(mMediaItem.ImgUri);
                        Bitmap bitmap = NetUtils.loadImgFrom(url, getContext());
                        ivShowImage.setImageBitmap(bitmap);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        ivShowImage.setImageResource(R.drawable.ic_podcast_not);
                    }
                } else {
                    ivShowImage.setImageResource(R.drawable.ic_podcast_not);
                    Log.i(TAG, "No image available");
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setShowAlways(mShowAlways);
        loadData();
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mBound) {
//            mContext.unbindService(mConnection);
//            mBound = false;
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean addMediaToPlayList(MediaItem mediaItem) {
        if (mService != null)
            return mService.addMediaToPlayList(mediaItem);
        else return false;
    }

    public boolean removeMediaFromPlayList(MediaItem mediaItem) {
        if (mService != null)
            return mService.removeMediaToPlayList(mediaItem);
        else return false;
    }

    public MediaItem[] getMediaItems() {
        if (mService != null)
            return mService.getMediaItems();
        else return null;
    }

    public void clearMediaPlayList() {
        if (mService != null)
            mService.clearMediaPlayList();
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.exo_prev_button) {
            mService.playPreviousMediaItem();
            loadData();
        } else if (i == R.id.exo_play) {
            //mService.playMediaItem();
            mService.resumeMediaPlaying();
            loadData();
        } else if (i == R.id.exo_pause) {
            mService.pauseMediaPlaying();
            loadData();
        } else if (i == R.id.exo_next_button) {
            mService.playNextMediaItem();
            loadData();
        } else {
            if (openIntent != null) {
                if (openIntent.hasExtra(K_SHOW))
                    openIntent.removeExtra(K_SHOW);
                openIntent.putExtra(K_SHOW, mMediaItem);
                startActivity(openIntent);
            }
        }

    }

    public void playMediaItem(MediaItem mediaItem) {
        mService.playMediaItem(mediaItem);
        if (mService.getCurrentMediaItemIndex() == 0) {

        }
    }

    public void playMediaItem() {
        mService.playMediaItem();
    }

    private void initializePlayerService() {
        Intent intent = new Intent(mContext, MediaPlaybackService.class);
        if (!isMyServiceRunning(MediaPlaybackService.class))
            mContext.startService(intent);
        mContext.bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
            mService = binder.getService(PlaybackViewFragment.this);
            SimpleExoPlayer exoPlayer = mService.getmExoPlayer();
            playerView.setPlayer(exoPlayer);
            loadData();
            mBound = true;
            mListener.onBindService(mService);
            setShowAlways(mShowAlways);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };

    @Override
    public void onPlayListItemsChanged(int mediaItemsCount, int position) {
        if (mediaItemsCount == 0) {
            buttonNext.setEnabled(false);
        } else if (mediaItemsCount > 0 &&
                position > MediaPlaybackService.INITIAL_INDEX &&
                position < mediaItemsCount - 2)
            buttonNext.setEnabled(true);

    }

    @Override
    public void onTrackChanged(int position, MediaItem mediaItem) {
        loadData();
        boolean isThereNextSong = (position > MediaPlaybackService.INITIAL_INDEX &&
                position < mService.getMediaItems().length - 1);
        buttonNext.setEnabled(isThereNextSong);
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
        if (!mShowAlways) {
            generalView.setVisibility(playWhenReady ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onBindService(MediaPlaybackService service);
    }
}
