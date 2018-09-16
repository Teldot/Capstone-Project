package com.teldot.android.podstone.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teldot.android.playerservicelib.data.MediaItem;
import com.teldot.android.podstone.R;
import com.teldot.android.podstone.data.provider.ShowContract;
import com.teldot.android.podstone.utils.FetchDataTask;
import com.teldot.android.podstone.utils.NetworkUtils;


public class ShowListAdapter extends RecyclerView.Adapter<ShowListAdapter.ShowListAdapterViewHolder> implements FetchDataTask.AsyncTaskCompleteListener<Object> {


    public interface ShowsListAdapterOnClickHandler {
        void onClick(MediaItem podcast, View v);
    }

    public interface ShowListItemIconOnClick {
        void onClick(MediaItem show, View view);
    }

    private boolean isFavorite;


    private MediaItem[] shows;
    private final Context mContext;
    private final ShowsListAdapterOnClickHandler mClickHandler;
    private final ShowListItemIconOnClick showListItemIconOnClick;
    private final RecyclerView mRecyclerView;

    ShowListAdapter(RecyclerView recyclerView, ShowsListAdapterOnClickHandler onClickHandler, ShowListItemIconOnClick onClickIcon, Context context) {
        mContext = context;
        mClickHandler = onClickHandler;
        showListItemIconOnClick = onClickIcon;
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ShowListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int listItemId;
        if (isFavorite)
            listItemId = R.layout.favorite_show_list_item;
        else
            listItemId = R.layout.channel_show_list_item;

        View view = inflater.inflate(listItemId, parent, false);
        return new ShowListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowListAdapterViewHolder holder, int position) {
        if (getShows()[position].Image != null) {
            Bitmap bitmap = NetworkUtils.getImageFromBytes(getShows()[position].Image);
            holder.ivShowImage.setImageBitmap(bitmap);
            holder.ivShowImage.setVisibility(View.VISIBLE);
        } else
            holder.ivShowImage.setVisibility(View.GONE);
        holder.tvTitle.setText(getShows()[position].Title);
//        holder.ibPlayIcon.setTag(getShows()[position].ShowId);
        holder.ratingBar.setRating((float) getShows()[position].Rating);
        String votesFormat = mContext.getResources().getString(R.string.votes_format);
        holder.tvVotes.setText(String.format(votesFormat, String.valueOf(getShows()[position].Votes)));
        holder.tvDate.setText(getShows()[position].ShowDate);
        holder.tvDescription.setText(getShows()[position].Description);
        if (getShows()[position].IsInDb)
            holder.ibFavIcon.setImageResource(R.drawable.ic_star_black_24dp);
        else
            holder.ibFavIcon.setImageResource(R.drawable.ic_star_border_black_24dp);
        checkShowUrl(position, getShows()[position].MediaUri);
    }

    private void checkShowUrl(int index, String url) {
        new FetchDataTask(mContext,
                this,
                FetchDataTask.TASK_CHECK_URL)
                .execute(url, String.valueOf(index));
    }

    @Override
    public void onTaskComplete(Object result, int task) {
        switch (task) {
            case FetchDataTask.TASK_CHECK_URL:
                String res = String.valueOf(result);
                String[] resS = res.split("-");
                boolean isOK = Boolean.parseBoolean(resS[1]);
                int pos = Integer.parseInt(resS[0]);
                final ShowListAdapterViewHolder holder = (ShowListAdapterViewHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);
                holder.ibPlayShareIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.mediaUrlOK)
                            showListItemIconOnClick.onClick(getShows()[holder.getAdapterPosition()], v);
                        else {
                            holder.ibFavIcon.setEnabled(false);
                            String mes = mContext.getResources().getString(R.string.podcast_list_item_broken_link);
                            Toast.makeText(mContext, mes, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                if (!isOK) return;

                holder.mediaUrlOK = true;
                holder.ibPlayShareIcon.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                break;
        }
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public int getItemCount() {
        if (shows == null) return 0;
        return shows.length;
    }

    public void setIsFavorite(boolean isfavorite) {
        this.isFavorite = isfavorite;
    }

    public MediaItem[] getShows() {
        if (shows == null)
            shows = new MediaItem[0];
        return shows;
    }

    public void swapData(MediaItem[] _shows) {
        if (_shows == null || _shows.length == 0) {
            shows = null;
            this.notifyDataSetChanged();
            return;
        }
        shows = _shows;
        this.notifyDataSetChanged();
    }

    public void swapData(Cursor data) {
        if (data == null) {
            swapData(new MediaItem[0]);
            return;
        }
        int col_ShowId = data.getColumnIndex(ShowContract.ShowEntry._ID);
        int col_Image = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_IMAGE);
        int col_Title = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_TITLE);
        int col_Rating = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_RATING);
        int col_Votes = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_VOTES);
        int col_Date = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_DATE);
        int col_Description = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_DESCRIPTION);
        int col_MediaLink = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_MEDIA_LINK);
        int col_Channel = data.getColumnIndex(ShowContract.ShowEntry.COLUMN_CHANNEL);
        MediaItem[] showData = new MediaItem[data.getCount()];
        for (int i = 0; i < data.getCount(); i++) {
            data.moveToPosition(i);
            showData[i] = new MediaItem();
            showData[i].IsInDb = true;
            showData[i].ShowId = data.getLong(col_ShowId);
            showData[i].Image = data.getBlob(col_Image);
            showData[i].Title = data.getString(col_Title);
            showData[i].Rating = data.getDouble(col_Rating);
            showData[i].Votes = data.getDouble(col_Votes);
            showData[i].ShowDate = data.getString(col_Date);
            showData[i].Description = data.getString(col_Description);
            showData[i].MediaUri = data.getString(col_MediaLink);
            showData[i].Channel = data.getString(col_Channel);
        }

        swapData(showData);
    }

    public class ShowListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView ivShowImage;
        final AppCompatRatingBar ratingBar;
        final TextView tvVotes;
        final TextView tvTitle;
        final TextView tvDate;
        final TextView tvDescription;
        final ImageButton ibPlayShareIcon;
        final ImageButton ibFavIcon;
        public boolean mediaUrlOK = false;

        ShowListAdapterViewHolder(View view) {
            super(view);
            ivShowImage = view.findViewById(R.id.channel_list_item_iv_image);
            ratingBar = view.findViewById(R.id.channel_list_item_rb_rating);
            tvVotes = view.findViewById(R.id.channel_list_item_tv_votes);
            tvTitle = view.findViewById(R.id.channel_list_item_tv_title);
            tvDate = view.findViewById(R.id.channel_list_item_tv_date);
            tvDescription = view.findViewById(R.id.channel_list_item_tv_description);
            if (isFavorite)
                ibPlayShareIcon = view.findViewById(R.id.share_icon);
            else
                ibPlayShareIcon = view.findViewById(R.id.play_icon);
            ibFavIcon = view.findViewById(R.id.favorite_icon);
            ibFavIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaUrlOK)
                        showListItemIconOnClick.onClick(getShows()[getAdapterPosition()], v);
                }
            });

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!isFavorite && !mediaUrlOK)
                return;

            int adapterPosition = getAdapterPosition();
            MediaItem o = getShows()[adapterPosition];

            mClickHandler.onClick(o, v);
        }
    }
}
