package com.fpi.biometricsystem.data.request


import com.google.gson.annotations.SerializedName

data class StudentAttendanceRequest(
    @SerializedName("lecture_code")
    val lectureCode: String,
    @SerializedName("student_id")
    val studentId: String
)