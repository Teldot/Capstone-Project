package com.teldot.android.podstone.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teldot.android.podstone.R;
import com.teldot.android.podstone.data.entities.Channel;
import com.teldot.android.podstone.utils.NetworkUtils;


public class ChannelsListAdapter extends RecyclerView.Adapter<ChannelsListAdapter.ChannelsListAdapterViewHolder> {

    public interface ChannelsListAdapterOnClickHandler {
        void onClick(Channel channel);
    }

    private Channel[] channels;
    private final Context mContext;
    private final ChannelsListAdapterOnClickHandler mClickHandler;


    ChannelsListAdapter(ChannelsListAdapterOnClickHandler onClickHandler, Context context) {
        mContext = context;
        mClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public ChannelsListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.podcast_list_item, parent, false);
        return new ChannelsListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelsListAdapterViewHolder holder, int position) {
        Bitmap bitmap = NetworkUtils.getImageFromBytes(getChannels()[position].Image);
        holder.ivChannelImage.setImageBitmap(bitmap);
        holder.tvTitle.setText(getChannels()[position].Title);
        holder.ratingBar.setRating((float) getChannels()[position].Rating);
        String votesFormat = mContext.getResources().getString(R.string.votes_format);
        holder.tvVotes.setText(String.format(votesFormat, String.valueOf(getChannels()[position].Vote)));
        holder.tvDate.setText(getChannels()[position].ChanDate);

    }

    @Override
    public int getItemCount() {
        if (channels == null) return 0;
        return channels.length;
    }

    private Channel[] getChannels() {
        return channels;
    }

    public void swapData(Channel[] _channels) {
        if (_channels == null || _channels.length == 0) {
            channels = null;
            this.notifyDataSetChanged();
            return;
        }
        channels = _channels;
        this.notifyDataSetChanged();
    }

    public class ChannelsListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView ivChannelImage;
        final AppCompatRatingBar ratingBar;
        final TextView tvVotes;
        final TextView tvTitle;
        final TextView tvDate;

        ChannelsListAdapterViewHolder(View view) {
            super(view);
            ivChannelImage = view.findViewById(R.id.podcast_list_item_iv_image);
            ratingBar = view.findViewById(R.id.podcast_list_item_rb_rating);
            tvVotes = view.findViewById(R.id.podcast_list_item_tv_votes);
            tvTitle = view.findViewById(R.id.podcast_list_item_tv_title);
            tvDate = view.findViewById(R.id.podcast_list_item_tv_date);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Channel o = getChannels()[adapterPosition];

            mClickHandler.onClick(o);
        }
    }
}
