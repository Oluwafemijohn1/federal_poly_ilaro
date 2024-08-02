package com.fpi.biometricsystem.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize

data class ExamEvent(
    @SerializedName("course_code")
    val courseCode: String,
    @SerializedName("course_name")
    val courseName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("department_id")
    val departmentId: Int,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("exam_date")
    val examDate: String,
    @SerializedName("exam_number")
    val examNumber: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("level_id")
    val levelId: Int,
    @SerializedName("semester_id")
    val semesterId: Int,
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("staff_id")
    val staffId: Int,
    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable