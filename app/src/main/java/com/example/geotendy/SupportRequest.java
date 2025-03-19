package com.example.geotendy;

public class SupportRequest {
    private String userEmail;
    private String userName;
    private String issue;

    public SupportRequest(String userEmail, String userName, String issue) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.issue = issue;
    }
}
