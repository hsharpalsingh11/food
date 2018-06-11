package com.example.singh.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.singh.myapplication.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignIn extends AppCompatActivity {

    MaterialEditText edtPhone,edtPassword;
    Button btnSignIn;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        btnSignIn = (Button)findViewById(R.id.btnLogin);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user= database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Please waiting...");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        //Check if user not exist in database
                        if(dataSnapshot.child(edtPhone.getText().toString()).exists())
                        {
                            //Get User Information
                            mDialog.dismiss();
                            DatabaseReference table_user_user= table_user.child(edtPhone.getText().toString());

                            User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                            String phoneNumber = dataSnapshot.child(edtPhone.getText().toString()).child("phone").getValue(String.class);

                           // Toast.makeText(SignIn.this, "  "+userName, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(SignIn.this, " db "+dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(SignIn.this, "Edt text"+edtPhone.getText().toString(), Toast.LENGTH_SHORT).show();
                            user.setPhone(phoneNumber);


                            if (user.getPassword().equals(edtPassword.getText().toString()))
                            {
                                Intent homeIntent = new Intent(SignIn.this,Home.class);
                                Common.currentUser = user;
                                //Toast.makeText(SignIn.this, " DB "+phoneNumber, Toast.LENGTH_SHORT).show();
                                String phone = phoneNumber;
                                homeIntent.putExtra("STRING_I_NEED", phone);

                                startActivity(homeIntent);
                                finish();
                            } else {
                                Toast.makeText(SignIn.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(SignIn.this, "User not exist in Database", Toast.LENGTH_SHORT).show();
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
