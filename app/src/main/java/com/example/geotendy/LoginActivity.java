package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(this, StudentDashboard.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edEmail = findViewById(R.id.editTextTextEmailAddress);
        edRegistration = findViewById(R.id.editTextRegistrationNo);
        btn = findViewById(R.id.button);



        // Initialize apiService using RetrofitClient
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edEmail.getText().toString().trim();
                String registrationNumber = edRegistration.getText().toString().trim();

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
            editor.putString("unitCode_" + i, courses.get(i).getUnitCode()); // Save each unit code
            editor.putString("unitName_" + i, courses.get(i).getUnitName()); // Save each unit name
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
                    if (response.body().isSuccess()) {

                        // Retrieve the courses from the response body
                        List<CourseUnit> courses = response.body().getCourses();

                        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("first_name", response.body().getFirst_name());
                        editor.putString("registration_no", response.body().getRegistration_no());
                        editor.putString("email", response.body().getEmail());
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

// Debugging: Log the stored values to ensure they are saved
                        Log.d("Debug", "Stored registration_no: " + sharedPreferences.getString("registration_no", "Not Found"));
                        Log.d("Debug", "Stored email: " + sharedPreferences.getString("email", "Not Found"));
                        Log.d("Debug", "Stored first_name: " + sharedPreferences.getString("first_name", "Not Found"));


                        // Save unit codes and unit names to SharedPreferences
                        saveUnitsToSharedPreferences(courses);

                        // Navigate to unitActivity with data in SharedPreferences
                        Intent intent = new Intent(LoginActivity.this, StudentDashboard.class);
                        intent.putExtra("courseList", new ArrayList<>(response.body().getCourses()));
                        startActivity(intent);
                        finish();
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
}
