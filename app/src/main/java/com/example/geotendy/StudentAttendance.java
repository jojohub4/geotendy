package com.example.geotendy;

import com.google.gson.annotations.SerializedName;

public class StudentAttendance {
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("second_name")
    private String secondName;

    @SerializedName("status")
    private String status;

    public String getFullName() {
        return firstName + " " + secondName;
    }

    public String getStatus() {
        return status;
    }
}
