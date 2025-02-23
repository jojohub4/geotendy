package com.example.geotendy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<StudentAttendance> attendanceLogs;
    private final int totalStudents;
    private final int presentStudents;
    private final int absentStudents;

    public AttendanceSummaryAdapter(List<StudentAttendance> attendanceLogs, int total, int present, int absent) {
        this.attendanceLogs = attendanceLogs;
        this.totalStudents = total;
        this.presentStudents = present;
        this.absentStudents = absent;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attendance_summary, parent, false);
            return new SummaryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attendance_table_row, parent, false);
            return new AttendanceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            ((SummaryViewHolder) holder).totalTextView.setText("Total: " + totalStudents);
            ((SummaryViewHolder) holder).presentTextView.setText("Present: " + presentStudents);
            ((SummaryViewHolder) holder).absentTextView.setText("Absent: " + absentStudents);
        } else {
            StudentAttendance log = attendanceLogs.get(position - 1);
            AttendanceViewHolder attendanceHolder = (AttendanceViewHolder) holder;

            attendanceHolder.nameTextView.setText(log.getFullName());
            attendanceHolder.statusTextView.setText(log.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return attendanceLogs.size() + 1; // +1 for the summary header
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView totalTextView, presentTextView, absentTextView;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            totalTextView = itemView.findViewById(R.id.text_total);
            presentTextView = itemView.findViewById(R.id.text_present);
            absentTextView = itemView.findViewById(R.id.text_absent);
        }
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, statusTextView;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_student_name);
            statusTextView = itemView.findViewById(R.id.text_status);
        }
    }
}
