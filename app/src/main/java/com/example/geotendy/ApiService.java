package com.example.geotendy;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request); // Verify user

    @POST("/api/attendance/record")
    Call<Void> recordAttendance(@Body AttendanceRecord attendanceRecord); // Send attendance to DB

    @GET("api/attendance/logs")
    Call<AttendanceResponse> fetchFilteredAttendance(
            @Query("email") String email,
            @Query("registration_no") String registrationNo,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate
    );

    @GET("/api/lecturer/units")
    Call<LecturerUnitsResponse> getLecturerUnits(@Query("email") String email);

    // ✅ Unified clocking endpoint for both admin and lecturer
    @POST("/api/clocking")
    Call<ApiResponse> clocking(@Body ClockingRequest request);

    @GET("/api/lecturer/attendance")
    Call<LecturerAttendanceResponse> getLecturerAttendance(
            @Query("email") String email,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate
    );

}
