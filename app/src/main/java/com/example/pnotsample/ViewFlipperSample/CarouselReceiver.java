package com.example.pnotsample.ViewFlipperSample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class CarouselReceiver extends BroadcastReceiver {

    private static final String TAG = "PNotSample_Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        Log.d(TAG, "onReceive: action=" + intent.getAction());

        switch (intent.getAction()) {
            case CarouselHelper.ACTION_FLIP:
                CarouselHelper.onAlarmFired(context);
                break;
            case CarouselHelper.ACTION_DISMISS:
                CarouselHelper.cancelAlarm(context);
                CarouselState.markStopped(context);
                Log.d(TAG, "onReceive: dismissed, alarm cancelled");
                break;
        }
    }
}
