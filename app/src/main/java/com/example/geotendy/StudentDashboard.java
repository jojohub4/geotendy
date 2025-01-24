package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Calendar;

public class StudentDashboard extends AppCompatActivity {

    TextView Firstname , greetingText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);

        // Force hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Firstname = findViewById(R.id.textViewProfileName);

        //retrieving first name
        // Retrieving first name from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String first_name = sharedPreferences.getString("first_name", null);

        // Displaying the first name
        Firstname.setText(first_name);

        //dynamic greeting
        greetingText = findViewById(R.id.textViewGreeting);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour>= 0 && hour < 12){
            greeting="Good Morning";
        } else if (hour >= 12 && hour < 18) {
            greeting="Good Afternoon";
        }else {
            greeting="Good Evening";
        }
        greetingText.setText(greeting);

        //logging out
        CardView exit = findViewById(R.id.cardLogout);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                startActivity(new Intent(StudentDashboard.this,LoginActivity.class));
            }
        });
        //directing the user to the unit page
        CardView unit = findViewById(R.id.cardUnits);
        unit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StudentDashboard.this,Units.class));
            }
        });
        //directing the user to the notification page
        CardView notification = findViewById(R.id.cardnotification);
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StudentDashboard.this,Units.class));
            }
        });
        //directing the user to the reports page
        CardView report = findViewById(R.id.cardReports);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StudentDashboard.this,Units.class));
            }
        });
        //directing the user to the location page
        CardView location = findViewById(R.id.cardLocation);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StudentDashboard.this,LocationActivity.class));
            }
        });
        //directing the user to the support page
        CardView support = findViewById(R.id.cardSupport);
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StudentDashboard.this,Units.class));
            }
        });
    }
}