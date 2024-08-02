package com.fpi.biometricsystem.data


import com.google.gson.annotations.SerializedName

data class StaffData(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("department_id")
    val departmentId: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("filenumber")
    val filenumber: String,
    @SerializedName("firstname")
    val firstname: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("lastname")
    val lastname: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("staff_biometrics")
    val staffBiometrics: List<StaffBiometric>,
    @SerializedName("staff_type")
    val staffType: StaffType,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("department")
    val department: Dept,
)

data class StaffType(
    @SerializedName("type")
    val staffType: String,
)