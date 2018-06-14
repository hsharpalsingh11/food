package com.example.singh.myapplication.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.singh.myapplication.Common;
import com.example.singh.myapplication.Model.Request;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.example.singh.myapplication.OrderStatus;
import com.example.singh.myapplication.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ListenOrder extends Service implements ChildEventListener
{
    FirebaseDatabase db;
    DatabaseReference requests;
    String newString,name;
    UserSessionManager session;

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";
    public ListenOrder()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        return  null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
        session = new UserSessionManager(getApplicationContext());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        requests.addChildEventListener(this);
        HashMap<String, String> user = session.getUserDetails();

        // name
        name = user.get(UserSessionManager.KEY_NAME);
        newString = user.get(UserSessionManager.KEY_PhONE);
        //newString = intent.getExtras().getString("STRING_I_NEED");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s)
    {

    }

    private void showNotification(String key, Request request)
    {
        Intent intent = new Intent(getBaseContext(), OrderStatus.class);
        intent.putExtra("STRING_I_NEED", newString);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

       /* builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("singhharpal")
                .setContentInfo("Your Order was Updated")
                .setContentText("Order #"+key+" status was updated to "+ Common.convertCodeToStatus(request.getStatus()))
                .setContentIntent(contentIntent)
                .setContentInfo("Info")
                .setSmallIcon(R.drawable.ic_shopping_cart_black_24dp);*/

        NotificationManager notificationManager= (NotificationManager)getBaseContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Your Order Was Updated")
                .setContentText("Order #"+key+" status was updated to "+ Common.convertCodeToStatus(request.getStatus()));

        notificationManager.notify(1,builder.build());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s)
    {
        //Trigger here
        Request request = dataSnapshot.getValue(Request.class);
        showNotification(dataSnapshot.getKey(),request);

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
