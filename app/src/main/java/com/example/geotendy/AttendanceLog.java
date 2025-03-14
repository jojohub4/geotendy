package com.example.geotendy;

import com.google.gson.annotations.SerializedName;

public class AttendanceLog {
    @SerializedName("date")
    private String date;

    @SerializedName("unitCode")
    private String unitCode;

    @SerializedName("unitName")
    private String unitName;

    @SerializedName("punchInTime")
    private String punchInTime;

    @SerializedName("punchOutTime")
    private String punchOutTime;

    @SerializedName("duration")
    private String duration;

    @SerializedName("status")
    private String status;

    public String getDate() { return date != null ? date : "N/A"; }
    public String getUnitCode() { return unitCode != null ? unitCode : "N/A"; }
    public String getUnitName() { return unitName != null ? unitName : "N/A"; }
    public String getPunchInTime() { return punchInTime != null ? punchInTime : "N/A"; }
    public String getPunchOutTime() { return punchOutTime != null ? punchOutTime : "N/A"; }
    public String getDuration() { return duration != null ? duration : "N/A"; }
    public String getStatus() { return status != null ? status : "N/A"; }
}
