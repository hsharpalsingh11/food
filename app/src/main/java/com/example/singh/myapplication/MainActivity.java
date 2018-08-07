package com.example.singh.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Model.User;
import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnSignUp;
    TextView txtSlogan;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(FacebookSdk.getApplicationContext());

        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        btnSignUp = (Button)findViewById(R.id.btnSignUp);

        txtSlogan = (TextView)findViewById(R.id.txtSlogan);

        Paper.init( this );

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent signUp= new Intent(MainActivity.this, SignUp.class);
                startActivity(signUp);

            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn= new Intent(MainActivity.this, SignIn.class);
                startActivity(signIn);

            }
        });
        String user = Paper.book().read( Common.USER_KEY );
        String pwd = Paper.book().read( Common.PWD_KEY );
        if(user != null && pwd != null)
        {
            if(!user.isEmpty() && !pwd.isEmpty())
                login(user,pwd);
        }

        printKeyHash();
    }

    private void printKeyHash() {
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo( "com.example.singh.myapplication", PackageManager.GET_SIGNATURES );
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void login(final String phone, final String pwd) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user= database.getReference("User");

        if (Common.isConnectedToInternet( getBaseContext() )) {


            final ProgressDialog mDialog = new ProgressDialog( MainActivity.this );
            mDialog.setMessage( "Please waiting..." );
            mDialog.show();

            table_user.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Check if user not exist in database
                    if (dataSnapshot.child( phone ).exists()) {
                        //Get User Information
                        mDialog.dismiss();
                        DatabaseReference table_user_user = table_user.child(phone );

                        User user = dataSnapshot.child(phone ).getValue( User.class );
                        Boolean loggedIn;
                        String phoneNumber = dataSnapshot.child(phone ).child( "phone" ).getValue( String.class );
                        String name = dataSnapshot.child( phone ).child( "name" ).getValue( String.class );


                        //editor.putString("key_phone", phoneNumber);
                        //editor.putString("key_name", name);

                        //session.createUserLoginSession(phoneNumber, name);
                        // Toast.makeText(SignIn.this, "  "+userName, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(SignIn.this, " db "+dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(SignIn.this, "Edt text"+edtPhone.getText().toString(), Toast.LENGTH_SHORT).show();
                        user.setPhone( phoneNumber );
                        user.setBalance(0.0);

                        if (user.getPassword().equals( pwd )) {
                            //session.createLoginSession( name, phoneNumber );
                            Intent homeIntent = new Intent( MainActivity.this, Home.class );
                            homeIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );

                            // Add new Flag to start new Activity
                            homeIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                            Common.currentUser = user;
                            //Toast.makeText(SignIn.this, " DB "+phoneNumber, Toast.LENGTH_SHORT).show();
                            String phone = phoneNumber;
                            homeIntent.putExtra( "STRING_I_NEED", phone );

                            startActivity( homeIntent );
                            finish();
                        } else {
                            Toast.makeText( MainActivity.this, "Sign In Failed", Toast.LENGTH_SHORT ).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText( MainActivity.this, "User not exist in Database", Toast.LENGTH_SHORT ).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            } );

        }

        else
        {
            Toast.makeText( MainActivity.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
            return;
        }
    }

}
