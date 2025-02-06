package com.example.geotendy;

import java.util.List;

public class AttendanceResponse {
    private boolean success;
    private List<AttendanceLog> logs; // Matches "logs": [ ... ] in API response

    public boolean isSuccess() {
        return success;
    }

    public List<AttendanceLog> getLogs() {
        return logs;
    }
}
