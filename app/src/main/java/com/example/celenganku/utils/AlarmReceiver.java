package com.example.celenganku.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.celenganku.R;  // Add this import

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_DAILY_REMINDER = "com.example.celenganku.ACTION_DAILY_REMINDER";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                (intent.getAction().equals(ACTION_DAILY_REMINDER) ||
                        intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))) {

            // Use context.getString() with your R.string resources
            String title = context.getString(R.string.notification_title);
            String message = context.getString(R.string.notification_message);

            NotifHelper.showNotification(context, NOTIFICATION_ID, title, message);
        }
    }
}