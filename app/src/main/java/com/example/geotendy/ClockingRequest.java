package com.example.geotendy;

public class ClockingRequest {
    private String registration_no;
    private String email;
    private String first_name;
    private String role;  // ✅ NEW: To differentiate between admin and lecturer
    private String action;
    private long timestamp;

    public ClockingRequest(String registration_no, String email, String first_name, String role, String action, long timestamp) {
        this.registration_no = registration_no;
        this.email = email;
        this.first_name = first_name;
        this.role = role;  // ✅ Now we pass "admin" or "lecturer"
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public String getEmail() {
        return email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getRole() {
        return role;
    }

    public String getAction() {
        return action;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
