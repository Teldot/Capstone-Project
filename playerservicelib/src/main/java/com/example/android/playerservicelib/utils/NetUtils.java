package com.example.android.playerservicelib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.net.URL;

public class NetUtils {
    private static final String TAG = NetUtils.class.getSimpleName();

    public static Bitmap loadImgFrom(URL url, Context context) {
        Bitmap bitmap = null;
        try {
            bitmap = Picasso.with(context).load(url.toString()).get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
        return bitmap;
    }
}
