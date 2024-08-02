package com.fpi.biometricsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fpi.biometricsystem.data.local.models.StudentInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_table ORDER BY idNo ASC")
    fun getAllStudents(): Flow<List<StudentInfo>>

    @Query("SELECT * FROM student_table WHERE idNo = :id LIMIT 1")
    fun getSingleStudent(id: String): LiveData<StudentInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentInfo: StudentInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(allStudentInfo: List<StudentInfo>)

    @Query("DELETE FROM student_table")
    suspend fun deleteAllStudents()
}