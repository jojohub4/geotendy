package com.example.geotendy;

public class AdminAttendanceLogs {
    private String date;
    private String time;
    private String action;

    public AdminAttendanceLogs(String date, String time, String action) {
        this.date = date;
        this.time = time;
        this.action = action;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getAction() {
        return action;
    }
}
