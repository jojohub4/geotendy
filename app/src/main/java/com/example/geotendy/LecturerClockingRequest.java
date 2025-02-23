package com.example.geotendy;

import com.google.gson.annotations.SerializedName;

public class LecturerClockingRequest {
    @SerializedName("registration_no")
    private String registrationNo;

    @SerializedName("email")
    private String email;

    @SerializedName("first_name") // ✅ Make sure it matches backend
    private String firstName;

    @SerializedName("action")
    private String action; // "Clock In" or "Clock Out"

    @SerializedName("timestamp")
    private long timestamp; // Current time in milliseconds

    public LecturerClockingRequest(String registrationNo, String email, String firstName, String action, long timestamp) {
        this.registrationNo = registrationNo;
        this.email = email;
        this.firstName = firstName; // ✅ Now matches backend correctly
        this.action = action;
        this.timestamp = timestamp;
    }
}
