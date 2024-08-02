package com.fpi.biometricsystem.data.repository

import androidx.lifecycle.LiveData
import com.fpi.biometricsystem.data.AllBiometrics
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.SimpleResponse
import com.fpi.biometricsystem.data.StudentDataInfo
import com.fpi.biometricsystem.data.local.StudentDao
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.data.local.models.toStudentInfo
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.data.remote.FpibService
import com.fpi.biometricsystem.data.request.ExamAttendanceRequest
import com.fpi.biometricsystem.data.request.StudentAttendanceRequest
import com.fpi.biometricsystem.data.request.StudentRegistrationRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val service: FpibService,
    private val preferenceStore: PreferenceStore,
) {
    private val lastVisitedPages = preferenceStore.lastVisitedPageStudents
    suspend fun getAllUsers(lastVisitedPage: Int = lastVisitedPages): SimpleResponse<GenericResponse<AllBiometrics<StudentDataInfo>>> {
        return safeApiCall { service.getAllStudents(lastVisitedPage) }.also {
            if (it.isSuccessful) {
                saveStudentsDetails(it.body.actualData)
                preferenceStore.lastVisitedPageStudents = lastVisitedPage
                if (it.body.actualData.currentPage < it.body.actualData.pagesCount && it.body.actualData.actualData.isNotEmpty()) {
                    getAllUsers(it.body.actualData.currentPage + 1)
                }
            }
        }
    }

    private suspend fun saveStudentsDetails(allBiometrics: AllBiometrics<StudentDataInfo>) {
        val studentsInfo = allBiometrics.actualData.map { it.toStudentInfo() }
        studentDao.insertAllStudents(studentsInfo)
    }

    fun allStudentsUpdateFlow(): Flow<List<StudentInfo>> {
        return studentDao.getAllStudents()
    }


    suspend fun updateStudentInfo(studentInfo: StudentInfo) {
        studentDao.insert(studentInfo)
    }

    suspend fun fetchStudent(matricNo: String) =
        safeApiCall { service.fetchStudentDetails(matricNo) }

    suspend fun registerStudent(request: StudentRegistrationRequest) =
        safeApiCall { service.registerStudent(request) }


    suspend fun markStudentAttendance(request: StudentAttendanceRequest) =
        safeApiCall { service.markStudentAttendance(request) }

    suspend fun markExamAttendance(request: ExamAttendanceRequest) =
        safeApiCall { service.markExamAttendance(request) }


    fun getUser(userId: String): LiveData<StudentInfo?> = studentDao.getSingleStudent(userId)

    suspend fun clearDatabase () {
        studentDao.deleteAllStudents()
    }
}