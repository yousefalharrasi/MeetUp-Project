package com.example.ammar.android2project;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    TextView txtLogin;
    EditText edUserName, edEmail, edPassword;
    String userName, password, email;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private String userID;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    String lattitude = "";
    String longitude = "";
    SimpleDateFormat simpleDateFormat = null;
    long date;
    boolean status = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        edUserName = (EditText) findViewById(R.id.input_name);
        edEmail = (EditText) findViewById(R.id.input_email);
        edPassword = (EditText) findViewById(R.id.input_password);
        txtLogin = (TextView) findViewById(R.id.link_login);


        firebaseAuth = FirebaseAuth.getInstance();
        /*firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();*/



        databaseReference = FirebaseDatabase.getInstance().getReference();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(CreateAccountActivity.this, MapsActivity.class);
                    intent.putExtra("long", longitude);
                    intent.putExtra("lat", lattitude);
                    intent.putExtra("email", email);
                    //intent.putExtra("userIDD", userID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }
            }
        };

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(CreateAccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void CreateAccount(View view) {
        simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy (hh:mm)a");
        date = System.currentTimeMillis();
        if (!validate()) {
            Toast.makeText(getBaseContext(), "Create Account Failed", Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateAccountActivity.this, "Successful Register", Toast.LENGTH_SHORT).show();
                        firebaseAuth.addAuthStateListener(authStateListener);
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(CreateAccountActivity.this, "No GPS is ON", Toast.LENGTH_SHORT).show();


                        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getLocation();
                        }
                        // Old Way to save to Firebase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                        UserInformation userInformation = new UserInformation(email, userName, longitude, lattitude, simpleDateFormat.format(date),status);
                        userID = firebaseAuth.getCurrentUser().getUid();
                        databaseReference.child("Users").child(userID).setValue(userInformation);

                        // New Way to save to Firebase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                       /* Map newPost = new HashMap();
                        newPost.put("email", email);
                        newPost.put("latitude", lattitude);
                        newPost.put("longitude", longitude);
                        newPost.put("name", userName);
                        newPost.put("registrationDate", simpleDateFormat.format(date));
                        newPost.put("status", status);
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                        databaseReference.setValue(newPost);*/

                        edUserName.setText("");
                        edEmail.setText("");
                        edPassword.setText("");
                    }
                }
            });


        }


    }


    // This is How to Get User's location >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    List<String> latlongList = new ArrayList<>();

    public List<String> getLocation() {
        latlongList.clear();
        if (ActivityCompat.checkSelfPermission(CreateAccountActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (CreateAccountActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CreateAccountActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                Toast.makeText(this, "Your current location is: " + lattitude + " ... " + longitude, Toast.LENGTH_SHORT).show();

            } else if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                Toast.makeText(this, "Your current location is: " + lattitude + " ... " + longitude, Toast.LENGTH_SHORT).show();

            } else if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);


                Toast.makeText(this, "Your current location is: " + lattitude + " ... " + longitude, Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, "Unable to Trace your location", Toast.LENGTH_SHORT).show();

            }
        }
        latlongList.add(longitude);
        latlongList.add(lattitude);
        return latlongList;
    }

    // This is For Validation of User name , Email , Password >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    public boolean validate() {
        boolean valid = true;

        userName = edUserName.getText().toString();
        email = edEmail.getText().toString();
        password = edPassword.getText().toString();

        if (userName.isEmpty() || userName.length() < 3) {
            edUserName.setError("at least 3 characters");
            valid = false;
        } else {
            edUserName.setError(null);
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("enter a valid email address");
            valid = false;
        } else {
            edEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 2 || password.length() > 10) {
            edPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            edPassword.setError(null);
        }

        return valid;
    }


}
