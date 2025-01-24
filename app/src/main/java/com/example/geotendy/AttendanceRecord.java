package com.example.geotendy;

public class AttendanceRecord {
    private String registration_no;
    private String email;
    private String first_name;
    private String unitCode;
    private String unitName;
    private String action;
    private long timestamp;
    private int classesHeld;
    private int classesAttended;

    public AttendanceRecord(String registration_no, String email, String first_name, String unitCode, String unitName, String action, long timestamp) {
        this.registration_no = registration_no;
        this.email = email;
        this.first_name = first_name;
        this.unitCode = unitCode;
        this.unitName = unitName;
        this.action = action;
        this.timestamp = timestamp;
    }

    // Getters and setters for each field (if needed)
    public float getAttendancePercentage() {
        if (classesHeld == 0) {
            return 0; // Avoid division by zero
        }
        return ((float) classesAttended / classesHeld) * 100;
    }
    // Getter for unitName
    public String getUnitName() {
        return unitName;
    }
    // Example setters
    public void setClassesHeld(int classesHeld) {
        this.classesHeld = classesHeld;
    }
    public void setClassesAttended(int classesAttended) {
        this.classesAttended = classesAttended;
    }
}
