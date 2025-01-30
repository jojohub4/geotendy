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

        // Retrieve login session details
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String userRole = sharedPreferences.getString("role", "unknown"); // Retrieve stored role

        // If user is already logged in, redirect to the correct dashboard
        if (isLoggedIn) {
            redirectToDashboard(userRole); // Correctly redirect based on role
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Force hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        edEmail = findViewById(R.id.editTextTextEmailAddress);
        edRegistration = findViewById(R.id.editTextRegistrationNo);
        btn = findViewById(R.id.button);

        // Initialize API service using RetrofitClient
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edEmail.getText().toString().trim();
                String registrationNumber = edRegistration.getText().toString().trim();
                String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID); // Fetch device ID

                if (email.isEmpty() || registrationNumber.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                verifyUser(email, registrationNumber, deviceId);
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

    private void verifyUser(String email, String registrationNumber, String deviceId) {
        LoginRequest request = new LoginRequest(email, registrationNumber, deviceId);
        Call<LoginResponse> call = apiService.loginUser(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {

                        // Get user role
                        String role = response.body().getRole();

                        // Store user data in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("first_name", response.body().getFirst_name());
                        editor.putString("registration_no", response.body().getRegistration_no());
                        editor.putString("email", response.body().getEmail());
                        editor.putString("role", role);
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        Log.d("Debug", "Stored registration_no: " + sharedPreferences.getString("registration_no", "Not Found"));
                        Log.d("Debug", "Stored email: " + sharedPreferences.getString("email", "Not Found"));
                        Log.d("Debug", "Stored first_name: " + sharedPreferences.getString("first_name", "Not Found"));
                        Log.d("Debug", "Stored role: " + sharedPreferences.getString("role", "Not Found"));

                        //save unit codes and unit names to sharedPreferences
                        if ("student".equals(role)) {
                            List<CourseUnit> courses = response.body().getCourses();
                            saveUnitsToSharedPreferences(courses);
                        }


                        // Redirect user based on role
                        redirectToDashboard(role);

                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or registration number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void redirectToDashboard(String role) {
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
