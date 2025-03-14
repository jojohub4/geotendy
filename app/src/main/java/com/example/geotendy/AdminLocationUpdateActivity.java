package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AdminLocationUpdateActivity extends AppCompatActivity {

    private EditText etLatitude, etLongitude, etRadius;
    private Button btnSaveLocation, btnSelectOnMap;
    private static final String PREFS_NAME = "GeofenceSettings";
    private static final int MAP_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_location_update);

        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        btnSaveLocation = findViewById(R.id.btnSaveLocation);
        btnSelectOnMap = findViewById(R.id.btnSelectOnMap);

        // Load saved location
        loadSavedLocation();

        // Save the location
        btnSaveLocation.setOnClickListener(v -> saveLocation());

        // Open map to select a location
        btnSelectOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLocationUpdateActivity.this, MapSelectionActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });
    }

    private void loadSavedLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etLatitude.setText(sharedPreferences.getString("latitude", "-1.188968933830619"));
        etLongitude.setText(sharedPreferences.getString("longitude", "36.96256212002725"));
        etRadius.setText(sharedPreferences.getString("radius", "250"));
    }

    private void saveLocation() {
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();
        String radius = etRadius.getText().toString().trim();

        if (latitude.isEmpty() || longitude.isEmpty() || radius.isEmpty()) {
            Toast.makeText(this, "Please enter all values!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", latitude);
        editor.putString("longitude", longitude);
        editor.putString("radius", radius);
        editor.apply();

        Toast.makeText(this, "Geofence location & radius updated!", Toast.LENGTH_SHORT).show();
        Log.d("AdminDashboard", "New Location: Lat=" + latitude + ", Lon=" + longitude + ", Radius=" + radius);
    }

    // ✅ Handle map result and update fields
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", -1);
            double longitude = data.getDoubleExtra("longitude", -1);

            if (latitude != -1 && longitude != -1) {
                etLatitude.setText(String.valueOf(latitude));
                etLongitude.setText(String.valueOf(longitude));
                Toast.makeText(this, "New location selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
