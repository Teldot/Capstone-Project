package com.teldot.android.podstone.data.entities;

import com.teldot.android.playerservicelib.data.MediaItem;

import java.io.Serializable;

public class Channel implements Serializable {
    public long ChannelId;
    public String Title;
    public String Description;
    public String Copyright;
    public String ChanDate;
    public String ImageUrl;
    public byte[] Image;
    public long Vote;
    public double Rating;
    public MediaItem[] Shows;
}
