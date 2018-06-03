package com.example.android.podstone.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.playerservicelib.data.MediaItem;
import com.example.android.podstone.R;
import com.example.android.podstone.data.provider.ShowContentProvider;

public class FavoritesActivity extends AppCompatActivity implements ShowListAdapter.ShowListItemIconOnClick, ShowListAdapter.ShowsListAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = FavoritesActivity.class.getSimpleName();

    private final String K_SHOWS_DATA = "K_SHOWS_DATA";
    private static final String K_SHOW = "K_SHOW";

    private static final int ID_SHOWS_LOADER = 457;

    private Toolbar tbToolbar;
    private RecyclerView mRecyclerView;
    private ShowListAdapter showsAdapter;
    private Parcelable listState;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        tbToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(tbToolbar);
        tbToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = findViewById(R.id.rv_shows_list);
        showsAdapter = new ShowListAdapter(this, this, this);
        mRecyclerView.setAdapter(showsAdapter);
        layoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.shows_list_column_items));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadShows();
            }
        });

        loadShows();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShows();
    }

    private void loadShows() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (getSupportLoaderManager().getLoader(ID_SHOWS_LOADER) != null)
            getSupportLoaderManager().restartLoader(ID_SHOWS_LOADER, null, this);
        else
            getSupportLoaderManager().initLoader(ID_SHOWS_LOADER, null, this);
    }

    @Override
    public void onClick(MediaItem show, View view) {
        switch (view.getId()) {
            case R.id.favorite_icon:
                askDeleteConfirmation(show);
                break;
            case R.id.play_icon:
                Intent playPodcast = new Intent(this, ShowActivity.class);
                playPodcast.putExtra(K_SHOW, show);
                startActivity(playPodcast);
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
            showsAdapter.swapData(data);
            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        MediaItem[] nullData = null;
        showsAdapter.swapData(nullData);
    }


}