package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapSelectionActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String PREFS_NAME = "GeofenceSettings";
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Load last saved location
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        double lat = Double.parseDouble(sharedPreferences.getString("latitude", "-1.188968933830619"));
        double lon = Double.parseDouble(sharedPreferences.getString("longitude", "36.96256212002725"));
        LatLng savedLocation = new LatLng(lat, lon);

        // Show the saved location with a marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(savedLocation, 15));
        mMap.addMarker(new MarkerOptions().position(savedLocation).title("Current Geofence Location"));

        // Select new location when user taps on the map
        mMap.setOnMapClickListener(newLocation -> {
            selectedLocation = newLocation;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(newLocation).title("Selected Location"));

            // Immediately save the new location
            saveLocation(newLocation);
            Toast.makeText(MapSelectionActivity.this, "Location updated! Tap back to apply changes.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveLocation(LatLng location) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", String.valueOf(location.latitude));
        editor.putString("longitude", String.valueOf(location.longitude));
        editor.apply();

        // Send back result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", location.latitude);
        resultIntent.putExtra("longitude", location.longitude);
        setResult(RESULT_OK, resultIntent);
    }
}
