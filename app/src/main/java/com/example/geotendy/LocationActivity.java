package com.example.geotendy;

import android.Manifest;
import android.content.Intent;
import android.app.PendingIntent;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final LatLng REQUIRED_LOCATION = new LatLng(-1.15386885166207, 36.962571402878766); // Replace with actual required location
    private static final float REQUIRED_RADIUS = 50; // Radius in meters

    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        geofencingClient = LocationServices.getGeofencingClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Request permissions
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

        // Add marker and circle for the required location
        mMap.addMarker(new MarkerOptions().position(REQUIRED_LOCATION).title("Required Location"));
        mMap.addCircle(new CircleOptions()
                .center(REQUIRED_LOCATION)
                .radius(REQUIRED_RADIUS)
                .strokeColor(0x220000FF) // Blue stroke
                .fillColor(0x220000FF)   // Transparent fill
                .strokeWidth(2));

        // Zoom into the required location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(REQUIRED_LOCATION, 15));

        // Show current location
        showCurrentLocation();
    }

    private void showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Add a marker for the user's current location
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                    // Check if the user is inside the geofence
                    float[] distance = new float[1];
                    android.location.Location.distanceBetween(
                            location.getLatitude(), location.getLongitude(),
                            REQUIRED_LOCATION.latitude, REQUIRED_LOCATION.longitude,
                            distance);

                    if (distance[0] <= REQUIRED_RADIUS) {
                        Toast.makeText(this, "You are in the required location!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "You are not in the required location. Please move closer.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createGeofence() {
        Geofence geofence = new Geofence.Builder()
                .setRequestId("required_location")
                .setCircularRegion(REQUIRED_LOCATION.latitude, REQUIRED_LOCATION.longitude, REQUIRED_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, GeofenceBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence: " + e.getMessage()));
        }
    }

//    private void proceedToAttendanceMarking() {
//        Intent intent = new Intent(LocationActivity.this, AttendanceMarking.class);
//        startActivity(intent);
//        finish();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createGeofence();
                showCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required for geofencing", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
