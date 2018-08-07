package com.example.singh.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Database.Database;
import com.example.singh.myapplication.Model.Food;
import com.example.singh.myapplication.Model.Order;
import com.example.singh.myapplication.Model.Rating;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;
import java.util.HashMap;

public class
FoodDetail extends AppCompatActivity implements RatingDialogListener
{
    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    RatingBar ratingBar;
    UserSessionManager session;
    Button btnShowComment;

    ElegantNumberButton numberButton;

    String foodId="";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference foods;
    DatabaseReference ratingTbl;

    Food currentFood;
    String newString;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //FIrebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        foods = firebaseDatabase.getReference("Food");
        ratingTbl = firebaseDatabase.getReference("Rating");
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        newString = user.get(UserSessionManager.KEY_PhONE);
        //Init view
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (CounterFab) findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ));

                Toast.makeText(FoodDetail.this, " Added TO cart", Toast.LENGTH_SHORT).show();
            }
        });
        btnCart.setCount( new Database( this ).getCountCart() );
        food_description=(TextView)findViewById(R.id.food_description);
        food_name=(TextView)findViewById(R.id.food_name);
        food_price=(TextView)findViewById(R.id.food_price);
        food_image=(ImageView)findViewById(R.id.img_food);
        btnShowComment = (Button) findViewById( R.id.btnShowComment );
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        btnShowComment.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(  FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);
            }
        } );
        //Get Food Id from Intent
        if(getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if(!foodId.isEmpty())
        {
            if(Common.isConnectedToInternet( getBaseContext() )) {
                getDetailFood( foodId );
                getRatingFood(foodId);
            }
            else
            {
                Toast.makeText( FoodDetail.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
                return;
            }
        }
    }

    private void getRatingFood(String foodId)
    {
        com.google.firebase.database.Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener(){
           int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
           {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt(item.getRateValue());
                    count++;
                }
                if(count != 0)
                {

                    float average = sum / count;
                    ratingBar.setRating(average);
                }
           }
           @Override
            public void onCancelled(DatabaseError databaseError)
           {

           }
        });
    }

    private void showRatingDialog()     {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions( Arrays.asList( "Very Bad","Not Good","Quite Ok","Very Good","Excellent" ))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here... ")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();

    }

    private void getDetailFood(String foodId)
    {
        foods.child(foodId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                currentFood = dataSnapshot.getValue(Food.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        final Rating rating = new Rating(newString
                ,foodId
                ,String.valueOf(value)
                ,comments);

        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText( FoodDetail.this,"Thank you for submit rating !!!", Toast.LENGTH_SHORT ).show();
                    }
                } );
        /*
        ratingTbl.child( newString ).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot)
           {
                if(dataSnapshot.child(newString).exists())
                {
                    ratingTbl.child(newString).removeValue();
                    ratingTbl.child(newString).setValue(rating);
                }
                else
                {
                    ratingTbl.child(newString).setValue(rating);
                }
               Toast.makeText( FoodDetail.this,"Thank you for submit rating !!!", Toast.LENGTH_SHORT ).show();
           }
           @Override
            public void onCancelled(DatabaseError databaseError)
           {

           }
        });*/
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
