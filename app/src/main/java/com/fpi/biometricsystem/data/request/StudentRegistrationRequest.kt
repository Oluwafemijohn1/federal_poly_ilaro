package com.fpi.biometricsystem.data.request


import com.google.gson.annotations.SerializedName

data class StudentRegistrationRequest(
    @SerializedName("biometric")
    val biometric: List<Biometric>,
    @SerializedName("matric_number")
    val matricnumber: String
)