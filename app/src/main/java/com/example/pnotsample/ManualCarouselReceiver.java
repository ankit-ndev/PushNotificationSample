package com.example.pnotsample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ManualCarouselReceiver extends BroadcastReceiver {
    private static final String TAG = "PNotSample_Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if ("ACTION_DISMISS".equals(action)) {
            Log.d(TAG, "Dismiss button clicked. Cancelling notification ID: " + MyFirebaseMessagingService.NOTIFICATION_ID);
            MyFirebaseMessagingService.isAutoRotating = false;
            if (manager != null) {
                manager.cancel(MyFirebaseMessagingService.NOTIFICATION_ID);
            }
            return;
        }
        int page = intent.getIntExtra("next_page", 1);
        Log.d(TAG, "Button clicked! Requested page: " + page);


        if (page > 3) page = 1;
        if (page < 1) page = 3;

        // Call the static method to update the existing notification
        MyFirebaseMessagingService.showPagerNotification(context, page);
    }
}