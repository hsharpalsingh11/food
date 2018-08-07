package com.example.singh.myapplication.Service;

import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Model.Token;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    UserSessionManager session;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();

        // name
        String phone = user.get(UserSessionManager.KEY_PhONE);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(tokenRefreshed,false);
        tokens.child(phone).setValue(token);

    }
}
