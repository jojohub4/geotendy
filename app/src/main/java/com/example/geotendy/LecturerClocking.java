package com.example.geotendy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerClocking extends AppCompatActivity {
    private Button clockInButton, clockOutButton;
    private ApiService apiService;
    private String lecturerEmail, registrationNo, firstName; // Remove hardcoded values

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_clocking);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        clockInButton = findViewById(R.id.button4);
        clockOutButton = findViewById(R.id.button5);

        // Retrieve stored lecturer details
        loadLecturerDetails();

        clockInButton.setOnClickListener(v -> {
            Log.d("LecturerClocking", "Clock In button clicked!");
            sendClockingRequest("Clock In");
        });

        clockOutButton.setOnClickListener(v -> {
            Log.d("LecturerClocking", "Clock Out button clicked!");
            sendClockingRequest("Clock Out");
        });
    }

    private void loadLecturerDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        lecturerEmail = sharedPreferences.getString("lecturer_email", null);
        registrationNo = sharedPreferences.getString("lecturer_reg_no", null);
        firstName = sharedPreferences.getString("first_name", null);

        if (lecturerEmail == null || registrationNo == null || firstName == null) {
            Toast.makeText(this, "Error: Lecturer details not found. Please log in again.", Toast.LENGTH_LONG).show();
            Log.e("LecturerClocking", "Lecturer details missing in SharedPreferences");
            finish(); // Close activity if details are missing
        }
    }

    private void sendClockingRequest(String action) {
        long timestamp = System.currentTimeMillis();
        LecturerClockingRequest request = new LecturerClockingRequest(registrationNo, lecturerEmail, firstName, action, timestamp);

        Log.d("LecturerClocking", "Sending JSON request: " + new Gson().toJson(request));

        apiService.lecturerClocking(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LecturerClocking", "Clocking successful: " + response.body().getMessage());
                    Toast.makeText(LecturerClocking.this, "Clocking successful", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorResponse = response.errorBody().string();
                        Log.e("LecturerClocking", "Clocking failed: " + errorResponse);
                        Toast.makeText(LecturerClocking.this, "Clocking failed: " + errorResponse, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("LecturerClocking", "Error reading errorBody: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("LecturerClocking", "Clocking request error: " + t.getMessage());
                Toast.makeText(LecturerClocking.this, "Clocking failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
