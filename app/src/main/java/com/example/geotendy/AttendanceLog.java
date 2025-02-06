package com.example.geotendy;

public class AttendanceLog {
    private String date;         // Extracted date from punchInTime
    private String punchInDate; // New field for punch-in date
    private String punchOutDate; // New field for punch-out date
    private String unit_code;
    private String unit_name;
    private String punchInTime;  // Extracted time (HH:mm:ss)
    private String punchOutTime; // Extracted time (HH:mm:ss)
    private String duration;     // Calculated duration
    private String status;       // Calculated status

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPunchInDate() { return punchInDate; }
    public void setPunchInDate(String punchInDate) { this.punchInDate = punchInDate; }

    public String getPunchOutDate() { return punchOutDate; }
    public void setPunchOutDate(String punchOutDate) { this.punchOutDate = punchOutDate; }

    public String getUnitCode() { return unit_code; }
    public void setUnitCode(String unitCode) { this.unit_code = unitCode; }

    public String getUnitName() { return unit_name; }
    public void setUnitName(String unitName) { this.unit_name = unitName; }

    public String getPunchInTime() { return punchInTime; }
    public void setPunchInTime(String punchInTime) { this.punchInTime = punchInTime; }

    public String getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(String punchOutTime) { this.punchOutTime = punchOutTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
