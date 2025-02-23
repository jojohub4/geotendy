package com.example.geotendy;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendanceSummaryResponse {
    @SerializedName("unitCode")
    private String unitCode;

    @SerializedName("unitName")
    private String unitName;

    @SerializedName("presentStudents")
    private int presentStudents;

    @SerializedName("absentStudents")
    private int absentStudents;

    @SerializedName("totalStudents")
    private int totalStudents;

    @SerializedName("students")
    private List<StudentAttendance> students; // List of students

    public String getUnitCode() {
        return unitCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public int getPresentStudents() {
        return presentStudents;
    }

    public int getAbsentStudents() {
        return absentStudents;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public List<StudentAttendance> getStudents() {
        return students;
    }
}
