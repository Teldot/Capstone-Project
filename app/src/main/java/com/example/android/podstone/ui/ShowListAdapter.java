package com.example.android.podstone.ui;

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

import com.example.android.playerservicelib.data.MediaItem;
import com.example.android.podstone.R;
import com.example.android.podstone.data.provider.ShowContract;
import com.example.android.podstone.utils.NetworkUtils;


public class ShowListAdapter extends RecyclerView.Adapter<ShowListAdapter.ShowListAdapterViewHolder> {

    public interface ShowsListAdapterOnClickHandler {
        void onClick(MediaItem podcast, View v);
    }

    public interface ShowListItemIconOnClick {
        void onClick(MediaItem show, View view);
    }


    private MediaItem[] shows;
    private final Context mContext;
    private final ShowsListAdapterOnClickHandler mClickHandler;
    private final ShowListItemIconOnClick showListItemIconOnClick;

    public ShowListAdapter(ShowsListAdapterOnClickHandler onClickHandler, ShowListItemIconOnClick onClickIcon, Context context) {
        mContext = context;
        mClickHandler = onClickHandler;
        showListItemIconOnClick = onClickIcon;
    }

    @NonNull
    @Override
    public ShowListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.channel_show_list_item, parent, false);
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
    }

    @Override
    public int getItemCount() {
        if (shows == null) return 0;
        return shows.length;
    }

    public MediaItem[] getShows() {
        return shows;
    }

    public MediaItem[] swapData(MediaItem[] _shows) {
        if (_shows == null || _shows.length == 0) {
            shows = null;
            this.notifyDataSetChanged();
            return shows;
        }
        shows = _shows;
        this.notifyDataSetChanged();
        return shows;
    }

    public MediaItem[] swapData(Cursor data) {
        if (data == null) {
            return swapData(new MediaItem[0]);
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

        return swapData(showData);
    }

    public class ShowListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView ivShowImage;
        public final AppCompatRatingBar ratingBar;
        public final TextView tvVotes;
        public final TextView tvTitle;
        public final TextView tvDate;
        public final TextView tvDescription;
        public final ImageButton ibPlayIcon;
        public final ImageButton ibFavIcon;

        ShowListAdapterViewHolder(View view) {
            super(view);
            ivShowImage = view.findViewById(R.id.channel_list_item_iv_image);
            ratingBar = view.findViewById(R.id.channel_list_item_rb_rating);
            tvVotes = view.findViewById(R.id.channel_list_item_tv_votes);
            tvTitle = view.findViewById(R.id.channel_list_item_tv_title);
            tvDate = view.findViewById(R.id.channel_list_item_tv_date);
            tvDescription = view.findViewById(R.id.channel_list_item_tv_description);
            ibPlayIcon = view.findViewById(R.id.play_icon);
            ibPlayIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showListItemIconOnClick.onClick(getShows()[getAdapterPosition()], v);
                }
            });
            ibFavIcon = view.findViewById(R.id.favorite_icon);
            ibFavIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showListItemIconOnClick.onClick(getShows()[getAdapterPosition()], v);
                }
            });

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            MediaItem o = getShows()[adapterPosition];

            mClickHandler.onClick(o, v);
        }
    }
}