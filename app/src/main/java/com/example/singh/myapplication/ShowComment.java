package com.example.singh.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Model.Food;
import com.example.singh.myapplication.Model.Rating;
import com.example.singh.myapplication.ViewHolder.FoodViewHolder;
import com.example.singh.myapplication.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import okhttp3.internal.http.CallServerInterceptor;

public class ShowComment extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout swipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating,ShowCommentViewHolder> adapter;
String foodId="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_show_comment );
        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView)findViewById( R.id.recyclerComment );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById( R.id.swipe_layout_show_comment );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                if(getIntent() != null)
                    foodId = getIntent().getStringExtra( Common.INTENT_FOOD_ID );
                if(!foodId.isEmpty()  && foodId != null)
                {
                    Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);


                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,
                            R.layout.show_comment_layout,
                            ShowCommentViewHolder.class,
                            ratingTbl.orderByChild("foodId").equalTo(foodId)) {
                        @Override
                        protected void populateViewHolder(final ShowCommentViewHolder holder,final Rating model,final int position) {

                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserPhone.setText(model.getUserPhone());
                        }


                    };
                    
                    loadComment(foodId);
                }
            }
        } );

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);

                if(getIntent() != null)
                    foodId = getIntent().getStringExtra( Common.INTENT_FOOD_ID );
                if(!foodId.isEmpty()  && foodId != null)
                {
                    Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);



                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,
                            R.layout.show_comment_layout,
                            ShowCommentViewHolder.class,
                            ratingTbl.orderByChild("foodId").equalTo(foodId)) {
                        @Override
                        protected void populateViewHolder(ShowCommentViewHolder holder, Rating model, int position) {

                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserPhone.setText(model.getUserPhone());
                        }
                    };

                    loadComment(foodId);
                }
            }
        });
    }

    private void loadComment(String foodId) {
       // adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
}
