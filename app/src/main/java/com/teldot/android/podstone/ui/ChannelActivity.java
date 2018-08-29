package com.teldot.android.podstone.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.teldot.android.playerservicelib.data.MediaItem;
import com.teldot.android.playerservicelib.service.MediaPlaybackService;
import com.teldot.android.playerservicelib.ui.PlaybackViewFragment;
import com.example.android.podstone.R;
import com.teldot.android.podstone.data.entities.Channel;
import com.teldot.android.podstone.data.provider.ShowContentProvider;
import com.teldot.android.podstone.data.provider.ShowContract;
import com.teldot.android.podstone.utils.FetchDataTask;
import com.teldot.android.podstone.utils.NetworkUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

public class ChannelActivity extends AppCompatActivity implements FetchDataTask.AsyncTaskCompleteListener<Object>, ShowListAdapter.ShowsListAdapterOnClickHandler, ShowListAdapter.ShowListItemIconOnClick, PlaybackViewFragment.OnFragmentInteractionListener {

    public static final String K_CHANNEL_ID = "K_CHANNEL_ID";
    public static final String K_CHANNEL = "K_CHANNEL";
    private static final String K_RECYCLEDVIEW_STATE = "K_RECYCLEDVIEW_STATE";
    private static final String K_SHOW = "K_SHOW";
    private static final String K_FORCE_NEW_PLAYING = "K_FORCE_NEW_PLAYING";

    private Toolbar tbToolbar;
    private ImageView ivChannelImage;
    private RatingBar rbRating;
    private TextView tvVotes;
    private TextView tvTitle;
    private TextView tvDate;
    private RecyclerView mRecyclerView;
    private ShowListAdapter showsAdapter;
    private Parcelable listState;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PlaybackViewFragment playbackViewFragment;
    private AdView mAdView;

    private MediaPlaybackService mService;

    private Channel mChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        tbToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(tbToolbar);
        tbToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ivChannelImage = findViewById(R.id.activity_channel_iv_image);
        rbRating = findViewById(R.id.podcast_list_item_rb_rating);
        tvVotes = findViewById(R.id.podcast_list_item_tv_votes);
        tvTitle = findViewById(R.id.podcast_list_item_tv_title);
        tvDate = findViewById(R.id.podcast_list_item_tv_date);
        mRecyclerView = findViewById(R.id.rv_shows_list);
        showsAdapter = new ShowListAdapter(this, this, this);
        mRecyclerView.setAdapter(showsAdapter);
        layoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.shows_list_column_items));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                remapChannelData();
            }
        });

//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        if (savedInstanceState == null) {
            long channelId = getIntent().getLongExtra(K_CHANNEL_ID, 0);
            loadChannelData(channelId);

            ViewGroup playbackControlContainer = findViewById(R.id.player_frame);
            playbackViewFragment = new PlaybackViewFragment();
            playbackViewFragment.setShowAlways(false);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(playbackControlContainer.getId(), playbackViewFragment, playbackViewFragment.getClass().getName())
                    .commit();
        } else {
            mChannel = (Channel) savedInstanceState.getSerializable(K_CHANNEL);
            playbackViewFragment = (PlaybackViewFragment) getSupportFragmentManager().findFragmentByTag(PlaybackViewFragment.class.getName());
        }

    }

    private void setOpenIntet2Fragment() {
        if (playbackViewFragment == null) return;
        int listLength = playbackViewFragment.getMediaItems() == null ? 0 : playbackViewFragment.getMediaItems().length;
        if (listLength == 1) {
            Intent openIntent = new Intent(this, ShowActivity.class);
            playbackViewFragment.setOpenIntent(openIntent);
        } else if (listLength > 1) {
            Intent openIntent = new Intent(this, FavoritesActivity.class);
            playbackViewFragment.setOpenIntent(openIntent);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        setOpenIntet2Fragment();
    }

    private void loadChannelData(long channelId) {
        mSwipeRefreshLayout.setRefreshing(true);
        new FetchDataTask(this,
                this,
                FetchDataTask.TASK_SINGLE_CHANNEL)
                .execute(channelId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(K_CHANNEL, mChannel);
        listState = layoutManager.onSaveInstanceState();
        outState.putParcelable(K_RECYCLEDVIEW_STATE, listState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(K_RECYCLEDVIEW_STATE);
            mChannel = (Channel) savedInstanceState.getSerializable(K_CHANNEL);
        }
    }

    private void loadData() {
        if (mChannel.Image != null)
            ivChannelImage.setImageBitmap(NetworkUtils.getImageFromBytes(mChannel.Image));
        rbRating.setRating((float) mChannel.Rating);
        tvVotes.setText(String.format(getString(R.string.votes_format), String.valueOf(mChannel.Vote)));
        tvTitle.setText(mChannel.Title);
        tbToolbar.setTitle("Channel: " + mChannel.Title);
        tvDate.setText(mChannel.ChanDate);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void remapChannelData() {
        if (mChannel != null) {
            checkShowsInDb();
            loadData();
            showsAdapter.swapData(mChannel.Shows);
            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        remapChannelData();
        setOpenIntet2Fragment();
    }

    @Override
    public void onTaskComplete(Object result, int task) {
        mChannel = (Channel) result;
        remapChannelData();
    }

    @Override
    public void onPreExecute() {

    }

    private void checkShowsInDb() {
        if (mChannel.Shows == null || mChannel.Shows.length == 0) return;

        HashMap<Long, Integer> idsPos = new HashMap<>();
        String[] ids = new String[mChannel.Shows.length];
        String selection = ShowContract.ShowEntry._ID + " IN (";
        for (int i = 0; i < mChannel.Shows.length; i++) {
            mChannel.Shows[i].IsInDb = false;
            idsPos.put(mChannel.Shows[i].ShowId, i);
            ids[i] = String.valueOf(mChannel.Shows[i].ShowId);
            selection += "?,";
        }
        selection = selection.substring(0, selection.length() - 1) + ")";

        Uri uri = ShowContentProvider.getUri(ShowContentProvider.IS_SHOW_IN_DB, String.valueOf(ShowContentProvider.NO_SHOW_ID));
        Cursor c = getContentResolver().query(uri, null, selection, ids, null);

        if (c != null && c.getCount() > 0) {
            int idColIdx = c.getColumnIndex(ShowContract.ShowEntry._ID);
            c.moveToFirst();
            do {
                long id = c.getLong(idColIdx);
                int pos = idsPos.get(id);
                mChannel.Shows[pos].IsInDb = true;
            }
            while (c.moveToNext());

            c.close();
        }

    }

    @Override
    public void onClick(MediaItem podcast, View view) {
        switch (view.getId()) {
            case R.id.favorite_icon:
                ImageButton b = (ImageButton) view;
                if (podcast.IsInDb) {
                    if (deleteShow(podcast.ShowId)) {
                        b.setImageResource(R.drawable.ic_star_border_black_24dp);
                        podcast.IsInDb = false;
                    }
                } else {
                    deleteShow(podcast.ShowId);
                    if (saveFavorite(podcast)) {
                        b.setImageResource(R.drawable.ic_star_black_24dp);
                        podcast.IsInDb = true;
                    }
                }
                break;
            case R.id.play_icon:
                Intent playPodcast = new Intent(this, ShowActivity.class);
                playPodcast.putExtra(K_SHOW, podcast);
                playPodcast.putExtra(K_FORCE_NEW_PLAYING, true);
                startActivity(playPodcast);
                break;
            default:
                TextView tvDesc = view.findViewById(R.id.channel_list_item_tv_description);
                if (tvDesc.getVisibility() == View.GONE) tvDesc.setVisibility(View.VISIBLE);
                else tvDesc.setVisibility(View.GONE);
                break;
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

    private boolean saveFavorite(MediaItem podcast) {
        Uri uri = ShowContentProvider.getUri(ShowContentProvider.SHOWS, String.valueOf(ShowContentProvider.NO_SHOW_ID));
        ContentValues values = new ContentValues();
        values.put(ShowContract.ShowEntry._ID, podcast.ShowId);
        values.put(ShowContract.ShowEntry.COLUMN_TITLE, podcast.Title);
        values.put(ShowContract.ShowEntry.COLUMN_AUTHOR, podcast.Author);
        values.put(ShowContract.ShowEntry.COLUMN_CHANNEL, podcast.Channel);
        values.put(ShowContract.ShowEntry.COLUMN_COPYRIGHT, podcast.Copyright);
        values.put(ShowContract.ShowEntry.COLUMN_DATE, podcast.ShowDate);
        values.put(ShowContract.ShowEntry.COLUMN_DESCRIPTION, podcast.Description);
        values.put(ShowContract.ShowEntry.COLUMN_IMAGE, podcast.Image);
        values.put(ShowContract.ShowEntry.COLUMN_MEDIA_LINK, podcast.MediaUri);
        values.put(ShowContract.ShowEntry.COLUMN_PLAYER_POS, 0);
        values.put(ShowContract.ShowEntry.COLUMN_RATING, podcast.Rating);
        values.put(ShowContract.ShowEntry.COLUMN_SIZE, podcast.Size);
        values.put(ShowContract.ShowEntry.COLUMN_VOTES, podcast.Votes);

        Uri res = getContentResolver().insert(uri, values);
        if (res != null) {
            Toast.makeText(this, String.format(getString(R.string.favorite_show_saved), podcast.Title), Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, getString(R.string.favorite_show_save_error), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onBindService(MediaPlaybackService service) {
        mService = service;
    }

    @Override
    public void onTrackChanged(int position, MediaItem mediaItem) {

    }
}
