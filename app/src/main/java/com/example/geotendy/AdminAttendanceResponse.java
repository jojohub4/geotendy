package com.example.geotendy;

import java.util.List;

public class AdminAttendanceResponse {
    private boolean success;
    private List<AdminAttendanceLogs> logs;

    public boolean isSuccess() {
        return success;
    }

    public List<AdminAttendanceLogs> getLogs() {
        return logs;
    }
}
