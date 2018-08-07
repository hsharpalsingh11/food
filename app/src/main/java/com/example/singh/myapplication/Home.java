package com.example.singh.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Database.Database;
import com.example.singh.myapplication.Interface.ItemClickListener;
import com.example.singh.myapplication.Model.Banner;
import com.example.singh.myapplication.Model.Category;
import com.example.singh.myapplication.Model.Token;
import com.example.singh.myapplication.Model.UserSessionManager;
//import com.example.singh.myapplication.ViewHolder.MenuViewHolder;
import com.example.singh.myapplication.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName,txtPhone;
    String newString,passwordString,nameString;
    UserSessionManager session;
    CounterFab fab;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    //Slider
    HashMap<String,String> image_list;
    SliderLayout mSlider;

    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        session = new UserSessionManager(getApplicationContext());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
                );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(Common.isConnectedToInternet( getBaseContext() ))
                    loadMenu();
                else
                {
                    Toast.makeText( getBaseContext(), "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
                    return;
                }
                }
        } );

        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet( getBaseContext() ))
                    loadMenu();
                else
                {
                    Toast.makeText( getBaseContext(), "Please Check Your Connection !!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        } );
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
       // newString = getIntent().getExtras().getString("STRING_I_NEED");
        //Toast.makeText(this, "phone "+newString, Toast.LENGTH_SHORT).show();
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(UserSessionManager.KEY_NAME);
        nameString = user.get(UserSessionManager.KEY_NAME);
        newString = user.get(UserSessionManager.KEY_PhONE);
        passwordString = user.get(UserSessionManager.KEY_PASSWORD);

        Toast.makeText(this, "name : "+name, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "password : "+passwordString, Toast.LENGTH_SHORT).show();

        // email
        String phone = user.get(UserSessionManager.KEY_PhONE);
        Toast.makeText(this, "phone : "+phone, Toast.LENGTH_SHORT).show();


        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category= database.getReference("Category");
        final DatabaseReference table_user= database.getReference("User");



         fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                cartIntent.putExtra("STRING_I_NEED", newString);
                startActivity(cartIntent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG.setAction("Action", null).show();
            }
        });
        fab.setCount(new Database(this).getCountCart());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name for User
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtPhone = (TextView)headerView.findViewById(R.id.txtPhone);
        txtFullName.setText(name);
        txtPhone.setText(phone);

        //Load Menu
        recycler_menu = (RecyclerView)findViewById(R.id.recyclerView);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);
        Paper.init( this );


        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,
                R.layout.menu_item,MenuViewHolder.class,category)
        {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position)
            {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                // final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onCLick(View view, int position, boolean isLongClick)
                    {
                        //Get CategoryId and send to new Activity
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        //Because Category is key, so we Just get Key of this item
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });

            }
        };

        updateToken( FirebaseInstanceId.getInstance().getToken());

        //Register service
//        Intent service = new Intent(Home.this, ListenOrder.class);
//        service.putExtra("STRING_I_NEED", newString);
//        startService(service);

        //Setup Slider
        setupSlider();

    }

    private void setupSlider()
    {
        mSlider = (SliderLayout)findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banner = database.getReference("Banner");
        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Banner banner = postSnapShot.getValue(Banner.class);
                    //Concat string
                    //Pizza_01 ==> and use pizza for sowing description, 01 for food id to click
                    image_list.put(banner.getName()+"_"+banner.getId(),banner.getImage());

                }

                for (String key : image_list.keySet())
                {
                    String[] keySplit = key.split("_");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });
                    // Add Extra Bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idOfFood);

                    mSlider.addSlider(textSliderView);

                    banner.removeEventListener(this);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);

    }

    private void updateToken(String token) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false);
        tokens.child( newString).setValue(data);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart());

        //if(adapter!=null)
            //adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSlider.stopAutoCycle();
    }

    private void loadMenu()
    {

        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing( false );
        //Animation

        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.refresh)
            loadMenu();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart)
        {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            cartIntent.putExtra("STRING_I_NEED", newString);

            startActivity(cartIntent);

        } else if (id == R.id.nav_order)
        {
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            orderIntent.putExtra("STRING_I_NEED", newString);

            startActivity(orderIntent);

        }
        else if (id == R.id.nav_change_pwd)
        {
            showChangePasswordDialog();

        }

        else if (id == R.id.nav_settings)
        {
            showSettingDialog();

        }
        else if (id == R.id.nav_logout)
        {
            Paper.book().destroy();
            session.logoutUser();
           Intent main = new Intent(Home.this,MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(main);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Home.this );
        alertDialog.setTitle( "SETTINGS" );
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_setting = inflater.inflate( R.layout.setting_layout,null);
        final CheckBox ckb_subscribe_new = (CheckBox) layout_setting.findViewById( R.id.ckb_sub_new );
        Paper.init( this );
        String isSubscribe = Paper.book().read( "sub_new" );
        if(isSubscribe == null || TextUtils.isEmpty( isSubscribe ) || isSubscribe.equals( "false" ))
            ckb_subscribe_new.setChecked( false );
        else
            ckb_subscribe_new.setChecked( true );

        alertDialog.setView(layout_setting);

        alertDialog.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                dialogInterface.dismiss();
                if(ckb_subscribe_new.isChecked())
                {
                    FirebaseMessaging.getInstance().subscribeToTopic( Common.topicName );
                    Paper.book().write( "sub_new","true" );
                }
                else
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic( Common.topicName );
                    Paper.book().write( "sub_new","false" );
                }
            }

        } );
        alertDialog.show();
    }

    private void showChangePasswordDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View Layout_pwd = inflater.inflate(R.layout.change_password_layout, null);

        final MaterialEditText edtPassword = (MaterialEditText)Layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText)Layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText)Layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(Layout_pwd);

        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                if (edtPassword.getText().toString().equals(passwordString))
                {
                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                    {
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password",edtNewPassword.getText().toString());

                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(newString)
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, " Password Updated Successfully ! " +
                                                "Please Login Again", Toast.LENGTH_SHORT).show();
                                        session.logoutUser();
                                        session.createLoginSession( nameString, newString ,edtNewPassword.getText().toString());

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                    else
                    {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "New Passwords doesn't match", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                   waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Wrong old Password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

            }
        });

        alertDialog.show();
    }
}
