package com.fpi.biometricsystem.data

import com.google.gson.annotations.SerializedName

data class StudentBiometric(
    @SerializedName("data")
    val `data`: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("student_id")
    val studentId: String,
    @SerializedName("type")
    val type: String
)