package com.example.singh.myapplication.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.singh.myapplication.Helper.NotificationHelper;
import com.example.singh.myapplication.MainActivity;
import com.example.singh.myapplication.Model.Notification;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.example.singh.myapplication.OrderStatus;
import com.example.singh.myapplication.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService
{
    UserSessionManager session;
    String phone;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived( remoteMessage );
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();

        phone = user.get(UserSessionManager.KEY_PhONE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        sendNotificationAPI26(remoteMessage);
        else
            sendNotification(remoteMessage);

    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage)
    {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = notification.getTitle();
        String content = notification.getBody();

        Intent intent = new Intent(this, OrderStatus.class);
        Toast.makeText(this, "ghkjlkghf", Toast.LENGTH_SHORT).show();
        intent.putExtra("STRING_I_NEED",phone);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationhelper = new NotificationHelper(this);
        android.app.Notification.Builder builder = notificationhelper
                .getFoodChannelNotification(title,content,pendingIntent,defaultSoundUri);

        notificationhelper.getManager().notify(new Random().nextInt(),builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage)
    {


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Intent intent = new Intent(this, OrderStatus.class);
        Toast.makeText(this, "ghkjlkghf", Toast.LENGTH_SHORT).show();
        intent.putExtra("STRING_I_NEED",phone);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon( R.mipmap.ic_launcher_round)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());

    }
}
