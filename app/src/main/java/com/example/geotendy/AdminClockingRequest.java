package com.example.geotendy;

public class AdminClockingRequest {
    private String registration_no;
    private String email;
    private String first_name;
    private String action;
    private long timestamp;

    public AdminClockingRequest(String registration_no, String email, String first_name, String action, long timestamp) {
        this.registration_no = registration_no;
        this.email = email;
        this.first_name = first_name;
        this.action = action;
        this.timestamp = timestamp;
    }

    // Getters
    public String getRegistrationNo() {
        return registration_no;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getAction() {
        return action;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
