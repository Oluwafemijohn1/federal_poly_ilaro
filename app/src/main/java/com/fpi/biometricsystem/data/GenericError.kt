package com.fpi.biometricsystem.data

import com.google.gson.annotations.SerializedName

data class GenericError(
    @SerializedName("message")
    val message: String
)
