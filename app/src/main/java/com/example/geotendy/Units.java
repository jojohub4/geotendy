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
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Units extends AppCompatActivity {

    private static final String TAG = "UnitsActivity";
    static final int REQUEST_LOCATION_PERMISSION = 101;
    private LatLng getRequiredLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences("GeofenceSettings", MODE_PRIVATE);
        double lat = Double.parseDouble(sharedPreferences.getString("latitude", "-1.188968933830619"));
        double lon = Double.parseDouble(sharedPreferences.getString("longitude", "36.96256212002725"));
        return new LatLng(lat, lon);
    }

    private static final float REQUIRED_RADIUS = 250; // Radius in meters

    private RecyclerView recyclerView;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize location and geofencing services
        geofencingClient = LocationServices.getGeofencingClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        TextView tvStudentInfo = findViewById(R.id.tvStudentInfo);
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String first_name = sharedPreferences.getString("first_name", "User");

        tvStudentInfo.setText("Welcome, " + first_name);

        // Dynamic greeting
        TextView greetingText = findViewById(R.id.textViewGreeting);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good Morning" : (hour < 18) ? "Good Afternoon" : "Good Evening";
        greetingText.setText(greeting);

        // Get the course list from SharedPreferences
        int courseCount = sharedPreferences.getInt("courseCount", 0);
        List<CourseUnit> courses = new ArrayList<>();

        for (int i = 0; i < courseCount; i++) {
            String unitCode = sharedPreferences.getString("unitCode_" + i, null);
            String unitName = sharedPreferences.getString("unitName_" + i, null);
            if (unitCode != null && unitName != null) {
                courses.add(new CourseUnit(unitCode, unitName));
            } else {
                Log.e(TAG, "Failed to retrieve course for index: " + i);
            }
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CourseAdapter(courses, this));

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
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> Log.d(TAG, "Location services enabled."))
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createGeofence();
            } else {
                Toast.makeText(this, "Location permission required for geofencing", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    float[] distance = new float[1];

                    LatLng updatedLocation = getRequiredLocation();  // ✅ Get admin-updated location

                    android.location.Location.distanceBetween(
                            location.getLatitude(), location.getLongitude(),
                            updatedLocation.latitude, updatedLocation.longitude,  // ✅ Use updated geofence
                            distance
                    );

                    boolean isInGeofence = distance[0] <= REQUIRED_RADIUS;

                    SharedPreferences.Editor editor = getSharedPreferences("UserProfile", Context.MODE_PRIVATE).edit();
                    editor.putBoolean("isInGeofence", isInGeofence);
                    editor.apply();

                    Log.d(TAG, "Geofence status updated: " + isInGeofence);

                    if (!isInGeofence) {
                        Toast.makeText(this, "You are outside the required location. Attendance not allowed.", Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch location: " + e.getMessage()));
        }
    }


    private void createGeofence() {
        LatLng location = getRequiredLocation(); // Get updated location

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Old geofence removed. Adding new one.");

                    Geofence geofence = new Geofence.Builder()
                            .setRequestId("required_location")
                            .setCircularRegion(location.latitude, location.longitude, REQUIRED_RADIUS)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build();

                    GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                            .addGeofence(geofence)
                            .build();

                    if (ActivityCompat.checkSelfPermission(Units.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence: " + e.getMessage()));
                    }
                });
    }


    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return geofencePendingIntent;
    }
}
