package com.fpi.biometricsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fpi.biometricsystem.data.local.models.StaffInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_table ORDER BY idNo ASC")
    fun getAllStaff(): Flow<List<StaffInfo>>

    @Query("SELECT * FROM staff_table WHERE idNo = :id LIMIT 1")
    fun getSingleStaff(id: String): LiveData<StaffInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staffInfo: StaffInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStaff(allStaffInfo: List<StaffInfo>)

    @Query("DELETE FROM staff_table")
    suspend fun deleteAllStaff()
}