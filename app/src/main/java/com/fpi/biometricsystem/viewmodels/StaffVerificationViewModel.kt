package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.Message
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.StaffData
import com.fpi.biometricsystem.data.individual.StaffInfoResponse
import com.fpi.biometricsystem.data.local.models.StaffInfo
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.data.repository.StaffRepository
import com.fpi.biometricsystem.data.request.StaffAttendanceRequest
import com.fpi.biometricsystem.utils.SingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffVerificationViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
) : ViewModel() {
    private val _allStaffs = MutableLiveData<GenericResponse<List<StaffData>>?>()
    val allStaffs get() = staffRepository.allStaffUpdateFlow()

    private val _Generic_response = MutableLiveData<SingleEvent<GenericResponse<Any>?>>()
    val finalResponse: LiveData<SingleEvent<GenericResponse<Any>?>> get() = _Generic_response

    private val _staffInfo = MutableLiveData<SingleEvent<GenericResponse<StaffInfoResponse>?>>()
    val staffInfo: LiveData<SingleEvent<GenericResponse<StaffInfoResponse>?>> get() = _staffInfo

    private val _errorResponse: MutableLiveData<SingleEvent<GenericError?>> = MutableLiveData()
    val errorResponse: LiveData<SingleEvent<GenericError?>> get() = _errorResponse

    private val _messageResponseLiveData: MutableLiveData<SingleEvent<Message>> = MutableLiveData()
    val messageResponseLiveData: LiveData<SingleEvent<Message>> = _messageResponseLiveData

    fun allUsers() = viewModelScope.launch { staffRepository.getAllUsers() }

    fun markAttendance(request: StaffAttendanceRequest) {
        viewModelScope.launch {
            val result = staffRepository.markStaffAttendance(request)
            if (result.isSuccessful) {
                _Generic_response.postValue(SingleEvent(result.data?.body()))
            } else {
                val errorBody = result.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun fetchStaffInfo(fileNo: String) {
        viewModelScope.launch {
            val result = staffRepository.fetchStaffInfo(fileNo)
            if (result.isSuccessful) {
                _staffInfo.postValue(SingleEvent(result.data?.body()))
            } else {
                val errorBody = result.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun getUser(userId: String): LiveData<StaffInfo?> = staffRepository.getUser(userId)
}