package com.example.geotendy;

import java.util.List;

public class LoginResponse {
    private boolean success;
    private String first_name;
    private String second_name;
    private String level;
    private String course;
    private int yr;
    private int semester;
    private String registration_no;
    private String email;
    private List<CourseUnit> courses; // Ensure this matches the response structure

    // Getters for all fields
    public boolean isSuccess() {
        return success;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getSecond_name() {
        return second_name;
    }

    public String getLevel() {
        return level;
    }

    public String getCourse() {
        return course;
    }

    public int getYr() {
        return yr;
    }

    public int getSemester() {
        return semester;
    }

    public List<CourseUnit> getCourses() {
        return courses;
    }

    public String getRegistration_no() {return registration_no;}

    public String getEmail() {return email;}
}
