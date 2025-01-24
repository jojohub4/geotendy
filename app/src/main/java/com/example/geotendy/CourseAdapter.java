// CourseAdapter.java
package com.example.geotendy;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<CourseUnit> courseList;
    private Context context;
    private GeofencingClient geofencingClient;

    public CourseAdapter(List<CourseUnit> courseList, Context context) {
        this.courseList = courseList;
        this.context = context;
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseUnit course = courseList.get(position);
        if (course != null) {
            String combinedText = course.getUnitCode() + " - " + course.getUnitName();
            holder.unitDetailsTextView.setText(combinedText);
            Log.d(TAG, "Binding course at position: " + position + ", with data: " + combinedText);
        } else {
            Log.d(TAG, "Course is null at position: " + position);
        }

        // Geofencing check on item click
        holder.itemView.setOnClickListener(v -> {
            checkGeofenceAndProceed(course);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    private void checkGeofenceAndProceed(CourseUnit course) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        boolean isInGeofence = sharedPreferences.getBoolean("isInGeofence", false); // Read flag
        Log.d(TAG, "checkGeofenceAndProceed: isInGeofence = " + isInGeofence);

        if (isInGeofence) {
            Log.d(TAG, "User is within geofence. Proceeding to AttendanceMarking for unit: " + course.getUnitName());
            Intent intent = new Intent(context, AttendanceMarking.class);
            intent.putExtra("unitCode", course.getUnitCode());
            intent.putExtra("unitName", course.getUnitName());
            context.startActivity(intent);
        } else {
            Log.d(TAG, "User is outside geofence. Access denied for unit: " + course.getUnitName());
            Toast.makeText(context, "Please be in the required location to mark attendance", Toast.LENGTH_LONG).show();
        }
    }




    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        public TextView unitDetailsTextView;

        public CourseViewHolder(View view) {
            super(view);
            unitDetailsTextView = view.findViewById(R.id.unitDetailsTextView);
        }
    }
}