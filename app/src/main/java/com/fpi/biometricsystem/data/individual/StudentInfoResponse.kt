package com.fpi.biometricsystem.data.individual


import androidx.annotation.Keep

@Keep
data class StudentBiometric(
    var created_at: String?, // 2024-02-15T11:53:09.000000Z
    var `data`: String?, //
    var id: Int?, // 10241
    var student_id: Int?, // 17105
    var type: String?, // thumb
    var updated_at: String? // 2024-02-15T11:53:09.000000Z
)
@Keep
data class Level(
    var created_at: String?, // 2023-07-21T02:03:03.000000Z
    var id: Int?, // 5
    var level: String?, // HND I
    var updated_at: String? // 2023-07-21T02:03:03.000000Z
)

@Keep
data class Department(
    var created_at: String?, // 2023-09-19T15:00:40.000000Z
    var department: String?, // Marketing
    var id: Int?, // 30
    var school_id: Int?, // 3
    var updated_at: String? // 2023-09-19T15:00:40.000000Z
)

@Keep
data class StudentInfoResponse(
    var changed: Int?, // 0
    var created_at: String?, // 2023-12-18T09:12:24.000000Z
    var department: Department?,
    var department_id: Int?, // 30
    var firstname: String?, // MIKAIL
    var id: Int?, // 17105
    var lastname: String?, // LAMIDI
    var level: Level?,
    var level_id: Int?, // 5
    var matricnumber: String?, // HMKT233345
    var student_biometrics: List<StudentBiometric?>?,
    var updated_at: String? // 2023-12-18T09:12:24.000000Z
)