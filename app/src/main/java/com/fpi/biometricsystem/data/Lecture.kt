package com.fpi.biometricsystem.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Lecture(
    @SerializedName("class")
    val className: String,
    @SerializedName("course_id")
    val courseId: Int,
    @SerializedName("date_time")
    val dateTime: String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("lecture_id")
    val lectureId: String,
    @SerializedName("topic")
    val topic: String
): Parcelable