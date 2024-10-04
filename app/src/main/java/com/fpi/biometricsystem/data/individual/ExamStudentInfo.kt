package com.fpi.biometricsystem.data.individual


import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class ExamStudentInfo(
    var course_code: String?, // STA III
    var course_name: String?, // ICT
    var created_at: String?, // 2024-10-02T08:49:29.000000Z
    var department_id: Any?, // null
    var duration: String?, // 2 hours
    var exam_date: String?, // 2024-10-02 00:00:00
    var exam_number: String?, // 319685
    var id: Int?, // 3
    var level_id: Any?, // null
    var semester_id: Int?, // 2
    var session_id: Int?, // 2
    var staff_id: Int?, // 27
    var updated_at: String? // 2024-10-02T08:49:29.000000Z
): Serializable