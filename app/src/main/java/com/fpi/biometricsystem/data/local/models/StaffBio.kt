package com.fpi.biometricsystem.data.local.models

data class StaffBio(
    val id: String,
    val name: String,
    val staffId: String,
    val biometrics: List<String>
)
