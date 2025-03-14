package com.example.geotendy;

import java.util.List;

public class LecturerAttendanceResponse {
    private boolean success;
    private List<LecturerAttendanceLog> logs;

    public boolean isSuccess() {
        return success;
    }

    public List<LecturerAttendanceLog> getLogs() {
        return logs;
    }
}
