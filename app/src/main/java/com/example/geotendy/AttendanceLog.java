package com.example.geotendy;

public class AttendanceLog {
    private String date;         // Extracted date from punchInTime
    private String punchInDate; // New field for punch-in date
    private String punchOutDate; // New field for punch-out date
    private String unitCode;
    private String unitName;
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

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getPunchInTime() { return punchInTime; }
    public void setPunchInTime(String punchInTime) { this.punchInTime = punchInTime; }

    public String getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(String punchOutTime) { this.punchOutTime = punchOutTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
