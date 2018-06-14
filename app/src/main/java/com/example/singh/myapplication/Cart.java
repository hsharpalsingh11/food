package com.example.singh.myapplication;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.singh.myapplication.Database.Database;
import com.example.singh.myapplication.Model.Order;
import com.example.singh.myapplication.Model.Request;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.example.singh.myapplication.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity
{
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    String newString= "";
    String namee= "";
    UserSessionManager session;


    TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
       // newString = getIntent().getExtras().getString("STRING_I_NEED");
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();

        // name
        namee = user.get(UserSessionManager.KEY_NAME);
        newString = user.get(UserSessionManager.KEY_PhONE);


        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (Button)findViewById(R.id.btnPlaceOrder);

        loadListFood();

    }

    private void loadListFood()
    {
        cart = new Database(this).getCarts();
        cartAdapter = new CartAdapter(cart,this);
        recyclerView.setAdapter(cartAdapter);

        //Calculate total Price
        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
        btnPlace = (Button)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showAlertDialog();

            }
        });
    }

    private void showAlertDialog()
    {
        AlertDialog.Builder alertDalog = new AlertDialog.Builder(Cart.this);
        alertDalog.setTitle("One More Step..");
        alertDalog.setMessage("Enter Your Address");

        final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(lp);
        alertDalog.setView(edtAddress);
        alertDalog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDalog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create new request
                Request request = new Request(
                        newString,
                        edtAddress.getText().toString(),

                        txtTotalPrice.getText().toString(),
                        namee,

                        cart
                        );

                //Submit to firebase
                requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);
                //Delete Cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank You, Order Placed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        alertDalog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDalog.show();
    }
}
