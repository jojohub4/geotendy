package com.example.geotendy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerAttendanceActivity extends AppCompatActivity {
    private TextView unitCodeTextView, unitNameTextView;
    private RecyclerView attendanceRecyclerView;
    private ApiService apiService;
    private String unitCode, unitName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_attendance);

        unitCodeTextView = findViewById(R.id.unitCode);
        unitNameTextView = findViewById(R.id.unitName);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);

        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("unitCode") && intent.hasExtra("unitName")) {
            unitCode = intent.getStringExtra("unitCode");
            unitName = intent.getStringExtra("unitName");

            unitCodeTextView.setText("Unit Code: " + unitCode);
            unitNameTextView.setText("Unit Name: " + unitName);

            fetchAttendance();
        } else {
            Toast.makeText(this, "Invalid unit selected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchAttendance() {
        apiService.getLecturerAttendance(unitCode, unitName).enqueue(new Callback<AttendanceSummaryResponse>() {
            @Override
            public void onResponse(Call<AttendanceSummaryResponse> call, Response<AttendanceSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceSummaryResponse data = response.body();
                    AttendanceSummaryAdapter adapter = new AttendanceSummaryAdapter(data.getStudents(),
                            data.getTotalStudents(), data.getPresentStudents(), data.getAbsentStudents());
                    attendanceRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(LecturerAttendanceActivity.this, "No attendance data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceSummaryResponse> call, Throwable t) {
                Toast.makeText(LecturerAttendanceActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
