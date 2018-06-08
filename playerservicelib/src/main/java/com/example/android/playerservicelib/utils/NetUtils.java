package com.example.android.playerservicelib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
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

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImageFromBytes(byte[] image) {
        if (image == null || image.length == 0) return null;
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
