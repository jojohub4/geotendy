package com.example.geotendy;

import android.Manifest;
import android.content.Context;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminClocking extends AppCompatActivity {
    private static final String TAG = "AdminClocking";
    private Button clockInButton, clockOutButton;
    private ApiService apiService;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SharedPreferences sharedPreferences;
    private static final float REQUIRED_RADIUS = 250;

    private String adminEmail, registrationNo, firstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_clocking);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        sharedPreferences = getSharedPreferences("GeofenceSettings", MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        clockInButton = findViewById(R.id.buttonAdminClockIn);
        clockOutButton = findViewById(R.id.buttonAdminClockOut);

        loadAdminDetails();

        clockInButton.setOnClickListener(v -> {
            Log.d(TAG, "Clock In button clicked!");
            checkLocationAndClock("Clock In");
        });

        clockOutButton.setOnClickListener(v -> {
            Log.d(TAG, "Clock Out button clicked!");
            checkLocationAndClock("Clock Out");
        });
    }

    private LatLng getSavedGeofenceLocation() {
        double lat = Double.parseDouble(sharedPreferences.getString("latitude", "-1.188968933830619"));
        double lon = Double.parseDouble(sharedPreferences.getString("longitude", "36.96256212002725"));
        return new LatLng(lat, lon);
    }

    private void checkLocationAndClock(String action) {
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

    private boolean isWithinGeofence(android.location.Location location, LatLng savedLocation) {
        float[] distance = new float[1];
        android.location.Location.distanceBetween(
                location.getLatitude(), location.getLongitude(),
                savedLocation.latitude, savedLocation.longitude,
                distance
        );
        return distance[0] <= REQUIRED_RADIUS;
    }

    private void loadAdminDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        adminEmail = sharedPreferences.getString("admin_email", null);
        registrationNo = sharedPreferences.getString("admin_reg_no", null);
        firstName = sharedPreferences.getString("admin_name", null);

        // ✅ Log to check if values are missing
        Log.d("AdminClocking", "Admin Details Loaded: Email=" + adminEmail + ", Reg No=" + registrationNo + ", Name=" + firstName);

        if (adminEmail == null || registrationNo == null || firstName == null) {
            Toast.makeText(this, "Error: Admin details not found. Please log in again.", Toast.LENGTH_LONG).show();
            Log.e("AdminClocking", "Admin details missing in SharedPreferences");
            finish(); // Close activity if details are missing
        }
    }



    private void sendClockingRequest(String action) {
        long timestamp = System.currentTimeMillis();

        AdminClockingRequest request = new AdminClockingRequest(registrationNo, adminEmail, firstName, action, timestamp);

        apiService.adminClocking(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("AdminClocking", "Clocking successful: " + response.body().getMessage());
                    Toast.makeText(AdminClocking.this, "Clocking successful", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorResponse = response.errorBody().string();
                        Log.e("AdminClocking", "Clocking failed: " + errorResponse);  // ✅ Add detailed logging
                        Toast.makeText(AdminClocking.this, "Clocking failed: " + errorResponse, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("AdminClocking", "Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("AdminClocking", "API Request Failed: " + t.getMessage());
                Toast.makeText(AdminClocking.this, "Clocking request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
