package com.fpi.biometricsystem.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fpi.biometricsystem.data.StaffData

@Entity(tableName = "staff_table")
data class StaffInfo(
    @PrimaryKey
    val idNo: String,
    val name: String,
    val department: String,
    val staffType: String,
    val fileNo: String,
    val fingerprint: List<String>,
)

fun StaffData.toStaffInfo(): StaffInfo {
    return StaffInfo(
        idNo = id,
        name = "$firstname $lastname",
        department = departmentId,
        staffType = "Academic",
        fileNo = filenumber,
        fingerprint = staffBiometrics.map { staffBiometric -> staffBiometric.data }
    )
}