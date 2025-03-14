package com.example.geotendy;

import com.google.gson.annotations.SerializedName;

public class StudentAttendance {
    @SerializedName("registration_no")
    private String registrationNo;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("email")
    private String email;

    @SerializedName("time") // Punch-in time
    private String punchInTime;

    public String getFullName() {
        return (firstName != null ? firstName : "Unknown");
    }

    public String getRegistrationNo() {
        return registrationNo != null ? registrationNo : "N/A";
    }

    public String getEmail() {
        return email != null ? email : "N/A";
    }

    public String getPunchInTime() {
        return punchInTime != null ? punchInTime : "N/A";
    }
}

