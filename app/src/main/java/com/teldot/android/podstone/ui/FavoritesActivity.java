package com.teldot.android.podstone.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.teldot.android.playerservicelib.data.MediaItem;
import com.teldot.android.playerservicelib.service.MediaPlaybackService;
import com.teldot.android.playerservicelib.ui.PlaybackViewFragment;
import com.teldot.android.podstone.R;
import com.teldot.android.podstone.data.provider.ShowContentProvider;
import com.teldot.android.podstone.ui.widget.PlayerWidgetService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class FavoritesActivity extends AppCompatActivity implements ShowListAdapter.ShowListItemIconOnClick, ShowListAdapter.ShowsListAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor>, PlaybackViewFragment.OnFragmentInteractionListener {
    private static final String TAG = FavoritesActivity.class.getSimpleName();

    //    private final String K_SHOWS_DATA = "K_SHOWS_DATA";
//    private static final String K_SHOW = "K_SHOW";
    private static final String K_RECYCLEDVIEW_STATE = "K_RECYCLEDVIEW_STATE";


    private static final int ID_SHOWS_LOADER = 457;

    private ShowListAdapter showsAdapter;
    private Parcelable listState;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PlaybackViewFragment playbackViewFragment;
    private MediaPlaybackService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar tbToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(tbToolbar);
        tbToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        RecyclerView mRecyclerView = findViewById(R.id.rv_shows_list);
        showsAdapter = new ShowListAdapter(this, this, this);
        mRecyclerView.setAdapter(showsAdapter);
        layoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.shows_list_column_items));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadShows();
            }
        });

        if (savedInstanceState == null) {
            ViewGroup playbackControlContainer = findViewById(R.id.player_frame);
            playbackViewFragment = new PlaybackViewFragment();
            playbackViewFragment.setShowAlways(false);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(playbackControlContainer.getId(), playbackViewFragment, playbackViewFragment.getClass().getName())
                    .commit();

        } else {
            playbackViewFragment = (PlaybackViewFragment) getSupportFragmentManager().findFragmentByTag(PlaybackViewFragment.class.getName());
        }
        loadShows();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShows();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite_menu_items, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.play_all_favorites) {
            if (showsAdapter == null || showsAdapter.getShows() == null || showsAdapter.getShows().length == 0) {
                Toast.makeText(this, R.string.favorites_activity_no_items_to_play, Toast.LENGTH_LONG).show();
                return true;
            }

            playbackViewFragment.clearMediaPlayList();
            for (MediaItem show : showsAdapter.getShows()) {
                playbackViewFragment.addMediaToPlayList(show);
            }
            playbackViewFragment.playMediaItem();
            PlayerWidgetService.startActionWidgetPlaying(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        listState = layoutManager.onSaveInstanceState();
        outState.putParcelable(K_RECYCLEDVIEW_STATE, listState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(K_RECYCLEDVIEW_STATE);
        }
    }

    private void loadShows() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (getSupportLoaderManager().getLoader(ID_SHOWS_LOADER) != null)
            getSupportLoaderManager().restartLoader(ID_SHOWS_LOADER, null, this);
        else
            getSupportLoaderManager().initLoader(ID_SHOWS_LOADER, null, this);
        colorCurrentShow();
    }

    @Override
    public void onClick(MediaItem show, View view) {
        switch (view.getId()) {
            case R.id.favorite_icon:
                askDeleteConfirmation(show);
                break;
            case R.id.share_icon:
                Spanned shareMess;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    shareMess = Html.fromHtml(show.getShareString(), Html.FROM_HTML_MODE_LEGACY);
                } else {
                    shareMess = Html.fromHtml(show.getShareString());
                }
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setText(shareMess)
                        .getIntent(), getString(R.string.action_share)));
                break;
            default:
                TextView tvDesc = view.findViewById(R.id.channel_list_item_tv_description);
                if (tvDesc.getVisibility() == View.GONE) tvDesc.setVisibility(View.VISIBLE);
                else tvDesc.setVisibility(View.GONE);
                break;
        }
    }

    private void askDeleteConfirmation(final MediaItem show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.fav_act_delete_conf_title));
        builder.setMessage(getString(R.string.fav_act_delete_conf_mess));
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteShow(show.ShowId);
                        loadShows();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteShow(long showId) {
        Uri uri = ShowContentProvider.getUri(ShowContentProvider.SHOW_ID, String.valueOf(showId));
        int res = getContentResolver().delete(uri, null, null);
        if (res > 0) {
            Toast.makeText(this, getString(R.string.favorite_show_removed), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.favorite_show_remove_error), Toast.LENGTH_LONG).show();
        }
    }

    private void colorCurrentShow() {
        if (mService != null && layoutManager.getChildCount() > 0) {
            int currentShowIdx = mService.getCurrentMediaItemIndex();
            if (currentShowIdx < 0) return;
            for (int i = 0; i < layoutManager.getChildCount(); i++) {
                if (i == currentShowIdx)
                    layoutManager.findViewByPosition(i).setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                else
                    layoutManager.findViewByPosition(i).setBackgroundColor(getResources().getColor(R.color.colorSecondaryText));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor cursor = null;

            @Nullable
            @Override
            public Cursor loadInBackground() {
                try {
                    Uri uri = ShowContentProvider.getUri(ShowContentProvider.SHOWS, String.valueOf(ShowContentProvider.NO_SHOW_ID));
                    return getContentResolver().query(uri, null, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onStartLoading() {
                if (cursor != null)
                    deliverResult(cursor);
                else
                    forceLoad();
            }

            public void deliverResult(Cursor data) {
                cursor = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null || data.getCount() > 0) {
            showsAdapter.setIsFavorite(true);
            showsAdapter.swapData(data);
            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
        colorCurrentShow();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        MediaItem[] nullData = null;
        showsAdapter.swapData(nullData);
    }


    @Override
    public void onBindService(MediaPlaybackService service) {
        mService = service;
        colorCurrentShow();
    }

    @Override
    public void onTrackChanged(int position, MediaItem mediaItem) {
        colorCurrentShow();
    }
}
