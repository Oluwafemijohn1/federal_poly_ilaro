package com.fpi.biometricsystem.data.local.models

data class StudentBio(
    val id: String,
    val name: String,
    val matricNo: String,
    val biometrics: List<String>
)
