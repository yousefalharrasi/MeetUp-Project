package com.example.ammar.android2project;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationServices;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.support.annotation.NonNull;


import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MapsActivity extends ActionBarActivity implements OnMapReadyCallback {

    private TextView txtEmail;
    private GoogleMap mMap;
    private String longitude = null;
    private String lattitude = null;
    //Need to be changed to be automatic
    LatLng startPoint = new LatLng(0, 0);
    private String email = null;
    private LatLng userLocation = null;
    private String userID;
    private String userName;
    FirebaseUser currentFirebaseUser;
    DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    Boolean status;
    private Firebase firebase = null;
    String ss = "";
    SimpleDateFormat simpleDateFormat = null;
    long date;
    LoginActivity l = new LoginActivity();
    private Marker currentUserMarker;
    private Location mLastLocation;
    //test
    LocationManager locationManager;
    String currentUserName = "";
    private long UPDATE_INTERVAL = 10 * 1000;  //* 10 secs *//*
    private long FASTEST_INTERVAL = 2000; //* 2 sec *//*
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startLocationUpdates();



        firebase.setAndroidContext(this);

        Intent intent = getIntent();
        longitude = intent.getStringExtra("long");
        lattitude = intent.getStringExtra("lat");

        if (lattitude == null || longitude == null) {
            lattitude = "0.0";
            longitude = "0.0";
        }

        email = intent.getStringExtra("email");
        //userID=intent.getStringExtra("userIDD");
        userName = intent.getStringExtra("name");

        //To make vibration when login
        Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        firebaseAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // To Write On Database for  current user's Location >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        //This Code to Get All Users That their Status is <True or Online> >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                mMap.clear();
                String longt = null;
                String lati = null;
                LatLng userLocation = null;
                for (final DataSnapshot user : dataSnapshot.getChildren()) {

                    if (user.child("email").getValue().equals(currentFirebaseUser.getEmail())) {
                        longt = user.child("longitude").getValue(String.class);
                        lati = user.child("latitude").getValue(String.class);
                        userLocation = new LatLng(Double.parseDouble(lati), Double.parseDouble(longt));
                        if (lattitude == null || longitude == null) {
                            lattitude = lati;
                            longitude = longt;
                        }
                        currentUserName = user.child("name").getValue(String.class);

                        startPoint = userLocation;
                        currentUserMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Current Login User")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .snippet(lattitude + " ,,, " + longitude));
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                        if (userLocation != null) {
                            // Add Circle >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                            mMap.addCircle(new CircleOptions().center(userLocation).radius(100).strokeColor(Color.RED).fillColor(Color.WHITE));
                        }
                        // This is How to Find or Detect User's Location >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(true);


                    } else if (user.child("status").getValue().equals(true)) {
                        longt = user.child("longitude").getValue(String.class);
                        lati = user.child("latitude").getValue(String.class);
                        final String name = user.child("name").getValue(String.class);
                        userLocation = new LatLng(Double.parseDouble(lati), Double.parseDouble(longt));
                        mMap.addMarker(new MarkerOptions().position(userLocation).title(name));
                        showNotification(userLocation);
                        final LatLng finalUserLocation = userLocation;
                        final Marker m = mMap.addMarker(new MarkerOptions().position(finalUserLocation).title(name).snippet(user.child("email").getValue().toString()));

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                UserDetails.chatWith = marker.getTitle();
                                UserDetails.username = currentUserName;
                                Intent intent1 = new Intent(MapsActivity.this, Chat.class);
                                startActivity(intent1);
                                databaseReference.child("Users").child(userID).child("status").setValue(true);
                            }
                        });
                    }

                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /* @Override
    protected void onStop() {
        super.onStop();
        databaseReference.child("Users").child(userID).child("status").setValue(false);
        databaseReference.child("Users").child(userID).child("latitude").setValue(lattitude);
        databaseReference.child("Users").child(userID).child("longitude").setValue(longitude);
        Toast.makeText(this, "Destroy OR Stop", Toast.LENGTH_SHORT).show();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.child("Users").child(userID).child("status").setValue(true);
        databaseReference.child("Users").child(userID).child("latitude").setValue(lattitude);
        databaseReference.child("Users").child(userID).child("longitude").setValue(longitude);
        Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        txtEmail = (TextView) findViewById(R.id.txtShowEmail);
        txtEmail.setText(email.toString());

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (mLastLocation != null) {
            databaseReference.child("Users").child(userID).child("latitude").setValue(mLastLocation.getLatitude());
            databaseReference.child("Users").child(userID).child("longitude").setValue(mLastLocation.getLongitude());

        } else {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.maptypeHYBRID:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    return true;
                }
            case R.id.maptypeNONE:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    return true;
                }
            case R.id.maptypeNORMAL:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                }
            case R.id.maptypeSATELLITE:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                }
            case R.id.maptypeTERRAIN:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                }


                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //This gets the user locations


    public void onLocationChanged(Location location) {

        mLastLocation = location;

        Double curLat = location.getLatitude();//current latitude
        Double curLong = location.getLongitude();//current longitude

            databaseReference.child("Users").child(userID).child("latitude").setValue(String.valueOf(curLat));
            databaseReference.child("Users").child(userID).child("longitude").setValue(String.valueOf(curLong));

    }






    public void LogOut(View view) {
        simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy (hh:mm)a");
        date = System.currentTimeMillis();

        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Toast.makeText(this, "You Log Out", Toast.LENGTH_SHORT).show();

        //UserInformation userInfo = new UserInformation(email, userName, longitude, lattitude, simpleDateFormat.format(date), false);
        databaseReference.child("Users").child(userID).child("status").setValue(false);
        databaseReference.child("Users").child(userID).child("latitude").setValue(lattitude);
        databaseReference.child("Users").child(userID).child("longitude").setValue(longitude);



    }

//Calculate the distance of two users
    public double calculateDistance(LatLng startP, LatLng endP) {

        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(startP.latitude);
        locationA.setLongitude(startP.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(endP.latitude);
        locationB.setLongitude(endP.longitude);
        distance = locationA.distanceTo(locationB);
        return distance;
    }
//show notification if the user is near
    public void showNotification(LatLng la) {


        if (calculateDistance(startPoint, la) < 100 && calculateDistance(startPoint, la) != 0) {

            Toast.makeText(this, "Someone is Close to you", Toast.LENGTH_LONG).show();
            Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        } else return;


    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }


    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }



}
