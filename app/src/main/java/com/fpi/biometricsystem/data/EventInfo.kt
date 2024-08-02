package com.fpi.biometricsystem.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class EventInfo(
    @SerializedName("date")
    val date: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("event_number")
    val eventNumber: String,
    @SerializedName("host")
    val host: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("location")
    val location: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("title")
    val title: String
): Parcelable