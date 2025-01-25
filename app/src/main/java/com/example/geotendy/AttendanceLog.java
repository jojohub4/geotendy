package com.example.geotendy;

public class AttendanceLog {
    private String date;
    private String unitCode;
    private String unitName;
    private String punchInTime;
    private String punchOutTime;
    private String duration; // Calculated field
    private String status;   // Calculated field

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

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

