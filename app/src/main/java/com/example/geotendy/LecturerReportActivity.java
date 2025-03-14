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
import java.util.Date;

public class LecturerReportActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private ApiService apiService;
    private EditText etFromDate, etToDate;
    private Button btnFetchAttendance;
    private String fromDate = "", toDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_report);

        // Initialize UI elements
        tableLayout = findViewById(R.id.tableLayout);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        btnFetchAttendance = findViewById(R.id.buttonFetch);

        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Set up date pickers
        etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        etToDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Fetch attendance data on button click
        btnFetchAttendance.setOnClickListener(v -> {
            if (fromDate.isEmpty() || toDate.isEmpty()) {
                Toast.makeText(LecturerReportActivity.this, "Please select both dates", Toast.LENGTH_SHORT).show();
            } else {
                fetchLecturerAttendance();
            }
        });
    }

    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, (month + 1), dayOfMonth);
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

    private void fetchLecturerAttendance() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String email = sharedPreferences.getString("lecturer_email", null);

        if (email == null) {
            Toast.makeText(this, "Lecturer data missing, please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("API Request", "Fetching logs for: " + email + " from " + fromDate + " to " + toDate);

        apiService.getLecturerAttendance(email, fromDate, toDate)
                .enqueue(new Callback<LecturerAttendanceResponse>() {
                    @Override
                    public void onResponse(Call<LecturerAttendanceResponse> call, Response<LecturerAttendanceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            populateTable(response.body().getLogs());
                        } else {
                            Toast.makeText(LecturerReportActivity.this, "No records found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LecturerAttendanceResponse> call, Throwable t) {
                        Toast.makeText(LecturerReportActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateTable(List<LecturerAttendanceLog> logs) {
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1));

        for (LecturerAttendanceLog log : logs) {
            TableRow tableRow = new TableRow(this);
            addTextViewToRow(tableRow, log.getDate());
            addTextViewToRow(tableRow, log.getTime());
            addTextViewToRow(tableRow, log.getAction());
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
