package com.fpi.biometricsystem.data.request


import com.google.gson.annotations.SerializedName

data class StaffRegistrationRequest(
    @SerializedName("biometric")
    val biometric: List<Biometric>,
    @SerializedName("filenumber")
    val filenumber: String
)