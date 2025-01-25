package com.example.geotendy;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create an ApiService instance using RetrofitClient
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Fetch attendance logs
        fetchAttendanceLogs();
    }

    private void fetchAttendanceLogs() {
        apiService.fetchAttendanceLogs().enqueue(new Callback<List<AttendanceLog>>() {
            @Override
            public void onResponse(Call<List<AttendanceLog>> call, Response<List<AttendanceLog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AttendanceLog> logs = calculateDurationAndStatus(response.body());
                    adapter = new AttendanceAdapter(logs);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(ReportActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceLog>> call, Throwable t) {
                Log.e("ReportActivity", "Error: " + t.getMessage());
                Toast.makeText(ReportActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractDate(String timestamp) {
        if (timestamp != null && !timestamp.isEmpty()) {
            return timestamp.split("T")[0]; // Splits at 'T' and gets the first part (date)
        }
        return "N/A"; // Default value if null
    }

    private String extractTime(String timestamp) {
        if (timestamp != null && !timestamp.isEmpty()) {
            String[] parts = timestamp.split("T"); // Splits at 'T'
            if (parts.length > 1) {
                return parts[1].split("\\.")[0]; // Gets time part and removes milliseconds
            }
        }
        return "N/A"; // Default value if null
    }

    private String calculateDuration(String punchInTime, String punchOutTime) {
        if (punchInTime == null || punchOutTime == null) {
            return "N/A"; // Return if either is missing
        }

        try {
            // Define the format of the time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date punchIn = timeFormat.parse(punchInTime);
            Date punchOut = timeFormat.parse(punchOutTime);

            // Calculate duration in milliseconds
            long durationMillis = punchOut.getTime() - punchIn.getTime();
            long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);

            // Convert minutes into hours and minutes
            return String.format(Locale.getDefault(), "%d hrs %d mins",
                    durationMinutes / 60, durationMinutes % 60);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error"; // Handle parsing errors
        }
    }

    private String determineStatus(String duration) {
        if ("N/A".equals(duration) || "Error".equals(duration)) {
            return "N/A"; // Return default for missing or erroneous duration
        }

        try {
            // Extract hours and minutes from the duration string
            String[] parts = duration.split(" ");
            int hours = Integer.parseInt(parts[0]); // First part is hours
            int minutes = Integer.parseInt(parts[2]); // Third part is minutes

            // Convert total duration to minutes
            int totalMinutes = hours * 60 + minutes;

            // Determine status
            if (totalMinutes >= 90 && totalMinutes <= 180) {
                return "Present";
            } else if (totalMinutes < 90) {
                return "Late";
            } else {
                return "Exceeds Limit"; // Optional: handle if duration exceeds 3 hours
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error"; // Handle parsing errors
        }
    }

    private List<AttendanceLog> calculateDurationAndStatus(List<AttendanceLog> logs) {
        List<AttendanceLog> updatedLogs = new ArrayList<>();

        for (AttendanceLog log : logs) {
            try {
                // Extract date and time from timestamps
                String punchInDate = extractDate(log.getPunchInTime());
                String punchOutDate = extractDate(log.getPunchOutTime());
                String punchInTime = extractTime(log.getPunchInTime());
                String punchOutTime = extractTime(log.getPunchOutTime());

//                log.setPunchInDate(punchInDate);
//                log.setPunchOutDate(punchOutDate);

                // Calculate duration and determine status
                String duration = calculateDuration(punchInTime, punchOutTime);
                String status = determineStatus(duration);

                log.setDuration(duration);
                log.setStatus(status);

            } catch (Exception e) {
                Log.e("ReportActivity", "Error processing log: " + e.getMessage());
            }
            updatedLogs.add(log);
        }

        return updatedLogs;
    }

}
