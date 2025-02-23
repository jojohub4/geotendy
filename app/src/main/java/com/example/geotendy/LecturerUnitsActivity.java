package com.example.geotendy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerUnitsActivity extends AppCompatActivity {
    private RecyclerView unitRecyclerView;
    private ApiService apiService;
    private List<CourseUnit> lecturerUnits;
    private LecturerUnitsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_units);

        // ✅ Initialize Retrofit API service before using it
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        unitRecyclerView = findViewById(R.id.lecturerUnitsRecyclerView);
        unitRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        lecturerUnits = new ArrayList<>();
        adapter = new LecturerUnitsAdapter(lecturerUnits, this);
        unitRecyclerView.setAdapter(adapter);

        TextView tvStudentInfo = findViewById(R.id.tvStudentInfo);
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String first_name = sharedPreferences.getString("first_name", "lecturer");

        tvStudentInfo.setText("Welcome, " + first_name);

        // Dynamic greeting
        TextView greetingText = findViewById(R.id.textViewGreeting);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good Morning" : (hour < 18) ? "Good Afternoon" : "Good Evening";
        greetingText.setText(greeting);

        fetchLecturerUnits();
    }

    private void fetchLecturerUnits() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String lecturerEmail = sharedPreferences.getString("email", null);

        if (lecturerEmail == null) {
            Toast.makeText(this, "User data missing, please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("API Debug", "Fetching lecturer units for: " + lecturerEmail);

        apiService.getLecturerUnits(lecturerEmail).enqueue(new Callback<LecturerUnitsResponse>() {
            @Override
            public void onResponse(Call<LecturerUnitsResponse> call, Response<LecturerUnitsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lecturerUnits = response.body().getUnits();
                    if (lecturerUnits == null || lecturerUnits.isEmpty()) {
                        Log.e("API Debug", "No units found for lecturer: " + lecturerEmail);
                        Toast.makeText(LecturerUnitsActivity.this, "No units found", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("API Debug", "Units found: " + lecturerUnits.size());
                        adapter.updateData(lecturerUnits);
                    }
                } else {
                    Log.e("API Debug", "Error: Response unsuccessful, response code: " + response.code());
                    try {
                        Log.e("API Debug", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LecturerUnitsActivity.this, "Error retrieving units", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<LecturerUnitsResponse> call, Throwable t) {
                Log.e("API Debug", "API Call Failed: " + t.getMessage());
                Toast.makeText(LecturerUnitsActivity.this, "API Call Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
