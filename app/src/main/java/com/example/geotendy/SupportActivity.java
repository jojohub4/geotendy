package com.example.geotendy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportActivity extends AppCompatActivity {
    private EditText etDescription;
    private Button btnSubmit;
    private String userEmail, userName;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        etDescription = findViewById(R.id.editTextText2);
        btnSubmit = findViewById(R.id.button3);

        // Retrieve user details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("email", "Unknown User");
        userName = sharedPreferences.getString("first_name", "Unknown");

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        btnSubmit.setOnClickListener(v -> sendSupportRequest());
    }

    private void sendSupportRequest() {
        String issue = etDescription.getText().toString().trim();
        if (issue.isEmpty()) {
            Toast.makeText(this, "Please describe your issue!", Toast.LENGTH_SHORT).show();
            return;
        }

        SupportRequest request = new SupportRequest(userEmail, userName, issue);

        apiService.sendSupportEmail(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SupportActivity.this, "Support request sent successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SupportActivity.this, "Failed to send support request!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("SupportActivity", "Request failed: " + t.getMessage());
                Toast.makeText(SupportActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
