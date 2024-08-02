package com.fpi.biometricsystem.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fpi.biometricsystem.data.local.models.StaffInfo

@Entity(tableName = "events_table")
data class Event(
    @PrimaryKey
    val id: String,
    val title: String,
    val attendanceList: List<StaffInfo>,
)
