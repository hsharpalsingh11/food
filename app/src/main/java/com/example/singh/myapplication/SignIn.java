package com.example.singh.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Model.User;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference table_user;
    MaterialEditText edtPhone,edtPassword;
    Button btnSignIn;
    TextView txtForgotPwd;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseReference requests;
    // User Session Manager Class
    UserSessionManager session;
    public static final String mypreference = "mypref";
    public static final String key_name = "key_name";
    public static final String key_phone = "key_phone";
    CheckBox cbkRemember;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        session = new UserSessionManager(getApplicationContext());
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        btnSignIn = (Button)findViewById(R.id.btnLogin);
        cbkRemember = (CheckBox) findViewById( R.id.ckbRemember);
        txtForgotPwd = (TextView) findViewById( R.id.txtForgotPwd );
        Paper.init( this );

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user= database.getReference("User");

        txtForgotPwd.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPwdDialog();
            }
        } );

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet( getBaseContext() )) {
                    if(cbkRemember.isChecked())
                    {
                        Paper.book().write( Common.USER_KEY,edtPhone.getText().toString() );
                        Paper.book().write( Common.PWD_KEY,edtPassword.getText().toString() );
                    }


                    final ProgressDialog mDialog = new ProgressDialog( SignIn.this );
                    mDialog.setMessage( "Please waiting..." );
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if user not exist in database
                            if (dataSnapshot.child( edtPhone.getText().toString() ).exists()) {
                                //Get User Information
                                mDialog.dismiss();
                                DatabaseReference table_user_user = table_user.child( edtPhone.getText().toString() );

                                User user = dataSnapshot.child( edtPhone.getText().toString() ).getValue( User.class );
                                Boolean loggedIn;
                                String phoneNumber = dataSnapshot.child( edtPhone.getText().toString() ).child( "phone" ).getValue( String.class );
                                String name = dataSnapshot.child( edtPhone.getText().toString() ).child( "name" ).getValue( String.class );
                                String password = dataSnapshot.child( edtPhone.getText().toString() ).child( "password" ).getValue( String.class );
                                Long balance = Long.valueOf(dataSnapshot.child( edtPhone.getText().toString() ).child( "balance" ).getValue( Long.class ));

                                //editor.putString("key_phone", phoneNumber);
                                //editor.putString("key_name", name);

                                //session.createUserLoginSession(phoneNumber, name);
                                // Toast.makeText(SignIn.this, "  "+userName, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(SignIn.this, " db "+dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                                //Toast.makeText(SignIn.this, "Edt text"+edtPhone.getText().toString(), Toast.LENGTH_SHORT).show();
                                user.setPhone( phoneNumber );
                               user.setBalance(Double.parseDouble(String.valueOf(balance)));


                                if (user.getPassword().equals( edtPassword.getText().toString() )) {
                                    session.createLoginSession( name, phoneNumber,password );
                                    Intent homeIntent = new Intent( SignIn.this, Home.class );
                                    homeIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );

                                    // Add new Flag to start new Activity
                                    homeIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                    Common.currentUser = user;
                                    //Toast.makeText(SignIn.this, " DB "+phoneNumber, Toast.LENGTH_SHORT).show();
                                    String phone = phoneNumber;
                                    homeIntent.putExtra( "STRING_I_NEED", phone );

                                    startActivity( homeIntent );
                                    finish();

                                    table_user.removeEventListener(this);
                                } else {
                                    Toast.makeText( SignIn.this, "Sign In Failed", Toast.LENGTH_SHORT ).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText( SignIn.this, "User not exist in Database", Toast.LENGTH_SHORT ).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }


                    } );

                }

                else
                {
                    Toast.makeText( SignIn.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        });
    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Forgot Password" );
        builder.setMessage( "Enter your secure code" );

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.forgot_password,null);

        builder.setView(forgot_view);
        builder.setIcon( R.drawable.ic_security_black_24dp );
        final MaterialEditText edtPhone = (MaterialEditText) forgot_view.findViewById( R.id.edtPhone );
        final MaterialEditText edtSecureCode = (MaterialEditText) forgot_view.findViewById( R.id.edtSecureCode );

        builder.setPositiveButton( "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                table_user.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child( edtPhone.getText().toString() ).getValue(User.class);

                        if(user.getSecureCode().equals(edtSecureCode.getText().toString()))
                            Toast.makeText( SignIn.this, "Your Password:"+user.getPassword(), Toast.LENGTH_LONG ).show();
                        else
                            Toast.makeText( SignIn.this, "Wrong secure code!", Toast.LENGTH_SHORT ).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );
            }
        } );
        builder.setNegativeButton( "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        } );
        builder.show();
    }
}
