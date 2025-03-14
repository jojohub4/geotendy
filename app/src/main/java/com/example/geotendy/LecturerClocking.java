package com.example.geotendy;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerClocking extends AppCompatActivity {
    private static final String TAG = "LecturerClocking";
    private Button clockInButton, clockOutButton;
    private ApiService apiService;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SharedPreferences sharedPreferences;

    private static final float REQUIRED_RADIUS = 250; // Geofence radius in meters

    private String lecturerEmail, registrationNo, firstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_clocking);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        sharedPreferences = getSharedPreferences("GeofenceSettings", MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        clockInButton = findViewById(R.id.button4);
        clockOutButton = findViewById(R.id.button5);

        loadLecturerDetails();

        clockInButton.setOnClickListener(v -> {
            Log.d(TAG, "Clock In button clicked!");
            checkLocationAndClock("Clock In");
        });

        clockOutButton.setOnClickListener(v -> {
            Log.d(TAG, "Clock Out button clicked!");
            checkLocationAndClock("Clock Out");
        });
    }

    /**
     * Retrieves the latest geofence location set by the admin.
     */
    private LatLng getSavedGeofenceLocation() {
        double lat = Double.parseDouble(sharedPreferences.getString("latitude", "-1.153868933830619"));
        double lon = Double.parseDouble(sharedPreferences.getString("longitude", "36.96256212002725"));
        return new LatLng(lat, lon);
    }

    /**
     * Checks if GPS is enabled before verifying location.
     */
    private void checkLocationAndClock(String action) {
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> verifyLocationAndClock(action))
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(this, 101);
                        } catch (IntentSender.SendIntentException ex) {
                            Log.e(TAG, "Error enabling location services", ex);
                        }
                    } else {
                        Toast.makeText(this, "Please enable GPS for attendance", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Retrieves the current location and checks if it's within the geofence.
     */
    private void verifyLocationAndClock(String action) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng savedLocation = getSavedGeofenceLocation();
                boolean isInGeofence = isWithinGeofence(location, savedLocation);

                if (isInGeofence) {
                    sendClockingRequest(action);
                } else {
                    Toast.makeText(this, "You are outside the required location. Attendance not allowed.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Unable to fetch location. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching location: " + e.getMessage());
            Toast.makeText(this, "Failed to fetch location", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Checks if the lecturer's current location is within the required geofence radius.
     */
    private boolean isWithinGeofence(android.location.Location location, LatLng savedLocation) {
        float[] distance = new float[1];
        android.location.Location.distanceBetween(
                location.getLatitude(), location.getLongitude(),
                savedLocation.latitude, savedLocation.longitude,
                distance
        );
        return distance[0] <= REQUIRED_RADIUS;
    }

    /**
     * Loads lecturer details from SharedPreferences.
     */
    private void loadLecturerDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        lecturerEmail = sharedPreferences.getString("lecturer_email", null);
        registrationNo = sharedPreferences.getString("lecturer_reg_no", null);
        firstName = sharedPreferences.getString("first_name", null);

        if (lecturerEmail == null || registrationNo == null || firstName == null) {
            Toast.makeText(this, "Error: Lecturer details not found. Please log in again.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Lecturer details missing in SharedPreferences");
            finish();
        }
    }

    /**
     * Sends clocking request to the API.
     */
    private void sendClockingRequest(String action) {
        long timestamp = System.currentTimeMillis();
        ClockingRequest request = new ClockingRequest(registrationNo, lecturerEmail, firstName, "lecturer", action, timestamp);

        apiService.clocking(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(LecturerClocking.this, "Clocking successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LecturerClocking.this, "Clocking failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LecturerClocking.this, "Clocking request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
