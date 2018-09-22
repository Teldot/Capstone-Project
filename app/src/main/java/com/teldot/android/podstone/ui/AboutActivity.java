package com.teldot.android.podstone.ui;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.teldot.android.podstone.BuildConfig;
import com.teldot.android.podstone.R;

public class AboutActivity extends AppCompatActivity {
    private Toolbar tbToolbar;
    private ImageView imageView;
    private TextView tvAppver;
    private TextView tvTermsCond;
    private TextView tvPrivPol;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        tvAppver = findViewById(R.id.tv_app_ver);
        String ver = String.format(getString(R.string.about_act_app_name), BuildConfig.VERSION_NAME);
        tvAppver.setText(ver);
        tbToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(tbToolbar);
        tbToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imageView = findViewById(R.id.iv_logo);
        tvTermsCond = findViewById(R.id.tv_terms_cond);
        tvPrivPol = findViewById(R.id.tv_privacy_policy);
        tvTermsCond.setMovementMethod(LinkMovementMethod.getInstance());
        tvPrivPol.setMovementMethod(LinkMovementMethod.getInstance());
        //imageView.setOnClickListener(this);

    }

//    @Override
//    public void onClick(View v) {
//        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    this,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//        String mess = "";
//
//        Bitmap bitmap = drawableToBitmap(imageView.getDrawable(), 512, 512);
//        String filename = "PodCastLogo.png";
//        String downloadDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String filePath = downloadDirectory + "/" + filename;
//
//        FileOutputStream outStream = null;
//        try {
//            outStream = new FileOutputStream(filePath);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            mess = e.getMessage();
//        } finally {
//            try {
//                if (outStream != null) {
//                    outStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                mess = e.getMessage();
//            }
//        }
//        if (mess == "")
//            mess = "File save to " + filePath;
//        Toast.makeText(AboutActivity.this, mess, Toast.LENGTH_LONG).show();
//    }

    public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }


        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
