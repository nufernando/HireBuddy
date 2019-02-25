package com.example.user.hirebuddy_application;

import android.Manifest;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.*;


public class CustomerMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mlastLocation;
    LocationRequest mLocationRequest;
    DrawerLayout drawer;

    private static final int Request_User_Location_Code = 99;
    private Button mechanicRequest;
    private LatLng customerLocation;
    private Boolean requestBol = false;
    private Marker serviceMarker;
    private String userID;
    private TextView drawerUsername, drawerEmail;
    private ImageView drawerImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLocationPermission();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapView mv = new MapView(getApplicationContext());
                    mv.onCreate(null);
                    mv.onPause();
                    mv.onDestroy();
                }catch (Exception ignored){

                }
            }
        }).start();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        drawerUsername = (TextView) headerView.findViewById(R.id.drawer_username);
        drawerEmail = (TextView) headerView.findViewById(R.id.drawer_email);
        drawerImage = (ImageView) headerView.findViewById(R.id.drawer_image);

        getCustomerUsernameDatabaseInstance();
        getCustomerEmailDatabaseInstance();
        getCustomerImageDatabaseInstance();

        drawerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (CustomerMapActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Mechanic Button click event
         */
        mechanicRequest = (Button) findViewById(R.id.requestBuddy);

        mechanicRequest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(requestBol){
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    mechanicLocationRef.removeEventListener(mechanicLocationRefListner);

                    if(mechanicFoundID != null){
                        DatabaseReference mechanicRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Mechanics").child(mechanicFoundID);
                        mechanicRef.setValue(true);
                        mechanicFoundID = null;
                    }

                    mechanicFound = false;
                    radius = 1;

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userID);

                    if(serviceMarker != null){
                        serviceMarker.remove();
                    }

                    mechanicRequest.setText("Hire a Buddy");

                }else{
                    requestBol = true;

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userID, new GeoLocation(mlastLocation.getLatitude(), mlastLocation.getLongitude()));

                    customerLocation = new LatLng(mlastLocation.getLatitude(), mlastLocation.getLongitude());
                    serviceMarker = mMap.addMarker(new MarkerOptions().position(customerLocation).title("I'm Here")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pin)));

                    mechanicRequest.setText("Finding a Buddy...");

                    getClosestMechanic();
                }
            }
        });
    }

    /**
     * Select Closest Mechanic Process
     */
    private int radius = 1;
    private Boolean mechanicFound = false;
    private String mechanicFoundID;
    GeoQuery geoQuery;
    private void getClosestMechanic (){
        DatabaseReference mechanicLocation = FirebaseDatabase.getInstance().getReference().child("MechanicAvailable");

        GeoFire geoFire = new GeoFire(mechanicLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerLocation.latitude, customerLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!mechanicFound && requestBol){

                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> mechanicMap = (Map<String, Object>)dataSnapshot.getValue();
                                if(mechanicFound){
                                    return;
                                }
                                if(mechanicMap.get("MechanicType").equals("Mechanic")){
                                    mechanicFound = true;
                                    mechanicFoundID = dataSnapshot.getKey();

                                    DatabaseReference mechanicRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicFoundID);
                                    String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("CustomerHireID", customerID);
                                    mechanicRef.updateChildren(map);

                                    getMechanicLocation();
                                    mechanicRequest.setText("Looking For Buddy's Location...");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!mechanicFound)
                {
                    radius++; //if there's no any mechanic around the radius then it check by incrementing the radius.
                    getClosestMechanic();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    /**
     * Get Mechanic Location Process
     */
    private Marker mMechanicMarker;
    private DatabaseReference mechanicLocationRef;
    private ValueEventListener mechanicLocationRefListner;
    private void getMechanicLocation(){
        mechanicLocationRef = FirebaseDatabase.getInstance().getReference().child("MechanicsWorking").child(mechanicFoundID).child("l");
        mechanicLocationRefListner = mechanicLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mechanicRequest.setText("HireBuddy Found..");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng mechanicLatLng = new LatLng(locationLat, locationLng);
                    if(mMechanicMarker != null){
                        mMechanicMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(customerLocation.latitude);
                    loc1.setLongitude(customerLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(mechanicLatLng.latitude);
                    loc2.setLongitude(mechanicLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    DecimalFormat df = new DecimalFormat("#.##");

                    String dist = "";

                    if(distance < 1000.0){
                        dist = df.format(distance) + " m";
                    }else
                    {
                        dist = df.format(distance / 1000) + " km";
                    }


                    if (distance < 100){
                        mechanicRequest.setText("HireBuddy is Here");
                    }else
                    {
                        mechanicRequest.setText("Cancel Request | HireBuddy Distance : "+ dist);
                    }

                    if(PredefineMethods.getCurrentSystemHour() >= PredefineMethods.NIGHT_HOUR
                            || PredefineMethods.getCurrentSystemHour() <=PredefineMethods.MORNING_HOUR){
                        mMechanicMarker = mMap.addMarker(new MarkerOptions().position(mechanicLatLng)
                                .title("Your Mechanic").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mechanic_car_marker_night)));
                    }else
                    {
                        mMechanicMarker = mMap.addMarker(new MarkerOptions().position(mechanicLatLng)
                                .title("Your Mechanic").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mechanic_car_marker_day)));
                    }


                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });
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
        getMenuInflater().inflate(R.menu.customer_map_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about_us) {
            Intent intent = new Intent (CustomerMapActivity.this, AboutUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_home) {
            DrawerLayout drawerHome = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerHome.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent (CustomerMapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected( Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null) {
            mlastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try{
            boolean success;

            if(PredefineMethods.getCurrentSystemHour() >= PredefineMethods.NIGHT_HOUR
                    || PredefineMethods.getCurrentSystemHour() <=PredefineMethods.MORNING_HOUR){
                success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstylenight));
            }else
            {
                success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
            }

            if(!success){
                Log.e("CustomerMapActivity", "Style parsing failed.");
            }
        }catch (Resources.NotFoundException e){
            Log.e("CustomerMapActivity", "Can't find style, Error : ", e);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    public boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }else
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(mGoogleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    private void getCustomerUsernameDatabaseInstance(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        DatabaseReference usernameDatabaseRef = rootRef.child("CustomerName");
        usernameDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    drawerUsername.setText(dataSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
 }
    private void getCustomerEmailDatabaseInstance(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        DatabaseReference emailDatabaseRef = rootRef.child("CustomerEmail");
        emailDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    drawerEmail.setText(dataSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getCustomerImageDatabaseInstance(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        DatabaseReference imageDatabaseRef = rootRef.child("CustomerProfileImage");
        imageDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Picasso.get().load(dataSnapshot.getValue().toString()).into(drawerImage);
                    drawerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
