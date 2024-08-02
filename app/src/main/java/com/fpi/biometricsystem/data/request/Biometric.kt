package com.fpi.biometricsystem.data.request


import com.google.gson.annotations.SerializedName

data class Biometric(
    @SerializedName("data")
    val `data`: String,
    @SerializedName("type")
    val type: String
)