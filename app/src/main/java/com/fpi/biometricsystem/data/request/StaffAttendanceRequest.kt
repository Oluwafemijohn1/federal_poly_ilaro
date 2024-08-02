package com.fpi.biometricsystem.data.request


import com.google.gson.annotations.SerializedName

data class StaffAttendanceRequest(
    @SerializedName("event_id")
    val eventId: String,
    @SerializedName("staff_id")
    val staffId: String
)