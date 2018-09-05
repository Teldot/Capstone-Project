package com.teldot.android.podstone.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.teldot.android.podstone.R;
import com.teldot.android.podstone.data.entities.Channel;
import com.teldot.android.podstone.data.entities.SearchResult;
import com.teldot.android.podstone.ui.widget.PlayerWidgetService;
import com.teldot.android.podstone.utils.FetchDataTask;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements ChannelsListAdapter.ChannelsListAdapterOnClickHandler, FetchDataTask.AsyncTaskCompleteListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ChannelsListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SearchResult searchResult;
    private String searchQuery;
    private TextView tvSearchResults;
    private int FETCHDATA_TASK;
    private Parcelable listState;

    private static final String K_SEARCH_RESULT = "K_SEARCH_RESULT";
    private static final String K_SEARCH_QUERY = "K_SEARCH_QUERY";
    private static final String K_FETCHDATA_TASK = "K_FETCHDATA_TASK";
    private static final String K_RECYCLEDVIEW_STATE = "K_RECYCLEDVIEW_STATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FORCE A CRASH
//        Crashlytics.getInstance().crash();

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        RecyclerView recyclerView = findViewById(R.id.rv_podcast_list);
        adapter = new ChannelsListAdapter(this, this);

        layoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.podcast_list_column_items));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        tvSearchResults = findViewById(R.id.tv_podcast_search_res);

        adapter = new ChannelsListAdapter(this, this);
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPodcasts(searchQuery);
            }
        });

//        AdView mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(K_RECYCLEDVIEW_STATE);
            searchQuery = savedInstanceState.getString(K_SEARCH_QUERY);
            searchResult = (SearchResult) savedInstanceState.getSerializable(K_SEARCH_RESULT);
            FETCHDATA_TASK = savedInstanceState.getInt(K_FETCHDATA_TASK);
        } else {
            FETCHDATA_TASK = FetchDataTask.TASK_STARTUP_LIST;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPodcasts(searchQuery);
        PlayerWidgetService.startActionWidgetPlaying(this);
    }

    private void loadPodcasts(String queryString) {
        mSwipeRefreshLayout.setRefreshing(true);
        searchQuery = queryString;
        if (queryString == null) {
            FETCHDATA_TASK = FetchDataTask.TASK_STARTUP_LIST;
            tvSearchResults.setVisibility(View.GONE);
            tvSearchResults.setText("");
        } else {
            FETCHDATA_TASK = FetchDataTask.TASK_SEARCH_LIST;
            tvSearchResults.setVisibility(View.VISIBLE);
            tvSearchResults.setText(String.format(getString(R.string.main_activity_text_result), queryString));
        }

        new FetchDataTask(this,
                this,
                FETCHDATA_TASK)
                .execute(queryString,
                        getString(R.string.PODCAST_DE_API_OFFSET_VAL));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(K_SEARCH_QUERY, searchQuery);
        outState.putSerializable(K_SEARCH_RESULT, searchResult);
        outState.putInt(K_FETCHDATA_TASK, FETCHDATA_TASK);
        listState = layoutManager.onSaveInstanceState();
        outState.putParcelable(K_RECYCLEDVIEW_STATE, listState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(K_RECYCLEDVIEW_STATE);
            searchQuery = savedInstanceState.getString(K_SEARCH_QUERY);
            searchResult = (SearchResult) savedInstanceState.getSerializable(K_SEARCH_RESULT);
            FETCHDATA_TASK = savedInstanceState.getInt(K_FETCHDATA_TASK);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            case R.id.action_favorites:
                Intent startFavoritesActivity = new Intent(this, FavoritesActivity.class);
                startActivity(startFavoritesActivity);
                return true;
            case R.id.action_about:
                Intent startAboutActivity = new Intent(this, AboutActivity.class);
                startActivity(startAboutActivity);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.menu_item_action_search));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadPodcasts(null);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > getResources().getInteger(R.integer.search_toolbar_max_query_length)) {
                    query = query.substring(0, getResources().getInteger(R.integer.search_toolbar_max_query_length));
                }
                mSwipeRefreshLayout.setRefreshing(true);
                loadPodcasts(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        return true;
    }

    @Override
    public void onClick(Channel channel) {
        Intent channelIntent = new Intent(this, ChannelActivity.class);
        channelIntent.putExtra(ChannelActivity.K_CHANNEL_ID, channel.ChannelId);
        startActivity(channelIntent);
    }

    @Override
    public void onTaskComplete(Object result, int task) {
        searchResult = (SearchResult) result;

        if (searchResult != null) {
            adapter.swapData(searchResult.Channels);
            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPreExecute() {

    }

}
