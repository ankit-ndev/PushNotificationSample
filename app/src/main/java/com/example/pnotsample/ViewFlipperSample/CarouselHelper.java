package com.example.pnotsample.ViewFlipperSample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.pnotsample.R;

public class CarouselHelper {

    private static final String TAG            = "PNotSample_Carousel";
    private static final String CHANNEL_ID     = "carousel_channel";
    public  static final int    NOTIFICATION_ID = 101;
    public  static final String ACTION_DISMISS  = "com.example.pnotsample.CAROUSEL_DISMISS";
    private static final long   INTERVAL_MS     = 2600L;

    // Single Handler reference — required so we can cancel it cleanly
    private static Handler  carouselHandler;
    private static Runnable carouselRunnable;

    static final String[][] ITEMS = {
            {"Pizza Sale - 40% Off", "Today only on all large pizzas"},
            {"Burger Deal - 30% Off", "All burgers discounted till midnight"},
            {"Buy 1 Get 1 Pizza", "Free pizza on every large order"},
            {"Family Combo - 25% Off", "Pizza, garlic bread and drinks combo"},
            {"Pasta Special - 20% Off", "Creamy and spicy pasta options"},
            {"Cheese Burst - 35% Off", "Extra cheese pizzas at lower price"},
            {"Late Night Pizza - 45% Off", "Offer valid from 11 PM to 2 AM"},
            {"Student Offer - 50% Off", "Show student ID to claim deal"}
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
        stopFlipping();
        show(context.getApplicationContext(), 0);
        scheduleFlip(context.getApplicationContext(), 0);
    }

    public static void stop(Context context) {
        Log.d(TAG, "stop: stopping carousel");
        stopFlipping();
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    private static void scheduleFlip(Context context, int currentIndex) {
        carouselHandler  = new Handler(Looper.getMainLooper());
        carouselRunnable = new Runnable() {
            int index = currentIndex;

            @Override
            public void run() {
                index = (index + 1) % ITEMS.length;
                Log.d(TAG, "flip: handler fired → index=" + index);
                show(context, index);
                carouselHandler.postDelayed(this, INTERVAL_MS);
            }
        };

        carouselHandler.postDelayed(carouselRunnable, INTERVAL_MS);
        Log.d(TAG, "scheduleFlip: first flip in " + INTERVAL_MS + "ms");
    }

    private static void stopFlipping() {
        if (carouselHandler != null && carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
            Log.d(TAG, "stopFlipping: callbacks removed");
        }
        carouselHandler  = null;
        carouselRunnable = null;
    }

    private static void show(Context context, int index) {
        Log.d(TAG, "show: index=" + index);

        Intent dismissIntent = new Intent(context, CarouselReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent dismissPi = PendingIntent.getBroadcast(context, 0, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomBigContentView(getRemoteViews(context, index))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setDeleteIntent(dismissPi)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        n.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n);
            Log.d(TAG, "show: notification posted ✓");
        } catch (SecurityException e) {
            Log.e(TAG, "show: POST_NOTIFICATIONS denied", e);
            stopFlipping();
        }
    }

    @NonNull
    private static RemoteViews getRemoteViews(Context context, int index) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_carousel);

        RemoteViews item = new RemoteViews(context.getPackageName(), R.layout.notification_carousel_item);
        item.setImageViewResource(R.id.item_image,  IMAGES[index]);
        item.setTextViewText(R.id.item_description, ITEMS[index][0] + " : " + ITEMS[index][1]);
        views.addView(R.id.carousel_flipper, item);

        views.setInt(R.id.carousel_flipper, "setDisplayedChild", 0);
        return views;
    }

    private static void createChannel(Context context) {
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Carousel", NotificationManager.IMPORTANCE_DEFAULT);
        ch.enableVibration(false);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);
        Log.d(TAG, "createChannel: done");
    }
}