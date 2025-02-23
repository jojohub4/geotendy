package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geotendy.LoginResponse;
import com.example.geotendy.LoginRequest;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText edEmail, edRegistration;
    private Button btn;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String userRole = sharedPreferences.getString("role", "unknown");
        String regNo = sharedPreferences.getString("registration_no", "Not Found");
        String email = sharedPreferences.getString("email", "Not Found");

        // Log stored values to debug missing data
        Log.d("AppStart", "Retrieved Registration No: " + regNo);
        Log.d("AppStart", "Retrieved Email: " + email);
        Log.d("AppStart", "Retrieved Role: " + userRole);

        // If user is logged in but registration/email is missing, force re-login
        if (isLoggedIn) {
            if ("Not Found".equals(regNo) || "Not Found".equals(email)) {
                Log.d("AppStart", "User data missing, forcing re-login...");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
            } else {
                redirectToDashboard(userRole);
                return;
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        edEmail = findViewById(R.id.editTextTextEmailAddress);
        edRegistration = findViewById(R.id.editTextRegistrationNo);
        btn = findViewById(R.id.button);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edEmail.getText().toString().trim();
                String registrationNumber = edRegistration.getText().toString().trim();
                String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

                if (email.isEmpty() || registrationNumber.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                verifyUser(email, registrationNumber);
            }
        });
    }



    private void saveUnitsToSharedPreferences(List<CourseUnit> courses) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("courseCount", courses.size()); // Store the number of courses
        Log.d(TAG, "Saving course count: " + courses.size());

        for (int i = 0; i < courses.size(); i++) {
            editor.putString("unitCode_" + i, courses.get(i).getUnitCode());
            editor.putString("unitName_" + i, courses.get(i).getUnitName());
            Log.d(TAG, "Saving course " + i + ": " + courses.get(i).getUnitCode() + " - " + courses.get(i).getUnitName());
        }

        editor.apply(); // Commit the changes
    }

    private void verifyUser(String email, String registrationNumber) {
        LoginRequest request = new LoginRequest(email, registrationNumber);
        Call<LoginResponse> call = apiService.loginUser(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Registration No: " + response.body().getRegistration_no());
                    Log.d("API Response", "Email: " + response.body().getEmail());
                    Log.d("API Response", "Role: " + response.body().getRole());

                    // Get shared preferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // Store common user details (both student & lecturer)
                    editor.putString("first_name", response.body().getFirst_name());
                    editor.putString("registration_no", response.body().getRegistration_no());
                    editor.putString("email", response.body().getEmail());
                    editor.putString("role", response.body().getRole());
                    editor.putBoolean("isLoggedIn", true);

                    // ✅ Store additional lecturer-specific details
                    if ("lecturer".equals(response.body().getRole())) {
                        Log.d("API Response", "Lecturer login detected");
                        editor.putString("lecturer_email", response.body().getEmail());
                        editor.putString("lecturer_reg_no", response.body().getRegistration_no());
                    }

                    // ✅ Store additional student-specific details (only if role is student)
                    if ("student".equals(response.body().getRole()) && response.body().getCourses() != null) {
                        Log.d("API Response", "Saving student courses...");
                        saveUnitsToSharedPreferences(response.body().getCourses());
                    } else {
                        Log.d("API Response", "No courses found (Lecturer/Admin)");
                    }

                    // Apply changes
                    editor.apply();

                    // Redirect to the correct dashboard
                    redirectToDashboard(response.body().getRole());
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or registration number.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    private void redirectToDashboard(String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Debug log: Ensure correct data is stored
        Log.d("Redirect", "Stored Registration No: " + sharedPreferences.getString("registration_no", "Not Found"));
        Log.d("Redirect", "Stored Email: " + sharedPreferences.getString("email", "Not Found"));
        Log.d("Redirect", "Stored Role: " + sharedPreferences.getString("role", "Not Found"));

        Intent intent;

        switch (role) {
            case "student":
                intent = new Intent(LoginActivity.this, StudentDashboard.class);
                break;
            case "lecturer":
                intent = new Intent(LoginActivity.this, LecturerDashboard.class);
                break;
            case "admin":
                intent = new Intent(LoginActivity.this, AdminDashboard.class);
                break;
            default:
                Toast.makeText(LoginActivity.this, "Unknown user role. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
        }

        startActivity(intent);
        finish();
    }

}