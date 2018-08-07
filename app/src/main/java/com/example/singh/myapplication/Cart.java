package com.example.singh.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.singh.myapplication.Common.Common;
import com.example.singh.myapplication.Common.Config;
import com.example.singh.myapplication.Database.Database;
import com.example.singh.myapplication.Helper.RecyclerItemTouchHelper;
import com.example.singh.myapplication.Interface.RecyclerItemtouchHelperListener;
import com.example.singh.myapplication.Model.MyResponse;
import com.example.singh.myapplication.Model.Notification;
import com.example.singh.myapplication.Model.Order;
import com.example.singh.myapplication.Model.Request;

import com.example.singh.myapplication.Model.Sender;
import com.example.singh.myapplication.Model.Token;
import com.example.singh.myapplication.Model.User;
import com.example.singh.myapplication.Model.UserSessionManager;
import com.example.singh.myapplication.Remote.APIService;
import com.example.singh.myapplication.Remote.IGoogleService;
import com.example.singh.myapplication.ViewHolder.CartAdapter;
import com.example.singh.myapplication.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.singh.myapplication.Common.Common.currentUser;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,RecyclerItemtouchHelperListener
{
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    String newString= "";
    String namee= "";
    UserSessionManager session;
    Double lattitude,longitude;
    Place shippingAddress;

    APIService mService;

    public TextView txtTotalPrice;
    Button btnPlace;
    Long balance;

    List<Order> cart = new ArrayList<>();


    CartAdapter cartAdapter;
    RelativeLayout rootLayout;
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    String address, comment;
    private static final int PAYPAL_REQUEST_CODE = 9999;

    //private LocationRequest mLocationRequest;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    //Declare Google Map API Retrofit
    IGoogleService mGoogleMapService;


    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
       // newString = getIntent().getExtras().getString("STRING_I_NEED");
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();

        //init
        mGoogleMapService = Common.getGoogleMapAPi();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
           ActivityCompat.requestPermissions( this,new String[]
                   {
                           Manifest.permission.ACCESS_COARSE_LOCATION,
                           Manifest.permission.ACCESS_FINE_LOCATION
                   },LOCATION_REQUEST_CODE);
        }
        else
        {
            if(checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }



        // name
        namee = user.get(UserSessionManager.KEY_NAME);
        newString = user.get(UserSessionManager.KEY_PhONE);


        //init service
        mService = Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);



        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (Button)findViewById(R.id.btnPlaceOrder);


        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);


        loadListFood();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( UPDATE_INTERVAL );
        mLocationRequest.setFastestInterval( FASTEST_INTERVAL );
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position)
    {
        if (viewHolder instanceof CartViewHolder)
        {

            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());

             final int deleteIndex = viewHolder.getAdapterPosition();

            cartAdapter.removeItem(deleteIndex);
            deleteCart(deleteIndex);

            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),newString);
            //deleteCart(deleteItem.getProductId(),newString);

            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts();
            for(Order item:orders)
                total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en","US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + "removed from cart!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartAdapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts();
                    for(Order item:orders)
                        total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en","US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int result_code = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );
        if(result_code != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError( result_code ))
                GooglePlayServicesUtil.getErrorDialog( result_code,this,PLAY_SERVICE_REQUEST ).show();
            else
            {
                Toast.makeText( this, "This Device is not supported", Toast.LENGTH_SHORT ).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void loadListFood()
    {
        cart = new Database(this).getCarts();
        cartAdapter = new CartAdapter(cart,this);
        cartAdapter.notifyDataSetChanged();
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

                if(cart.size() > 0)
                  showAlertDialog();
                else
                    Toast.makeText( Cart.this, "Your cart is Empty!!", Toast.LENGTH_SHORT ).show();
            }
        });
    }

    private void showAlertDialog()
    {
        AlertDialog.Builder alertDalog = new AlertDialog.Builder(Cart.this);
        alertDalog.setTitle("One More Step..");
        alertDalog.setMessage("Enter Your Address");
//        Toast.makeText(Cart.this, " Balance "+currentUser.getBalance(), Toast.LENGTH_SHORT).show();

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);
       // final MaterialEditText edtAddress = (MaterialEditText) order_address_comment.findViewById(R.id.edtAddress);

        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //set Hint for autocomplete edit text
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your Address");
        //Set text Size
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        //get adreess from place autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
               lattitude= shippingAddress.getLatLng().latitude;
               longitude= shippingAddress.getLatLng().longitude;
            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR",status.getStatusMessage());

            }
        });

        //Radio
        final RadioButton rbShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rbShipToAddress);
        final RadioButton rbHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rbShipToHome);

        final RadioButton rbCod = (RadioButton) order_address_comment.findViewById(R.id.rbCOD);
        final RadioButton rbPaypal = (RadioButton) order_address_comment.findViewById(R.id.rbPaypal);
        final RadioButton rbBalance = (RadioButton) order_address_comment.findViewById(R.id.rbBalance);

        //Event Radio
        rbShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Ship to this address feature
                if(isChecked)
                {
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //If fetch API ok
                                    try
                                    {
                                      lattitude =  mLastLocation.getLatitude();
                                      longitude =  mLastLocation.getLongitude();
                                        JSONObject jsonObject = new JSONObject(response.body().toString());
                                        JSONArray resultArray = jsonObject.getJSONArray("results");
                                        JSONObject firstObject = resultArray.getJSONObject(0);
                                        address = firstObject.getString("formatted_address");
                                        //Set this address to EdtAddress
                                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);
        alertDalog.setView(order_address_comment);
        alertDalog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDalog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {


                //Add Check condition here
                //if user select address from Place Fragment, just use it
                //if ser select ship to this address, get Address form location and use it
                //if use select home address, get homeAddress from profile and use it

                if(!rbShipToAddress.isChecked() && !rbHomeAddress.isChecked())
                {
                    if(shippingAddress != null) {
                        address = shippingAddress.getAddress().toString();
                        Toast.makeText(Cart.this, ""+address, Toast.LENGTH_SHORT).show();

                    }

                    else
                    {
                        Toast.makeText(Cart.this, "Please enter address or select options given", Toast.LENGTH_SHORT).show();
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }






                //first, get Address and Comment from Alert Dialog
                comment = edtComment.getText().toString();

                //CHeck payment
                if(!rbCod.isChecked() && !rbPaypal.isChecked() && !rbBalance.isChecked())
                {
                    Toast.makeText(Cart.this, "Please Select Payment method", Toast.LENGTH_SHORT).show();

                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;

                }
                else if(rbPaypal.isChecked())
                {

                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("$", "")
                            .replace(",", "");
                    float amoun = Float.parseFloat(formatAmount);

                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                            "USD",
                            "Forever Hungry App Order",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                }
                else if(rbCod.isChecked())
                {

                    //create new request
                    Request request = new Request(
                            newString,address,
                            "Unpaid",
                            "COD",
                            txtTotalPrice.getText().toString(),
                            namee,
                            "0",
                            comment,
                            String.format("%s,%s",mLastLocation.getLongitude(),mLastLocation.getLongitude()),cart
                    );

                    //Submit to firebase
                    String order_number = String.valueOf(System.currentTimeMillis());

                    requests.child(order_number).setValue(request);
                    //Delete Cart
                    new Database(getBaseContext()).cleanCart();

                    sendNotificationOrder(order_number);
                    Toast.makeText(Cart.this, "Thank You, Order Placed", Toast.LENGTH_SHORT).show();
                    finish();
                }

                else if(rbBalance.isChecked())
                {
                    Toast.makeText(Cart.this, ""+ currentUser.getBalance(), Toast.LENGTH_SHORT).show();
                        double amount=0.0;
                    try {
                        amount = Common.formatCurrency( txtTotalPrice.getText().toString(),Locale.US ).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if( Common.currentUser.getBalance()  >= amount)
                    {

                        //create new request
                        Request request = new Request(
                                newString,address,
                                "Paid",
                                "Forever Hungry Balance",
                                txtTotalPrice.getText().toString(),
                                namee,
                                "0",
                                comment,
                                String.format("%s,%s",mLastLocation.getLongitude(),mLastLocation.getLongitude()),cart
                        );

                        //Submit to firebase
                        final String order_number = String.valueOf(System.currentTimeMillis());

                        requests.child(order_number).setValue(request);
                        //Delete Cart
                        new Database(getBaseContext()).cleanCart();
                        //update balance

                        final double balance = Common.currentUser.getBalance() - amount;
                        final Map<String,Object> update_balance = new HashMap<>();
                        update_balance.put("balance",balance);
                        FirebaseDatabase.getInstance()
                                .getReference("User")
                                .child( newString )
                                .updateChildren(update_balance)
                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            currentUser.setBalance(balance);
                                            FirebaseDatabase.getInstance()
                                                    .getReference("User")
                                                    .child( newString)
                                                    .addListenerForSingleValueEvent( new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            currentUser = dataSnapshot.getValue( User.class);
                                                            sendNotificationOrder(order_number);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    } );

                                        }
                                    }
                                } );
                    }
                    else
                    {
                        Toast.makeText( Cart.this, "Your balance not enough, please choose other payment", Toast.LENGTH_SHORT ).show();
                    }
                }

            }
        });

        alertDalog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDalog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PAYPAL_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null)
                {
                    try
                    {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);



                        //create new request
                        Request request = new Request(
                                newString,address,
                                jsonObject.getJSONObject("response").getString("state"),
                                "Paypal",
                                txtTotalPrice.getText().toString(),
                                namee,
                                "0",
                                comment,
                                String.format("%s,%s",lattitude,longitude),cart
                        );

                        //Submit to firebase
                        String order_number = String.valueOf(System.currentTimeMillis());

                        requests.child(order_number).setValue(request);
                        //Delete Cart
                        new Database(getBaseContext()).cleanCart();

                        sendNotificationOrder(order_number);
                        Toast.makeText(Cart.this, "Thank You, Order Placed", Toast.LENGTH_SHORT).show();
                        finish();

                    }

                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(Cart.this, "PAyment Canceled", Toast.LENGTH_SHORT).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(Cart.this, "Invalid Payment", Toast.LENGTH_SHORT).show();

        }
    }
    private void sendNotificationOrder(final String order_number)
    {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener( new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Token serverToken = postSnapShot.getValue(Token.class);

                    //create raw payload to send
                    Notification notification = new Notification("Forever Hungry", "You have New Order"+order_number);
                    Sender content = new Sender(serverToken.getToken(),notification);
                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.body().success == 1)
                                    {
                                        Toast.makeText(Cart.this, "Thank you, Order Placed", Toast.LENGTH_SHORT).show();

                                            finish();
                                    }
                                    else
                                    {
                                        //Toast.makeText(Cart.this, "Order Placing failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t)
                                {
                                    Toast.makeText(Cart.this, "Error : "+t.getMessage().toString(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove( position );
        new Database( this).cleanCart();
        for(Order item:cart)
            new Database( this ).addToCart( item );
        loadListFood();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            Log.d("LOCATION","Your location : "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }
        else
        {
            Log.d("LOCATION","Could not get your location!!");
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient,mLocationRequest,this );
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
}
