package com.example.geotendy;

public class LoginRequest {
    private String email;
    private String registration_no; // Update this field name

    public LoginRequest(String email, String registration_no) { // Update parameter name
        this.email = email;
        this.registration_no = registration_no; // Update assignment
    }

    public String getEmail() {
        return email;
    }

    public String getRegistration_no() { // Update getter name
        return registration_no;
    }
}
