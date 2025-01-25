package com.example.geotendy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceLog> attendanceLogs;

    public AttendanceAdapter(List<AttendanceLog> attendanceLogs) {
        this.attendanceLogs = attendanceLogs;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_log, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceLog log = attendanceLogs.get(position);
        holder.textDate.setText("Date: " + log.getDate());
        holder.textUnitCode.setText("Unit: " + log.getUnitCode());
        holder.textUnitName.setText("Unit: " + log.getUnitName());
        holder.textPunchInTime.setText("Punch In: " + log.getPunchInTime());
        holder.textPunchOutTime.setText("Punch Out: " + log.getPunchOutTime());
        holder.textDuration.setText("Duration: " + log.getDuration());
        holder.textStatus.setText("Status: " + log.getStatus());

    }

    @Override
    public int getItemCount() {
        return attendanceLogs.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        public TextView textDate, textUnitName, textUnitCode, textPunchInTime, textPunchOutTime, textDuration, textStatus;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textUnitName = itemView.findViewById(R.id.textUnitName);
            textUnitCode = itemView.findViewById(R.id.textUnitCode);
            textPunchInTime = itemView.findViewById(R.id.textPunchInTime);
            textPunchOutTime = itemView.findViewById(R.id.textPunchOutTime);
            textDuration = itemView.findViewById(R.id.textDuration);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
