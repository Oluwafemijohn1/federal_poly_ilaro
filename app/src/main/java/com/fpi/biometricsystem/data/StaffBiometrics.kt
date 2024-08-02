package com.fpi.biometricsystem.data

import com.google.gson.annotations.SerializedName

data class StaffBiometric(
    @SerializedName("data")
    val `data`: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("staff_id")
    val staffId: String,
    @SerializedName("type")
    val type: String
)