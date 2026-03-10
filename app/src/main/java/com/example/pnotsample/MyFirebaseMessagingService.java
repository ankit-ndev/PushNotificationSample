package com.example.pnotsample;

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

import com.example.pnotsample.ViewFlipperSample.CarouselHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PNotSample_FCM";
    public static final int NOTIFICATION_ID = 100;
    private static final int AUTO_ROTATE_DELAY = 4000;
    public static boolean isAutoRotating = true;
    private static long remainingSeconds = 5000;
    private static final Handler timerHandler = new Handler(Looper.getMainLooper());
    private static Runnable timerRunnable;

    private static final int[] IMAGES = {
            R.drawable.pizza_1,
            R.drawable.pizza_2,
            R.drawable.pizza_3
    };
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Important: Log this so you can copy it from Logcat for testing
        Log.d(TAG, "NEW_TOKEN: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        CarouselHelper.stop(getApplicationContext());
        CarouselHelper.start(getApplicationContext());


//        showPagerNotification(this, 1);
//        startTimer(this);
    }

    /**
     * Build and show/update the Pager notification
     */
    public static void showPagerNotification(Context context, int page) {
        Log.d(TAG, "Updating Notification Pager to Page: " + page);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_carousel_manual);

        // 1. Update UI Elements
        remoteViews.setTextViewText(R.id.carousel_content, "Offer " + page + " of 3");
        remoteViews.setImageViewResource(R.id.carousel_image, IMAGES[page - 1]);

        // 2. Setup Navigation Button Click Listeners
        remoteViews.setOnClickPendingIntent(R.id.btn_next, getNavigationIntent(context, page + 1, 1));
        remoteViews.setOnClickPendingIntent(R.id.btn_prev, getNavigationIntent(context, page - 1, 2));

        Intent dismissIntent = new Intent(context, ManualCarouselReceiver.class);
        dismissIntent.setAction("ACTION_DISMISS");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        remoteViews.setOnClickPendingIntent(R.id.btn_dismiss, dismissPendingIntent);
        // 3. Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomBigContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }

        if (isAutoRotating) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAutoRotating) {
                    int nextPage = (page % 3) + 1;
                    showPagerNotification(context, nextPage);
                }
            }, AUTO_ROTATE_DELAY);
        }
    }


    public static void startTimer(Context context) {
        // Reset the timer if it was already running
        timerHandler.removeCallbacks(timerRunnable);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (remainingSeconds >= 0) {
                    updateTimerNotification(context, remainingSeconds);
                    remainingSeconds--;
                    // Schedule next update in 1 second
                    timerHandler.postDelayed(this, 1000);
                } else {
                    // Timer Finished!
                    onTimerFinished(context);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private static void updateTimerNotification(Context context, long secondsLeft) {
        // Format the time: HH:MM:SS
        long h = secondsLeft / 3600;
        long m = (secondsLeft % 3600) / 60;
        long s = secondsLeft % 60;
        String timeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_timer);

        // Use a regular TextView instead of Chronometer for custom formatting
        remoteViews.setTextViewText(R.id.notification_timer, timeText);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomBigContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOnlyAlertOnce(true) // Crucial: prevents vibrating every second
                .setOngoing(true); // User can't swipe it away while running

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private static void onTimerFinished(Context context) {
        Log.d(TAG, "Timer reached zero!");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Either cancel it or update it to show "Finished!"
        if (manager != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Offer Expired")
                    .setContentText("You missed the pizza deal!")
                    .setOngoing(false); // Let them swipe it away now
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    // Don't forget to stop the timer if the app is destroyed or notification dismissed
    public static void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private static PendingIntent getNavigationIntent(Context context, int nextPage, int requestCode) {
        Intent intent = new Intent(context, ManualCarouselReceiver.class);
        intent.putExtra("next_page", nextPage);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}