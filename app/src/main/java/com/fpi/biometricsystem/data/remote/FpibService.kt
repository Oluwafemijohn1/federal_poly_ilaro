package com.fpi.biometricsystem.data.remote

import com.fpi.biometricsystem.data.AllBiometrics
import com.fpi.biometricsystem.data.BaseUrlResponse
import com.fpi.biometricsystem.data.EventInfo
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.Lecture
import com.fpi.biometricsystem.data.LectureInfo
import com.fpi.biometricsystem.data.StaffData
import com.fpi.biometricsystem.data.StudentDataInfo
import com.fpi.biometricsystem.data.individual.ExamStudentInfo
import com.fpi.biometricsystem.data.individual.StaffInfoResponse
import com.fpi.biometricsystem.data.individual.StudentInfoResponse
import com.fpi.biometricsystem.data.request.ExamAttendanceRequest
import com.fpi.biometricsystem.data.request.StaffAttendanceRequest
import com.fpi.biometricsystem.data.request.StaffRegistrationRequest
import com.fpi.biometricsystem.data.request.StudentAttendanceRequest
import com.fpi.biometricsystem.data.request.StudentRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FpibService {
    @GET("staff/fetch")
    suspend fun fetchStaffDetails(@Query("filenumber") staffId: String): Response<GenericResponse<StaffData>>

    @GET("student/fetch")
    suspend fun fetchStudentDetails(@Query("matric_number") studentId: String): Response<GenericResponse<StudentDataInfo>>

    @GET("staff")
    suspend fun getAllStaff(@Query("page") lastPageNumber: Int = 1): Response<GenericResponse<AllBiometrics<StaffData>>>

    @GET("student")
    suspend fun getAllStudents(@Query("page") lastPageNumber: Int = 1): Response<GenericResponse<AllBiometrics<StudentDataInfo>>>

    @GET("events")
    suspend fun fetchEvents(): Response<GenericResponse<List<EventInfo>>>

    @GET("exam/{examNum}")
    suspend fun fetchExam(@Path("examNum") examNum: String): Response<GenericResponse<ExamStudentInfo>>

    @GET("events/{id}")
    suspend fun fetchEventById(@Path("id") id: String): Response<GenericResponse<EventInfo>>

    @GET("events")
    suspend fun fetchLectures(): Response<GenericResponse<List<LectureInfo>>>
//    @GET("settings/base_url")
//    suspend fun fetchBaseUrl(): Response<GenericResponse<BaseUrlResponse>>

    @GET("departments")
    suspend fun fetchBaseUrl(): Response<GenericResponse<Department>>
    @GET("course/lecture")
    suspend fun fetchCourseById(@Query("lectureid") id: String): Response<GenericResponse<List<Lecture>>>

    //    https://attendance.federalpolyilaro.edu.ng/api/course?courseid=55755
    @GET("course/lectures")
    suspend fun fetchCourseByCode(@Query("courseid") id: String): Response<GenericResponse<List<Lecture>>>

    @POST("attendance/staff")
    suspend fun markStaffAttendance(@Body request: StaffAttendanceRequest) : Response<GenericResponse<Any>>

    @POST("attendance/student")
    suspend fun markStudentAttendance(@Body request: StudentAttendanceRequest) : Response<GenericResponse<Any>>

    @POST("exam/attendance")
    suspend fun markExamAttendance(@Body request: ExamAttendanceRequest) : Response<GenericResponse<Any>>

    @PATCH("staff")
    suspend fun registerStaff(@Body request: StaffRegistrationRequest) : Response<GenericResponse<Any>>

    @PATCH("student")
    suspend fun registerStudent(@Body request: StudentRegistrationRequest) : Response<GenericResponse<Any>>
    @GET("student/fetch")
    suspend fun fetchStudentInfo(@Query("matric_number") matric_number: String): Response<GenericResponse<StudentInfoResponse>>

    @GET("staff/fetch")
    suspend fun fetchStaffInfo(@Query("filenumber") filenumber: String): Response<GenericResponse<StaffInfoResponse>>

}