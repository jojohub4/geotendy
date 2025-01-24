// Units.java
package com.example.geotendy;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Units extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private RecyclerView recyclerView;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        // Force hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Geofencing
        geofencingClient = LocationServices.getGeofencingClient(this);

        TextView tvStudentInfo = findViewById(R.id.tvStudentInfo);
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String registration_no = sharedPreferences.getString("registration_no", null);
        String email = sharedPreferences.getString("email", null);
        String first_name = sharedPreferences.getString("first_name", null);

        Log.d("Debug", "Retrieved registration_no: " + registration_no);
        Log.d("Debug", "Retrieved email: " + email);
        Log.d("Debug", "Retrieved first_name: " + first_name);

        tvStudentInfo.setText("Welcome, " + first_name);

        // Dynamic greeting
        TextView greetingText = findViewById(R.id.textViewGreeting);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 0 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        greetingText.setText(greeting);

        // Get the course list from SharedPreferences
        int courseCount = sharedPreferences.getInt("courseCount", 0);
        Log.d(TAG, "Course count retrieved: " + courseCount);
        List<CourseUnit> courses = new ArrayList<>();

        // Loop through the number of courses saved in SharedPreferences
        for (int i = 0; i < courseCount; i++) {
            String unitCode = sharedPreferences.getString("unitCode_" + i, null);
            String unitName = sharedPreferences.getString("unitName_" + i, null);

            if (unitCode != null && unitName != null) {
                courses.add(new CourseUnit(unitCode, unitName));
                Log.d(TAG, "Retrieved course: " + unitCode + " - " + unitName);
            } else {
                Log.e(TAG, "Failed to retrieve course for index: " + i);
            }
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the CourseAdapter with the list of courses
        CourseAdapter courseAdapter = new CourseAdapter(courses, this);
        recyclerView.setAdapter(courseAdapter);

        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            createGeofence();
        }

        checkLocationServicesEnabled();
        checkCurrentLocation();
    }

    private void checkLocationServicesEnabled() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> {
                    Log.d(TAG, "Location services are enabled.");
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(this, REQUEST_LOCATION_PERMISSION);
                        } catch (IntentSender.SendIntentException ex) {
                            Log.e(TAG, "Error enabling location services", ex);
                        }
                    }
                });
    }

    // Handle runtime permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createGeofence();
            } else {
                Toast.makeText(this, "Location permission is required to enable geofencing", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkCurrentLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    float[] distance = new float[1];
                    android.location.Location.distanceBetween(
                            location.getLatitude(), location.getLongitude(),
                            -1.15386885166207, 36.962571402878766, // Replace with your geofence coordinates
                            distance
                    );

                    SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    if (distance[0] <= 50) { // Replace with geofence radius
                        editor.putBoolean("isInGeofence", true);
                        Log.d(TAG, "User is in geofence area. Flag updated to true.");
                    } else {
                        editor.putBoolean("isInGeofence", false);
                        Log.d(TAG, "User is outside geofence area. Flag updated to false.");
                    }
                    editor.apply();
                }
            });
        }
    }

    private void createGeofence() {
        Geofence geofence = new Geofence.Builder()
                .setRequestId("required_location")
                .setCircularRegion(
                        -1.15386885166207, // Latitude
                        36.962571402878766, // Longitude
                        50 // Radius in meters
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        geofencePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent(this, GeofenceBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofence added successfully");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add geofence: " + e.getMessage());
                    }
                });
    }
}
