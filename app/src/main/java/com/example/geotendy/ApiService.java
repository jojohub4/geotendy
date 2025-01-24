package com.example.geotendy;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;

public interface ApiService {
    @POST("/api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("/api/attendance/record")
    Call<Void> recordAttendance(@Body AttendanceRecord attendanceRecord);
    @GET("/api/attendance/data")
    Call<List<AttendanceRecord>> getAttendanceData(); // Fetch attendance data
}
