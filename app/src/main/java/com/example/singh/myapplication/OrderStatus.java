package com.example.singh.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.singh.myapplication.Model.Request;
import com.example.singh.myapplication.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OrderStatus extends AppCompatActivity
{
    public RecyclerView recyclerViewStatus;
    public RecyclerView.LayoutManager layoutManagerStatus;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;


    String newString;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //FIrebase
        database = FirebaseDatabase.getInstance();
        requests= database.getReference("Requests");
        newString = getIntent().getExtras().getString("STRING_I_NEED");


        recyclerViewStatus = (RecyclerView)findViewById(R.id.listOrders);
        recyclerViewStatus.setHasFixedSize(true);
        layoutManagerStatus = new LinearLayoutManager(this) ;
        recyclerViewStatus.setLayoutManager(layoutManagerStatus);
        loadOrders(Common.currentUser.getPhone());
        
    }

    private void loadOrders(String phone)
    {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("phone").equalTo(newString)
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {

                //viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                String orderId = String.valueOf(adapter.getRef(position).getKey());

                Toast.makeText(OrderStatus.this, ""+adapter.getRef(position).getKey().toString(), Toast.LENGTH_SHORT).show();
                viewHolder.txtOrderId.setText(orderId);
                viewHolder.txtOrderStatus.setText(convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(newString);
            }
        };
        recyclerViewStatus.setAdapter(adapter);
    }

    private String convertCodeToStatus(String status)
    {
        if (status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On My Way";
        else return "Shipped";
    }
}
