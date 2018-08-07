package com.example.singh.myapplication.Helper;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.singh.myapplication.BuildConfig;
import com.example.singh.myapplication.Model.Notification;
import com.example.singh.myapplication.R;

public class NotificationHelper extends ContextWrapper
{
    private static final String CHANEL_ID = "com.example.singh.myapplication";
    private static final String CHANEL_NAME = "Food";
    NotificationManager manager;
    public NotificationHelper(Context base)
    {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
        
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel()
    {
        NotificationChannel channel = new NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager()
    {
        if (manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getFoodChannelNotification(String title, String body, PendingIntent contentintent,
                                                                       Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(), CHANEL_ID)
                .setContentIntent(contentintent)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
