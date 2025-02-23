package com.example.geotendy;

public class LoginRequest {
    private String email;
    private String registration_no;
    private String device_id; // Added device_id

    public LoginRequest(String email, String registration_no) {
        this.email = email;
        this.registration_no = registration_no;
        this.device_id = device_id; // Assign device_id
    }

    public String getEmail() {
        return email;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public String getDevice_id() { // Getter for device_id
        return device_id;
    }
}
