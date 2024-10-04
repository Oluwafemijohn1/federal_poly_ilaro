package com.fpi.biometricsystem.data.individual


import androidx.annotation.Keep


@Keep
data class StaffBiometric(
    var created_at: String?, // 2024-08-06T11:28:24.000000Z
    var `data`: String?, //
    var id: Int?, // 3364
    var staff_id: Int?, // 8
    var type: String?, // thumb
    var updated_at: String? // 2024-08-06T11:28:24.000000Z
)

@Keep
data class StaffType(
    var created_at: String?, // 2023-07-21T10:30:49.000000Z
    var id: Int?, // 1
    var type: String?, // Academic
    var updated_at: String? // 2023-07-21T10:30:49.000000Z
)


@Keep
data class StaffInfoResponse(
    var active: Int?, // 0
    var changed: Int?, // 0
    var created_at: String?, // 2023-09-19T15:32:31.000000Z
    var department: Department?,
    var department_id: Int?, // 3
    var email: Any?, // null
    var filenumber: String?, // PSS745
    var firstname: String?, // Mukail
    var id: Int?, // 8
    var lastname: String?, // Akinde
    var level: String?, // 0
    var otp: String?, // 55555
    var password: Any?, // null
    var staff_biometrics: List<StaffBiometric?>?,
    var staff_type: StaffType?,
    var staff_type_id: Int?, // 1
    var updated_at: String?, // 2024-06-11T08:39:06.000000Z
    var uuid: String? // 222b0d6a-f2f7-4f8d-a3d0-b9d67525cae0
)