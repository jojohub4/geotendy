package com.example.geotendy;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;


public class ReportActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private ApiService apiService;
    private EditText etFromDate, etToDate;
    private Button btnFetchAttendance;
    private String fromDate = "", toDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize UI elements
        tableLayout = findViewById(R.id.tableLayout);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        btnFetchAttendance = findViewById(R.id.button2);

        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Set up date pickers
        etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        etToDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Set up button click listener
        btnFetchAttendance.setOnClickListener(v -> {
            if (fromDate.isEmpty() || toDate.isEmpty()) {
                Toast.makeText(ReportActivity.this, "Please select both dates", Toast.LENGTH_SHORT).show();
            } else {
                fetchFilteredAttendance();
            }
        });
    }

    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Convert to YYYY-MM-DD format
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, (month + 1), dayOfMonth);

                    if (isFromDate) {
                        fromDate = selectedDate;
                        etFromDate.setText(selectedDate); // Update input field
                    } else {
                        toDate = selectedDate;
                        etToDate.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }


    private void fetchFilteredAttendance() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String registrationNo = sharedPreferences.getString("registration_no", null);

        if (email == null || registrationNo == null) {
            Toast.makeText(this, "User data missing, please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("API Request", "Sending Request: " +
                "Email=" + email +
                ", RegNo=" + registrationNo +
                ", From=" + fromDate +
                ", To=" + toDate);

        apiService.fetchFilteredAttendance(email, registrationNo, fromDate, toDate)
                .enqueue(new Callback<AttendanceResponse>() {
                    @Override
                    public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                        Log.d("API Response", "Response Code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("API Response", "Full Response: " + new Gson().toJson(response.body()));  // 👈 Log full response

                            List<AttendanceLog> logs = response.body().getLogs(); // Extract logs
                            Log.d("API Response", "Received logs count: " + logs.size());

                            if (logs.isEmpty()) {
                                Toast.makeText(ReportActivity.this, "No attendance logs found", Toast.LENGTH_SHORT).show();
                            } else {
                                populateTable(logs);
                            }
                        } else {
                            Log.e("API Error", "Response error: " + response.message());
                            Toast.makeText(ReportActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                        Toast.makeText(ReportActivity.this, "Error fetching data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("API Failure", "Request failed: " + t.getMessage());
                    }
                });
    }

    private void populateTable(List<AttendanceLog> attendanceLogs) {
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1)); // Clears only dynamic rows

        for (AttendanceLog log : attendanceLogs) {
            TableRow tableRow = new TableRow(this);

            

            // Add columns
            addTextViewToRow(tableRow, log.getDate());
            addTextViewToRow(tableRow, log.getUnitCode());
            addTextViewToRow(tableRow, log.getUnitName());
            addTextViewToRow(tableRow, log.getPunchInTime());
            addTextViewToRow(tableRow, log.getPunchOutTime());
            addTextViewToRow(tableRow, log.getDuration());
            addTextViewToRow(tableRow, log.getStatus());

            tableLayout.addView(tableRow);
        }
    }



    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(this);

        // 🔹 Convert date format if it's in ISO 8601
        if (text != null && text.contains("T")) {
            text = formatDate(text);  // Convert to readable format
        }

        textView.setText(text != null && !text.isEmpty() ? text : "N/A");  // 🔹 Ensure no blank values
        textView.setPadding(8, 8, 8, 8);
        textView.setBackground(getResources().getDrawable(R.drawable.cell_border));
        textView.setSingleLine(true);
        textView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color to black
        row.addView(textView);

    }

    // 🔹 Helper method to format date from "2025-02-21T00:00:00.000Z" to "2025-02-21"
    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = isoFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("ReportActivity", "Date parsing error: " + e.getMessage());
            return isoDate;  // Return original if parsing fails
        }
    }
}
