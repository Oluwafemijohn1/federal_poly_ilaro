package com.fpi.biometricsystem.data


import com.google.gson.annotations.SerializedName

data class GenericResponse<T>(
    @SerializedName("data")
    val actualData: T,
    @SerializedName("message")
    val message: String
)