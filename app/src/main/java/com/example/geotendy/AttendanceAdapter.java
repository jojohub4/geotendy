package com.example.geotendy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<StudentAttendance> attendanceLogs;

    public AttendanceAdapter(List<StudentAttendance> attendanceLogs) {
        this.attendanceLogs = attendanceLogs;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_table_row, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        StudentAttendance log = attendanceLogs.get(position);

        holder.nameTextView.setText(log.getFullName() != null ? log.getFullName() : "N/A");
        holder.regNoTextView.setText(log.getRegistrationNo() != null ? log.getRegistrationNo() : "N/A");
        holder.emailTextView.setText(log.getEmail() != null ? log.getEmail() : "N/A");
        holder.punchInTextView.setText(log.getPunchInTime() != null ? log.getPunchInTime() : "N/A");
    }

    @Override
    public int getItemCount() {
        return attendanceLogs.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, regNoTextView, emailTextView, punchInTextView;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind TextViews to correct layout IDs
            nameTextView = itemView.findViewById(R.id.text_name);
            regNoTextView = itemView.findViewById(R.id.text_reg_no);
            emailTextView = itemView.findViewById(R.id.text_email);
            punchInTextView = itemView.findViewById(R.id.text_punch_in);
        }
    }
}
