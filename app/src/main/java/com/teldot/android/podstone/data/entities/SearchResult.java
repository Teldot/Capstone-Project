package com.teldot.android.podstone.data.entities;

import java.io.Serializable;

public class SearchResult implements Serializable {
    public int Limit;
    public int Offset;
    public int Count;
    public Channel[] Channels;
}
