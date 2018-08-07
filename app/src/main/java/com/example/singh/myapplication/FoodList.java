package com.example.singh.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Database.Database;
import com.example.singh.myapplication.Interface.ItemClickListener;
import com.example.singh.myapplication.Model.Food;
import com.example.singh.myapplication.Model.Order;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.example.singh.myapplication.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FoodList extends AppCompatActivity
{

    RecyclerView recyclerViewFood;
    RecyclerView.LayoutManager layoutManagerFood;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference foodList;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    String categoryId = "";
    String newString = "";
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapterFood;
    Database localDb;
    UserSessionManager session;

    //create target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if(ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    //Search

    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList =  new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        foodList = firebaseDatabase.getReference("Food");

        //Init Facebook
        callbackManager =  CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //local db
        localDb = new Database(this);
        session = new UserSessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();

        // name
        newString = user.get(UserSessionManager.KEY_PhONE);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById( R.id.swipe_layout );
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent() !=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null)
                {
                    if(Common.isConnectedToInternet( getBaseContext() ))
                        loadFoodList(categoryId);
                    else
                    {
                        Toast.makeText( FoodList.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }

            }
        } );

        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                if(getIntent() !=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null)
                {
                    if(Common.isConnectedToInternet( getBaseContext() ))
                        loadFoodList(categoryId);
                    else
                    {
                        Toast.makeText( FoodList.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }


                //search
                materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your food");
                loadSuggest();
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //when user type text, it will chnage accordingly
                        List<String> suggest = new ArrayList<>();
                        for(String search:suggestList)
                        {
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                            {
                                suggest.add(search);
                            }
                            materialSearchBar.setLastSuggestions(suggest);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //when search bar closes
                        //Restore original suggest adapter
                        if (!enabled)
                            recyclerViewFood.setAdapter(adapterFood);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text)
                    {
                        //when serch finish show result of search adapter
                        startSearch(text);


                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });

            }
        } );

        recyclerViewFood = (RecyclerView)findViewById(R.id.recyclerViewFoods);
        recyclerViewFood.setHasFixedSize(true);
        layoutManagerFood = new LinearLayoutManager(this);
        recyclerViewFood.setLayoutManager(layoutManagerFood);

        //get Intent


    }


    private void startSearch(CharSequence text)
    {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("name").equalTo(text.toString())
        ) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position)
            {
                viewHolder.food_name.setText(model.getName());
                Toast.makeText(FoodList.this, ""+model.getPrice(), Toast.LENGTH_SHORT).show();
                viewHolder.food_price.setText(String.format("$ %s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                viewHolder.food_name.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(FoodList.this, "fooood name", Toast.LENGTH_SHORT).show();
                    }
                });




                final Food local = model;

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onCLick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }
        };
        recyclerViewFood.setAdapter(searchAdapter);
    }

    private void loadSuggest()
    {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());
                        }

                        materialSearchBar.setLastSuggestions(suggestList);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(adapterFood!=null)
          // adapterFood.startListening();
    }

    private void loadFoodList(String categoryId)
    {
        adapterFood = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder,final Food model,final int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                //click to share

                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                            @Override
                            public void onSuccess(Sharer.Result result)     {
                                Toast.makeText(FoodList.this, "Successful", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancel() {
                                Toast.makeText(FoodList.this, "Canceled", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onError(FacebookException error) {
                                Toast.makeText(FoodList.this, ""+error.getMessage().toString(), Toast.LENGTH_SHORT).show();

                            }
                        });
                        Toast.makeText(FoodList.this, "fooood name", Toast.LENGTH_SHORT).show();

                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //Quick Cart

                viewHolder.btn_quick_cart.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Database(getBaseContext()).addToCart(new Order(
                                adapterFood.getRef( position ).getKey(),
                                model.getName(),
                               "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()
                        ));

                        Toast.makeText(FoodList.this, " Added TO cart", Toast.LENGTH_SHORT).show();
                    }
                    } );
                //Add favourites
             /* if(localDb.isFavourite(adapterFood.getRef(position).getKey(),newString))
                  viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);


                //click to change state of favourite
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Toast.makeText(FoodList.this, "clicked", Toast.LENGTH_SHORT).show();
                        if(!localDb.isFavourite(adapterFood.getRef(position).getKey(),newString))
                        {
                            localDb.addToFavourites(adapterFood.getRef(position).getKey(),newString);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" is added to favourites", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDb.removeFromFavourites(adapterFood.getRef(position).getKey(),newString);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" is removed from favourites", Toast.LENGTH_SHORT).show();
                        }
                    }
                }); */
                final Food local = model;

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onCLick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapterFood.getRef(position).getKey());
                        startActivity(foodDetail);
                        }
                });
            }


        };

        //setAdapter
        recyclerViewFood.setAdapter(adapterFood);
        swipeRefreshLayout.setRefreshing( false );
    }
}
