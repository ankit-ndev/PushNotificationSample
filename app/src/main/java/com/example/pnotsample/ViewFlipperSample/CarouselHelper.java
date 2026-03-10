package com.example.pnotsample.ViewFlipperSample;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.pnotsample.R;

public class CarouselHelper {

    private static final String TAG             = "PNotSample_Carousel";
    private static final String CHANNEL_ID      = "carousel_channel";
    public  static final int    NOTIFICATION_ID  = 101;
    public  static final String ACTION_FLIP      = "com.example.pnotsample.CAROUSEL_FLIP";
    public  static final String ACTION_DISMISS   = "com.example.pnotsample.CAROUSEL_DISMISS";
    private static final long   INTERVAL_MS      = 600L;

    static final String[][] ITEMS = {
            {"Breaking News",  "Markets hit an all time high today across global exchanges"},
            {"Flash Sale",     "50% off on all electronics — today only, limited stock"   },
            {"Weather Update", "Heavy rain expected in your city tonight, stay safe"      },
            {"Breaking News",  "Markets hit an all time high today across global exchanges"},
            {"Flash Sale",     "50% off on all electronics — today only, limited stock"   },
            {"Weather Update", "Heavy rain expected in your city tonight, stay safe"      },
            {"Breaking News",  "Markets hit an all time high today across global exchanges"},
            {"Flash Sale",     "50% off on all electronics — today only, limited stock"   },
    };

    static final int[] IMAGES = {
            R.drawable.pizza_1,
            R.drawable.pizza_2,
            R.drawable.pizza_3,
            R.drawable.pizza_4,
            R.drawable.pizza_5,
            R.drawable.pizza_6,
            R.drawable.pizza_7,
            R.drawable.pizza_8,
    };

    public static void start(Context context) {
        Log.d(TAG, "start: initialising carousel");
        createChannel(context);
        CarouselState.reset(context);
        show(context, 0);
//        scheduleFlip(context, 0);
    }

    public static void stop(Context context) {
        Log.d(TAG, "stop: cancelling carousel");
//        cancelAlarm(context);
        CarouselState.markStopped(context);
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

//    public static void onAlarmFired(Context context) {
//        if (CarouselState.isStopped(context)) {
//            Log.d(TAG, "onAlarmFired: carousel stopped, skipping");
//            cancelAlarm(context);
//            return;
//        }
//        int current = CarouselState.getIndex(context);
//        int next    = (current + 1) % ITEMS.length;
//        Log.d(TAG, "onAlarmFired: flipping from " + current + " → " + next);
//        CarouselState.saveIndex(context, next);
//        show(context, next);
//        scheduleFlip(context, next);
//    }

    private static void show(Context context, int index) {
        Log.d(TAG, "show: building notification for index " + index);
        RemoteViews views = getRemoteViews(context, index);
        Log.d(TAG, "show: setDisplayedChild → " + index);

        Intent dismissIntent = new Intent(context, CarouselReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent dismissPi = PendingIntent.getBroadcast(context, 0, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomBigContentView(views)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setDeleteIntent(dismissPi)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)   // ensures expanded by default
                .build();

        n.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n);
            Log.d(TAG, "show: notification posted for index " + index);
        } catch (SecurityException e) {
            Log.e(TAG, "show: POST_NOTIFICATIONS denied", e);
        }
    }

    @NonNull
    private static RemoteViews getRemoteViews(Context context, int index) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_carousel);

        RemoteViews item = new RemoteViews(context.getPackageName(), R.layout.notification_carousel_item);
        item.setImageViewResource(R.id.item_image,  IMAGES[index]);
        item.setTextViewText(R.id.item_title,       ITEMS[index][0] + " -> Page: " + (index + 1));
        item.setTextViewText(R.id.item_description, ITEMS[index][1]);
        views.addView(R.id.carousel_flipper, item);

        views.setInt(R.id.carousel_flipper, "setDisplayedChild", 0);
        return views;
    }

//    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
//    private static void scheduleFlip(Context context, int currentIndex) {
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        long triggerAt  = SystemClock.elapsedRealtime() + INTERVAL_MS;
//
//        Intent intent = new Intent(context, CarouselReceiver.class);
//        intent.setAction(ACTION_FLIP);
//        PendingIntent pi = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
//            am.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, 500L, pi);
//            Log.d(TAG, "scheduleFlip: window alarm scheduled");
//        } else {
//            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi);
//            Log.d(TAG, "scheduleFlip: exact alarm scheduled at +" + INTERVAL_MS + "ms");
//        }
//    }
//
//    public static void cancelAlarm(Context context) {
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent   = new Intent(context, CarouselReceiver.class);
//        intent.setAction(ACTION_FLIP);
//        PendingIntent pi = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent,
//                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
//        if (pi != null) {
//            am.cancel(pi);
//            Log.d(TAG, "cancelAlarm: alarm cancelled");
//        } else {
//            Log.w(TAG, "cancelAlarm: no pending intent found");
//        }
//    }

    private static void createChannel(Context context) {
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Carousel", NotificationManager.IMPORTANCE_DEFAULT);
        ch.enableVibration(false);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);
        Log.d(TAG, "createChannel: channel created");
    }
}