package com.example.geotendy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceLog> attendanceLogs;

    public AttendanceAdapter(List<AttendanceLog> attendanceLogs) {
        this.attendanceLogs = attendanceLogs;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_table_row, parent, false); // Create this layout
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceLog log = attendanceLogs.get(position);

        holder.dateTextView.setText(log.getDate() != null ? log.getDate() : "N/A");
        holder.inTextView.setText(log.getPunchInTime() != null ? log.getPunchInTime() : "N/A");
        holder.outTextView.setText(log.getPunchOutTime() != null ? log.getPunchOutTime() : "N/A");
        holder.durationTextView.setText(log.getDuration() != null ? log.getDuration() : "N/A");
        holder.unitCodeTextView.setText(log.getUnitCode() != null ? log.getUnitCode() : "N/A");
        holder.unitNameTextView.setText(log.getUnitName() != null ? log.getUnitName() : "N/A");
        holder.statusTextView.setText(log.getStatus() != null ? log.getStatus() : "N/A");
    }

    @Override
    public int getItemCount() {
        return attendanceLogs.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, inTextView, outTextView, durationTextView, unitCodeTextView, unitNameTextView, statusTextView;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind TextViews for the table columns
            dateTextView = itemView.findViewById(R.id.text_date);
            inTextView = itemView.findViewById(R.id.text_in);
            outTextView = itemView.findViewById(R.id.text_out);
            durationTextView = itemView.findViewById(R.id.text_duration);
            unitCodeTextView = itemView.findViewById(R.id.text_unit_code);
            unitNameTextView = itemView.findViewById(R.id.text_unit_name);
            statusTextView = itemView.findViewById(R.id.text_status);
        }
    }
}
