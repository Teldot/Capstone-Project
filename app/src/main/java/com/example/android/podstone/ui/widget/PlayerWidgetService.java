package com.example.android.podstone.ui.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.android.podstone.R;

public class PlayerWidgetService extends IntentService {
    private static final String ACTION_PLAYING = "com.example.android.podstone.action.playing";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PlayerWidgetService() {
        super("PlayerWidgetService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAYING.equals(action)) {
                handleActionPlaying();
            }
        }
    }

    private void handleActionPlaying() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, NowPlayingWidget.class));
        NowPlayingWidget.updatePlayingWidgets(this, appWidgetManager, appWidgetIds);
    }

    public static void startActionWidgetPlaying(Context context) {
        Intent intent = new Intent(context, PlayerWidgetService.class);
        intent.setAction(ACTION_PLAYING);
        context.startService(intent);
    }
}
