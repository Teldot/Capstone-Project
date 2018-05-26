package com.example.android.playerservicelib.data;

import java.io.Serializable;

public class MediaItem implements Serializable {
    public MediaItem() {
    }

    public MediaItem(String title, String channel, String mediaUri, String imgUri) {
        this.Title = title;
        this.Channel = channel;
        this.MediaUri = mediaUri;
        this.ImgUri = imgUri;
    }

    public long ShowId;
    public String Title;
    public String Description;
    public String Author;
    public String ShowDate;
    public String MediaUri;
    public String PodLink;
    public double Rating;
    public double Votes;
    public String Copyright;
    public String Channel;
    public double Size;
    public String ImgUri;
    public byte[] Image;
    public boolean IsInDb;
}

