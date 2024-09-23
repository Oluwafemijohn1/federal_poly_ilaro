package com.fpi.biometricsystem.data


import com.google.gson.annotations.SerializedName

data class StudentDataInfo(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("department_id")
    val departmentId: String,
    @SerializedName("firstname")
    val firstname: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("lastname")
    val lastname: String,
    @SerializedName("level_id")
    val levelId: String,
    @SerializedName("matricnumber")
    val matricnumber: String,
    @SerializedName("student_biometrics")
    val studentBiometrics: List<StudentBiometrics>,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("level")
    val level: Level,
    @SerializedName("department")
    val department: Dept,

    )

data class Level(
    @SerializedName("level")
    val level: String,
)
data class Dept(
    @SerializedName("department")
    val dept: String,
)