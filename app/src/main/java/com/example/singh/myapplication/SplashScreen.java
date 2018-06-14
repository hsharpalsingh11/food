package com.example.singh.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.singh.myapplication.Model.UserSessionManager;

import java.util.HashMap;

public class SplashScreen extends AppCompatActivity
{
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    UserSessionManager session;
    public static final String mypreference = "mypref";
    public static final String key_name = "key_name";
    public static final String key_phone = "key_phone";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Session class instance
        session = new UserSessionManager(getApplicationContext());

        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(UserSessionManager.KEY_NAME);

        // email
        String phone = user.get(UserSessionManager.KEY_PhONE);
        //startActivity(new Intent(SplashScreen.this, MainActivity.class));
    }
}
