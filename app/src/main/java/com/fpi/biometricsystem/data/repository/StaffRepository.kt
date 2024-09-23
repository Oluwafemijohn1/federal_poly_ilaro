package com.fpi.biometricsystem.data.repository

import androidx.lifecycle.LiveData
import com.fpi.biometricsystem.data.AllBiometrics
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.SimpleResponse
import com.fpi.biometricsystem.data.StaffData
import com.fpi.biometricsystem.data.local.models.StaffInfo
import com.fpi.biometricsystem.data.local.StaffDao
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.data.local.models.toStaffInfo
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.data.remote.FpibService
import com.fpi.biometricsystem.data.request.StaffAttendanceRequest
import com.fpi.biometricsystem.data.request.StaffRegistrationRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val staffDao: StaffDao,
    private val service: FpibService,
    private val preferenceStore: PreferenceStore,
) {
    private val lastVisitedPages = preferenceStore.lastVisitedPageStaffs
    suspend fun getAllUsers(lastVisitedPage: Int = lastVisitedPages): SimpleResponse<GenericResponse<AllBiometrics<StaffData>>> {
        return safeApiCall { service.getAllStaff(lastVisitedPage) }.also {

            if (it.isSuccessful) {
                saveStaffDetails(it.body.actualData)
                preferenceStore.lastVisitedPageStaffs = lastVisitedPage
                if (it.body.actualData.currentPage < it.body.actualData.pagesCount && it.body.actualData.actualData.isNotEmpty()) {
                    getAllUsers(it.body.actualData.currentPage + 1)
                }
            } else {
                // Pass a message to the front
            }
        }
    }

    private suspend fun saveStaffDetails(allBiometrics: AllBiometrics<StaffData>) {
        val staffsInfo = allBiometrics.actualData.map { it.toStaffInfo() }
        staffDao.insertAllStaff(staffsInfo)
    }

    fun allStaffUpdateFlow(): Flow<List<StaffInfo>> {
        return staffDao.getAllStaff()
    }

    suspend fun insertUser(staffInfo: StaffInfo) {
        staffDao.insert(staffInfo)
    }

    suspend fun getStaff(staffId: String) = safeApiCall { service.fetchStaffDetails(staffId) }
    suspend fun markStaffAttendance(request: StaffAttendanceRequest) =
        safeApiCall { service.markStaffAttendance(request) }

    suspend fun fetchStaffInfo(fileNo: String) =
        safeApiCall { service.fetchStaffInfo(fileNo) }

    suspend fun registerStaff(request: StaffRegistrationRequest) =
        safeApiCall { service.registerStaff(request) }

    fun getUser(userId: String): LiveData<StaffInfo?> = staffDao.getSingleStaff(userId)

    suspend fun clearDatabase() {
        staffDao.deleteAllStaff()
    }
}