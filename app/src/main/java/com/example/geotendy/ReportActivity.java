package com.example.geotendy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private TableLayout tableLayout;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize TableLayout
        tableLayout = findViewById(R.id.tableLayout);

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
                    if (logs.isEmpty()) {
                        Toast.makeText(ReportActivity.this, "No attendance logs found", Toast.LENGTH_SHORT).show();
                    } else {
                        populateTable(logs); // Pass logs to populateTable()
                    }
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

    private void populateTable(List<AttendanceLog> attendanceLogs) {
        for (AttendanceLog log : attendanceLogs) {
            TableRow tableRow = new TableRow(this);

            // Add Date
            TextView dateTextView = new TextView(this);
            dateTextView.setText(log.getDate());
            dateTextView.setPadding(8, 8, 8, 8);
            dateTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            tableRow.addView(dateTextView);

            // Add Unit Code
            TextView unitCodeTextView = new TextView(this);
            unitCodeTextView.setText(log.getUnitCode());
            unitCodeTextView.setPadding(8, 8, 8, 8);
            unitCodeTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            tableRow.addView(unitCodeTextView);

            // Add Unit Name
            TextView unitNameTextView = new TextView(this);
            unitNameTextView.setText(log.getUnitName());
            unitNameTextView.setPadding(8, 8, 8, 8);
            unitNameTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            unitNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            unitNameTextView.setSingleLine(true);
            tableRow.addView(unitNameTextView);

            // Add Punch In Time
            TextView punchInTextView = new TextView(this);
            punchInTextView.setText(log.getPunchInTime());
            punchInTextView.setPadding(8, 8, 8, 8);
            punchInTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            tableRow.addView(punchInTextView);

            // Add Punch Out Time
            TextView punchOutTextView = new TextView(this);
            punchOutTextView.setText(log.getPunchOutTime());
            punchOutTextView.setPadding(8, 8, 8, 8);
            punchOutTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            tableRow.addView(punchOutTextView);

            // Add Duration
            TextView durationTextView = new TextView(this);
            durationTextView.setText(log.getDuration());
            durationTextView.setPadding(8, 8, 8, 8);
            durationTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            tableRow.addView(durationTextView);

            // Add Status
            TextView statusTextView = new TextView(this);
            statusTextView.setText(log.getStatus());
            statusTextView.setPadding(8, 8, 8, 8);
            statusTextView.setBackground(getResources().getDrawable(R.drawable.cell_border));
            tableRow.addView(statusTextView);

            // Add the row to the TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private List<AttendanceLog> calculateDurationAndStatus(List<AttendanceLog> logs) {
        List<AttendanceLog> updatedLogs = new ArrayList<>();

        for (AttendanceLog log : logs) {
            try {
                // Extract date from punchInTime (or punchOutTime if punchInTime is null)
                String date = extractDate(log.getPunchInTime() != null ? log.getPunchInTime() : log.getPunchOutTime());
                log.setDate(date); // Set the extracted date in the log

                // Extract time from punchInTime and punchOutTime
                String punchInTime = extractTime(log.getPunchInTime());
                String punchOutTime = extractTime(log.getPunchOutTime());

                // Set the cleaned-up punchInTime and punchOutTime
                log.setPunchInTime(punchInTime);
                log.setPunchOutTime(punchOutTime);

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

    private String extractDate(String timestamp) {
        if (timestamp != null && !timestamp.isEmpty() && timestamp.contains("T")) {
            return timestamp.split("T")[0];
        }
        return "N/A";
    }

    private String extractTime(String timestamp) {
        if (timestamp != null && !timestamp.isEmpty()) {
            String[] parts = timestamp.split("T");
            if (parts.length > 1) {
                return parts[1].split("\\.")[0];
            }
        }
        return "N/A";
    }

    private String calculateDuration(String punchInTime, String punchOutTime) {
        if (punchInTime == null || punchOutTime == null || punchInTime.equals("N/A") || punchOutTime.equals("N/A")) {
            return "N/A";
        }

        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date punchIn = timeFormat.parse(punchInTime);
            Date punchOut = timeFormat.parse(punchOutTime);

            long durationMillis = punchOut.getTime() - punchIn.getTime();
            long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);

            return String.format(Locale.getDefault(), "%d hrs %d mins", durationMinutes / 60, durationMinutes % 60);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private String determineStatus(String duration) {
        if ("N/A".equals(duration) || "Error".equals(duration)) {
            return "N/A";
        }

        try {
            String[] parts = duration.split(" ");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[2]);

            int totalMinutes = hours * 60 + minutes;

            if (totalMinutes >= 90 && totalMinutes <= 180) {
                return "Present";
            } else if (totalMinutes < 90) {
                return "Late";
            } else {
                return "Exceeds Limit";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
