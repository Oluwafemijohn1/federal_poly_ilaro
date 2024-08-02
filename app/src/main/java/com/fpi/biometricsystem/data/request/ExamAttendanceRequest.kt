package com.fpi.biometricsystem.data.request

import com.google.gson.annotations.SerializedName

data class ExamAttendanceRequest(
    @SerializedName("exam_number")
    val examNumber: String,
    @SerializedName("student_id")
    val studentId: String
)
