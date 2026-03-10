package com.example.pnotsample.ViewFlipperSample;

import android.content.Context;
import android.content.SharedPreferences;

public class CarouselState {

    private static final String PREFS   = "carousel_prefs";
    private static final String KEY_IDX = "index";
    private static final String KEY_STP = "stopped";

    public static void reset(Context ctx) {
        prefs(ctx).edit().putInt(KEY_IDX, 0).putBoolean(KEY_STP, false).apply();
    }

    public static void saveIndex(Context ctx, int i) {
        prefs(ctx).edit().putInt(KEY_IDX, i).apply();
    }

    public static int getIndex(Context ctx) {
        return prefs(ctx).getInt(KEY_IDX, 0);
    }

    public static void markStopped(Context ctx) {
        prefs(ctx).edit().putBoolean(KEY_STP, true).apply();
    }

    public static boolean isStopped(Context ctx) {
        return prefs(ctx).getBoolean(KEY_STP, true);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
