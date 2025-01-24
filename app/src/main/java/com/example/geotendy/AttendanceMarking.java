package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceMarking extends AppCompatActivity {

    private static final String TAG = "AttendanceMarking";
    private TextView tvUnitInfo;
    private Button buttonTimeIn, buttonTimeOut;
    private boolean isPunchedIn = false;
    private ApiService apiService;

    private void initiateRetrofit() {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_marking);

        // Force hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Log.d(TAG, "Initializing Retrofit instance");
        initiateRetrofit();

        tvUnitInfo = findViewById(R.id.tvUnitInfo);
        buttonTimeIn = findViewById(R.id.buttonTimeIn);
        buttonTimeOut = findViewById(R.id.buttonTimeOut);

        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String registration_no = sharedPreferences.getString("registration_no", null);
        String email = sharedPreferences.getString("email", null);
        String first_name = sharedPreferences.getString("first_name", null);

        // Retrieve unit information from the intent
        Intent intent = getIntent();
        String unitCode = intent.getStringExtra("unitCode");
        String unitName = intent.getStringExtra("unitName");

        Log.d("Debug", "Received registration_no: " + registration_no);
        Log.d("Debug", "Received email: " + email);
        Log.d("Debug", "Received first_name: " + first_name);

        // Display unit information
        tvUnitInfo.setText("Unit: " + unitCode + " - " + unitName);

        // Set up button click listeners
        buttonTimeIn.setOnClickListener(v -> recordAttendance("in", unitCode, unitName, registration_no, email, first_name));
        buttonTimeOut.setOnClickListener(v -> {
            if (!isPunchedIn) {
                Toast.makeText(this, "Please punch in first", Toast.LENGTH_SHORT).show();
            } else {
                recordAttendance("out", unitCode, unitName, registration_no, email, first_name);
            }
        });

        Log.d(TAG, "Setting up BottomNavigationView");
//        // Set up BottomNavigationView
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//
//            if (itemId == R.id.navigation_report) {
//                startActivity(new Intent(AttendanceMarking.this, ReportActivity.class));
//                return true;
//            } else if (itemId == R.id.navigation_home) {
//                startActivity(new Intent(AttendanceMarking.this, StudentDashboard.class));
//                return true;
//            } else if (itemId == R.id.navigation_support) {
//                startActivity(new Intent(AttendanceMarking.this, SupportActivity.class));
//                return true;
//            }
//
//            return false;
//        });
    }

    private void recordAttendance(String action, String unitCode, String unitName, String registration_no, String email, String first_name) {
        Log.d(TAG, "Recording attendance");
        AttendanceRecord record = new AttendanceRecord(registration_no, email, first_name, unitCode, unitName, action, System.currentTimeMillis());

        // Send data to server
        sendAttendanceToServer(record);

        if ("in".equals(action)) {
            isPunchedIn = true;
            Toast.makeText(this, "Punched In successfully", Toast.LENGTH_SHORT).show();
        } else {
            isPunchedIn = false;
            Toast.makeText(this, "Punched Out successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendAttendanceToServer(AttendanceRecord record) {
        Log.d(TAG, "Sending attendance to server: " + record.toString());
        Call<Void> call = apiService.recordAttendance(record);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(AttendanceMarking.this, "Attendance recorded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AttendanceMarking.this, "Failed to record attendance: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(AttendanceMarking.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
