package com.teldot.android.podstone.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.teldot.android.playerservicelib.data.MediaItem;
import com.teldot.android.playerservicelib.service.MediaPlaybackService;
import com.teldot.android.playerservicelib.ui.PlaybackViewFragment;
import com.teldot.android.podstone.R;
import com.teldot.android.podstone.data.provider.ShowContentProvider;
import com.teldot.android.podstone.data.provider.ShowContract;
import com.teldot.android.podstone.ui.widget.PlayerWidgetService;
import com.teldot.android.podstone.utils.NetworkUtils;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ShowActivity extends AppCompatActivity implements PlaybackViewFragment.OnFragmentInteractionListener {

    private static final String K_SHOW = "K_SHOW";
    private static final String K_FORCE_NEW_PLAYING = "K_FORCE_NEW_PLAYING";
    private static final String K_PLAYER_POSITION = "K_PLAYER_POSITION";
    private static final String K_PLAY_WHEN_READY = "K_PLAY_WHEN_READY";

    private MediaItem mShow;

    private TextView tvChannel;
    private TextView tvShowName;
    private TextView tvAuthor;
    private TextView tvCopyright;
    private WebView tvDescription;
    private ImageButton ibFavIcon;
    private PlaybackViewFragment playbackViewFragment;
    private MediaPlaybackService mService;

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
        ibFavIcon = findViewById(R.id.favorite_icon);
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        if (savedInstanceState != null) {
            mShow = (MediaItem) savedInstanceState.getSerializable(K_SHOW);
            playWReady = savedInstanceState.getBoolean(K_PLAY_WHEN_READY);
            playerPosition = savedInstanceState.getLong(K_PLAYER_POSITION);
            forceNewPlaying = savedInstanceState.getBoolean(K_FORCE_NEW_PLAYING);

            playbackViewFragment = (PlaybackViewFragment) getSupportFragmentManager().findFragmentByTag(PlaybackViewFragment.class.getName());
        } else {
            mShow = (MediaItem) getIntent().getSerializableExtra(K_SHOW);
            forceNewPlaying = getIntent().getBooleanExtra(K_FORCE_NEW_PLAYING, false);
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
        tvDescription.loadData(mShow.Description, null, null);

        if (mShow.IsInDb)
            ibFavIcon.setImageResource(R.drawable.ic_star_black_24dp);
        else
            ibFavIcon.setImageResource(R.drawable.ic_star_border_black_24dp);
        if (forceNewPlaying)
            playbackViewFragment.playMediaItem(mShow);
        PlayerWidgetService.startActionWidgetPlaying(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(K_PLAYER_POSITION, playerPosition);
        outState.putBoolean(K_PLAY_WHEN_READY, playWReady);
        outState.putSerializable(K_SHOW, mShow);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onButtonClick(View view) {
        if (view.getId() == R.id.favorite_icon) {
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
        } else if (view.getId() == R.id.share_fab) {
            Spanned shareMess;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                shareMess = Html.fromHtml(mShow.getShareString(), Html.FROM_HTML_MODE_LEGACY);
            } else {
                shareMess = Html.fromHtml(mShow.getShareString());
            }
            startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setText(shareMess)
                    .getIntent(), getString(R.string.action_share)));
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

    @Override
    public void onTrackChanged(int position, MediaItem mediaItem) {

    }
}
