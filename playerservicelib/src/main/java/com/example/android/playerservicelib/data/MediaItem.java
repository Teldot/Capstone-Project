package com.example.android.playerservicelib.data;

import java.io.Serializable;

public class MediaItem implements Serializable {
    private static final String SHARE_TEMP = "Enjoy this great podcast with <i><b>PodStone</b></i>:<p>Channel: %s</p><p>Title: %s</p><p>%s</p>";

    public MediaItem() {
    }

    public long ShowId;
    public String Title;
    public String Description;
    public String Author;
    public String ShowDate;
    public String MediaUri;
    public double Rating;
    public double Votes;
    public String Copyright;
    public String Channel;
    public double Size;
    public String ImgUri;
    public byte[] Image;
    public boolean IsInDb;

    public String getShareString() {
        return String.format(SHARE_TEMP, Channel, Title, MediaUri);
    }
}

