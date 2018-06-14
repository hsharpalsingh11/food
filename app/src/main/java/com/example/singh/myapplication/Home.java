package com.example.singh.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.singh.myapplication.Interface.ItemClickListener;
import com.example.singh.myapplication.Model.Category;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.example.singh.myapplication.Service.ListenOrder;
import com.example.singh.myapplication.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName,txtPhone;
    String newString;
    UserSessionManager session;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        session = new UserSessionManager(getApplicationContext());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
       // newString = getIntent().getExtras().getString("STRING_I_NEED");
        //Toast.makeText(this, "phone "+newString, Toast.LENGTH_SHORT).show();
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(UserSessionManager.KEY_NAME);
        newString = user.get(UserSessionManager.KEY_PhONE);

        Toast.makeText(this, "name : "+name, Toast.LENGTH_SHORT).show();

        // email
        String phone = user.get(UserSessionManager.KEY_PhONE);
        Toast.makeText(this, "phone : "+phone, Toast.LENGTH_SHORT).show();


        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category= database.getReference("Category");
        final DatabaseReference table_user= database.getReference("User");



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                cartIntent.putExtra("STRING_I_NEED", newString);
                startActivity(cartIntent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG.setAction("Action", null).show();
            }
        });

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
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);



        loadMenu();


        //Register service
        Intent service = new Intent(Home.this, ListenOrder.class);
        service.putExtra("STRING_I_NEED", newString);
        startService(service);


    }

    private void loadMenu()
    {
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
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
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

        } else if (id == R.id.nav_logout)
        {
            session.logoutUser();
           // Intent main = new Intent(Home.this,MainActivity.class);
            //main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //startActivity(main);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
