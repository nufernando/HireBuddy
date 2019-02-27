package com.example.user.hirebuddy_application;

import android.Manifest;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import common.PredefineMethods;

public class MechanicMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, RoutingListener {

        private GoogleMap mMap;
        GoogleApiClient mGoogleApiClient;
        Location mLastLocation;
        LocationRequest mLocationRequest;
        private Boolean requestBol = false;
        private String customerId = "";
        private Boolean curentLogoutStatus = false;
        private static final int Request_User_Location_Code = 99;
        DrawerLayout drawer;
        private String userId;
        private TextView drawerUsername, drawerEmail, customerName, customerEmail;
        private ImageView drawerImage, customerProfileImage;
        private RelativeLayout customerInfomation, mechanicDetails;
        private Animation slideUp;
        private Animation slideOut;
        private Boolean registrationStatus = false;
        private RadioGroup mechanicType;
        private RadioButton mechanicRadio, technicianRadio;
        private String mechanicTypeValue = "";
        private List<Polyline> polylines;
        private static final int[] COLORS = new int[]{android.R.color.holo_red_light};

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_mechanic_map);

            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                checkUserLocationPermission();
            }

            polylines = new ArrayList<>();

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
            customerInfomation = (RelativeLayout) findViewById(R.id.customerInfo);
            mechanicDetails = (RelativeLayout) findViewById(R.id.mechanicDetails);
            customerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
            customerName = (TextView) findViewById(R.id.customerName);
            customerEmail = (TextView) findViewById(R.id.customerEmail);
            mechanicType = (RadioGroup) findViewById(R.id.mechanicType);
            mechanicRadio = (RadioButton) findViewById(R.id.radio_mechanic);
            technicianRadio = (RadioButton) findViewById(R.id.radio_technician);

            // Animation
            slideUp = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_up);
            slideOut = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_out);

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

            getMechanicUsernameDatabaseInstance();
            getMechanicEmailDatabaseInstance();
            getMechanicImageDatabaseInstance();
            viewWelcomMessage();

            drawerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent (MechanicMapActivity.this, MechanicProfileActivity.class);
                    startActivity(intent);
                }
            });

            getAssignedCustomer();
        }


    /**
     *Get Assigned Customer Process
     */
    private void getAssignedCustomer(){
        String mechanicId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicId).child("CustomerHireID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerServiceLocation();
                    getAssignedCustomerInfo();
                }else
                {
                    erasePolylines();
                    customerId = "";
                    if(serviceMarker != null){
                        serviceMarker.remove();
                    }
                    if (assignedCustomerServiceupLocationRefListner != null) {
                        assignedCustomerServiceupLocationRef.removeEventListener(assignedCustomerServiceupLocationRefListner);
                    }
                    if(customerInfomation.getVisibility() == View.VISIBLE){
                        customerInfomation.startAnimation(slideOut);
                        customerInfomation.setVisibility(View.INVISIBLE);
                        customerName.setText("");
                        customerEmail.setText("");
                        Picasso.get().load("https://lh3.googleusercontent.com/-j0_tfWvEL-c/XG_n6hCX_OI/AAAAAAAAATQ/AX6Nw6cu994oQzUFxSUNgXlE4LNTsWrSQCEwYBhgL/w140-h139-p").into(customerProfileImage);
                        customerProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    /**
     *Get Assigned Customer Service Location Process
     */
    Marker serviceMarker;
    private DatabaseReference assignedCustomerServiceupLocationRef;
    private ValueEventListener assignedCustomerServiceupLocationRefListner;
    private void getAssignedCustomerServiceLocation(){
        assignedCustomerServiceupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest")
                .child(customerId).child("l");
        assignedCustomerServiceupLocationRefListner = assignedCustomerServiceupLocationRef
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng serviceLatLng = new LatLng(locationLat,locationLng);
                        serviceMarker = mMap.addMarker(new MarkerOptions().position(serviceLatLng).title("Service Location")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pin)));
                        getRouteToMarker(serviceLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRouteToMarker(LatLng serviceLatLng) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyDQodJxByKZYCNG277QUsI48lVAmnm4W_c")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), serviceLatLng)
                .build();
        routing.execute();
    }

    /**
     *Get Assigned Customer Information Process
     */
    private void getAssignedCustomerInfo() {

        DatabaseReference customerRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference customerRef = customerRootRef.child("Users").child("Customers").child(customerId);

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("CustomerName") != null){
                        customerName.setText(map.get("CustomerName").toString());
                    }

                    if(map.get("CustomerEmail") != null){
                        customerEmail.setText(map.get("CustomerEmail").toString());
                    }

                    if(map.get("CustomerProfileImage") != null){
                        Picasso.get().load(map.get("CustomerProfileImage").toString()).into(customerProfileImage);
                        customerProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(customerInfomation.getVisibility() == View.GONE || customerInfomation.getVisibility() == View.INVISIBLE){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            customerInfomation.setVisibility(View.VISIBLE);
            customerInfomation.startAnimation(slideUp);
        }
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
            getMenuInflater().inflate(R.menu.mechanic_map_activity, menu);
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
                Intent intent = new Intent (MechanicMapActivity.this, AboutUsMechanicActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_home) {
                DrawerLayout drawerHome = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerHome.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_logout) {
                curentLogoutStatus = true;
                DisconnectMechanic();
                FirebaseAuth.getInstance().signOut();
                LogoutMechanic();
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
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
    public void onConnected(@Nullable Bundle bundle) {
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
        if(getApplicationContext()!=null){

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("MechanicAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("MechanicsWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (customerId){
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!curentLogoutStatus){
            DisconnectMechanic();
        }

    }

    private void DisconnectMechanic() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("MechanicAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    private void LogoutMechanic() {
        Intent intent = new Intent(MechanicMapActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    private void getMechanicUsernameDatabaseInstance(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userId);
        DatabaseReference usernameDatabaseRef = rootRef.child("MechanicName");
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
    private void getMechanicEmailDatabaseInstance(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userId);
        DatabaseReference emailDatabaseRef = rootRef.child("MechanicEmail");
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

    private void getMechanicImageDatabaseInstance(){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userId);
        DatabaseReference imageDatabaseRef = rootRef.child("MechanicProfileImage");
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

    private void viewWelcomMessage() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userId);
        DatabaseReference mechanicTypeDatabaseRef = rootRef.child("MechanicType");
        mechanicTypeDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getValue().toString().equals("NA")){
                        mechanicDetails.setVisibility(View.VISIBLE);
                        mechanicDetails.startAnimation(slideUp);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userId);
        switch(view.getId()) {
            case R.id.radio_mechanic:
                if (checked)
                    rootRef.child("MechanicType").setValue("Mechanic");
                    break;
            case R.id.radio_technician:
                if (checked)
                    rootRef.child("MechanicType").setValue("Technician");
                    break;
        }mechanicDetails.startAnimation(slideOut);
        mechanicDetails.setVisibility(View.INVISIBLE);
        Toast.makeText(MechanicMapActivity.this,"Thank You!" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();;
        }

        polylines.clear();
    }
}

