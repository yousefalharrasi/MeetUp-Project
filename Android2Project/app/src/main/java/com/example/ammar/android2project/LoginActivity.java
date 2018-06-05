package com.example.ammar.android2project;

import android.*;
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
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText edEmail, edPassword;
    TextView txtCreateAccount;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener authStateListener;
    String userName, email, password;
    String lattitude, longitude;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    private String userID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edEmail = (EditText) findViewById(R.id.email);
        edPassword = (EditText) findViewById(R.id.password);
        txtCreateAccount = (TextView) findViewById(R.id.create);


        firebaseAuth = FirebaseAuth.getInstance();


        databaseReference = FirebaseDatabase.getInstance().getReference();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    intent.putExtra("long", longitude);
                    intent.putExtra("lat", lattitude);
                    intent.putExtra("email", email);
                    intent.putExtra("userIDD", userID);
                    intent.putExtra("userName", userName);
                    startActivity(intent);

                }

            }
        };


        // Here To Create a New Account >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);

            }
        });


    }

    public void Login(View view) {

        email = edEmail.getText().toString();
        password = edPassword.getText().toString();
        if (validate() == false) {
            Toast.makeText(getBaseContext(), "LogIn Failed", Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Failed To LogIn: Check Your Email and Password", Toast.LENGTH_SHORT).show();

                    } else {
                        firebaseAuth.addAuthStateListener(authStateListener);
                        Toast.makeText(LoginActivity.this, "Successfully LogIn", Toast.LENGTH_SHORT).show();


                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(LoginActivity.this, "No GPS is ON", Toast.LENGTH_SHORT).show();


                        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getLocation();
                        }

                        if (lattitude == null || longitude == null)
                        {
                            lattitude = "0.0";
                            longitude = "0.0";
                        }
                        userID = firebaseAuth.getCurrentUser().getUid();
                        databaseReference.child("Users").child(userID).child("status").setValue(true);
                        databaseReference.child("Users").child(userID).child("latitude").setValue(lattitude);
                        databaseReference.child("Users").child(userID).child("longitude").setValue(longitude);
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
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (LoginActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                Log.e("longitude",latti+"");
                Log.e("lattitude",longi+"");

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

                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();

            }
        }
        latlongList.add(longitude);
        latlongList.add(lattitude);
        return latlongList;
    }

    public boolean validate() {
        boolean valid = true;

        email = edEmail.getText().toString();
        password = edPassword.getText().toString();


        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("enter a valid email address");
            valid = false;
        } else {
            edEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            edPassword.setError("Password Incorrect");
            valid = false;
        } else {
            edPassword.setError(null);
        }

        return valid;
    }


}
