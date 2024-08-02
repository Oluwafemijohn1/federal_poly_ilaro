package com.fpi.biometricsystem.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fpi.biometricsystem.data.StudentDataInfo

@Entity(tableName = "student_table")
data class StudentInfo(
    @PrimaryKey
    val idNo: String,
    val matricNo: String,
    val name: String,
    val department: String,
    val level: String,
    val studentType: String,
    val fingerprint: List<String>,
)

fun StudentDataInfo.toStudentInfo(): StudentInfo {
    return  StudentInfo(
        idNo = id,
        matricNo = matricnumber,
        name = "$firstname $lastname",
        department = departmentId,
        level = levelId,
        studentType = if (levelId.startsWith("P")) "PART-TIME" else "FULL-TIME",
        fingerprint = studentBiometrics.map { biometrics-> biometrics.data }
    )
}