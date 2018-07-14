package com.example.android.playerservicelib.data;

import java.io.Serializable;

/**
 * Media Item serializable class
 */
public class MediaItem implements Serializable {
    private static final String SHARE_TEMP = "Enjoy this great podcast with <i><b>PodStone</b></i>:<p>Channel: %s</p><p>Title: %s</p><p>%s</p>";

    public MediaItem() {
    }

    /**
     * Entity Id.
     */
    public long ShowId;
    /**
     * Media title.
     */
    public String Title;
    /**
     * Media Description.
     */
    public String Description;
    /**
     * Media Author.
     */
    public String Author;
    /**
     * Media date.
     */
    public String ShowDate;
    /**
     * Media Uri.
     */
    public String MediaUri;
    /**
     * Media rating.
     */
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

