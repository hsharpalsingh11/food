package com.example.singh.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.singh.myapplication.Model.User;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignUp extends AppCompatActivity
{

    Button signUp;
    UserSessionManager session;

    MaterialEditText edtPhone,edtName,edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        session = new UserSessionManager(getApplicationContext());

        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtName = (MaterialEditText)findViewById(R.id.edtName);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        signUp = (Button)findViewById(R.id.signUp
        );

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user= database.getReference("User");

        signUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                mDialog.setMessage("Please waiting...");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child(edtPhone.getText().toString()).exists())
                        {
                            mDialog.dismiss();
                            Toast.makeText(SignUp.this, "Already Exists", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            mDialog.dismiss();
                            User user = new User(edtName.getText().toString(),edtPassword.getText().toString(),edtPhone.getText().toString());
                            table_user.child(edtPhone.getText().toString()).setValue(user);
                            Toast.makeText(SignUp.this, "Signed Up Successfully", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(SignUp.this, ""+user.getPhone().toString(), Toast.LENGTH_SHORT).show();
                            String phone = user.getPhone().toString();
                            Common.currentUser = user;
                            session.createLoginSession(edtName.getText().toString(), phone);



                            Intent homeIntent = new Intent(SignUp.this,Home.class);
                            homeIntent.putExtra("STRING_I_NEED", phone);
                            startActivity(homeIntent);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }
}
