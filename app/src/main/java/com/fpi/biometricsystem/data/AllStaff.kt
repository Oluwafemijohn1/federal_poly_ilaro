package com.fpi.biometricsystem.data

import com.google.gson.annotations.SerializedName

data class AllBiometrics<T>(
    @SerializedName("data")
    val actualData: List<T>,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("last_page")
    val pagesCount: Int
)