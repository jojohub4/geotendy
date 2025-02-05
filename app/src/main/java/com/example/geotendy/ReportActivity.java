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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                fetchFilteredAttendance(); // No parameters, uses global variables
            }
        });
    }

    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", (month + 1), dayOfMonth, year); // MM/DD/YYYY format
                    if (isFromDate) {
                        fromDate = selectedDate;
                        etFromDate.setText(selectedDate);
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

        // Debugging: Log the API parameters before sending request
        Log.d("API Request", "Sending Request: " +
                "Email=" + email +
                ", RegNo=" + registrationNo +
                ", From=" + fromDate +
                ", To=" + toDate);

        apiService.fetchFilteredAttendance(email, registrationNo, fromDate, toDate)
                .enqueue(new Callback<List<AttendanceLog>>() {
                    @Override
                    public void onResponse(Call<List<AttendanceLog>> call, Response<List<AttendanceLog>> response) {
                        Log.d("API Response", "Response Code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            List<AttendanceLog> logs = response.body();
                            if (logs.isEmpty()) {
                                Toast.makeText(ReportActivity.this, "No attendance logs found", Toast.LENGTH_SHORT).show();
                            } else {
                                populateTable(logs);
                            }
                        } else {
                            Toast.makeText(ReportActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                            Log.e("API Error", "Response error: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AttendanceLog>> call, Throwable t) {
                        Toast.makeText(ReportActivity.this, "Error fetching data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("API Failure", "Request failed: " + t.getMessage());
                    }
                });
    }


    private void populateTable(List<AttendanceLog> attendanceLogs) {
        tableLayout.removeAllViews(); // Clear previous data but keep headers

        for (AttendanceLog log : attendanceLogs) {
            TableRow tableRow = new TableRow(this);

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
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setBackground(getResources().getDrawable(R.drawable.cell_border));
        textView.setSingleLine(true);
        row.addView(textView);
    }
}
