package com.example.geotendy;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final String PREFS_NAME = "GeofenceSettings";

    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;
    private PendingIntent geofencePendingIntent;
    private SharedPreferences sharedPreferences;
    private Circle geofenceCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        geofencingClient = LocationServices.getGeofencingClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            createGeofence();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadGeofenceData(); // Load required location
        displayCurrentLocation(); // Display user's current location
    }

    private void displayCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // Add marker for current location
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Your Current Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                            // Adjust map to fit both current and required locations
                            LatLng requiredLocation = getSavedGeofenceLocation();
                            LatLngBounds bounds = new LatLngBounds.Builder()
                                    .include(currentLocation)
                                    .include(requiredLocation)
                                    .build();

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        } else {
                            Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching current location: " + e.getMessage());
                        Toast.makeText(this, "Error fetching current location.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Request location permissions if not already granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }


    private void loadGeofenceData() {
        LatLng requiredLocation = getSavedGeofenceLocation();
        float radius = getSavedRadius();

        // ✅ Debugging: Log the loaded values
        Log.d(TAG, "Loaded Geofence: Lat=" + requiredLocation.latitude + ", Lon=" + requiredLocation.longitude + ", Radius=" + radius);

        updateMap(requiredLocation, radius);
    }

    private void updateMap(LatLng location, float radius) {
        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(location).title("Required Location"));
        geofenceCircle = mMap.addCircle(new CircleOptions()
                .center(location)
                .radius(radius)
                .strokeColor(0x550000FF)
                .fillColor(0x220000FF)
                .strokeWidth(3));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private LatLng getSavedGeofenceLocation() {
        double lat = Double.parseDouble(sharedPreferences.getString("latitude", "-1.188968933830619"));
        double lon = Double.parseDouble(sharedPreferences.getString("longitude", "36.96256212002725"));
        return new LatLng(lat, lon);
    }

    private float getSavedRadius() {
        return Float.parseFloat(sharedPreferences.getString("radius", "250"));
    }

    private void createGeofence() {
        LatLng requiredLocation = getSavedGeofenceLocation();
        float radius = getSavedRadius();

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnCompleteListener(task -> {
                    Geofence geofence = new Geofence.Builder()
                            .setRequestId("required_location")
                            .setCircularRegion(requiredLocation.latitude, requiredLocation.longitude, radius)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build();

                    GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                            .addGeofence(geofence)
                            .build();

                    if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
