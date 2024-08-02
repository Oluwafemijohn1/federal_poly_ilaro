package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.Message
import com.fpi.biometricsystem.data.MessageType
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.StaffData
import com.fpi.biometricsystem.data.local.models.StaffInfo
import com.fpi.biometricsystem.data.repository.StaffRepository
import com.fpi.biometricsystem.data.request.StaffRegistrationRequest
import com.fpi.biometricsystem.utils.SingleEvent
import com.fpi.biometricsystem.utils.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffRegistrationViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
) : ViewModel() {
    private val _messageResponseLiveData: MutableLiveData<SingleEvent<Message>> = MutableLiveData()
    val messageResponseLiveData: LiveData<SingleEvent<Message>> = _messageResponseLiveData

    private val _Generic_response = MutableLiveData<SingleEvent<GenericResponse<StaffData>?>>()
    val finalResponse: LiveData<SingleEvent<GenericResponse<StaffData>?>> get() = _Generic_response

    private val _errorResponse: MutableLiveData<SingleEvent<GenericError?>> = MutableLiveData()
    val errorResponse: LiveData<SingleEvent<GenericError?>> get() = _errorResponse
    fun createUser(staffRegistrationRequest: StaffRegistrationRequest) {
        viewModelScope.launch {
            val result = staffRepository.registerStaff(staffRegistrationRequest)
            if (result.isSuccessful) {
                _messageResponseLiveData.value =
                    SingleEvent(Message(MessageType.SUCCESS, "Data saved successfully"))
            } else {
                val errorBody = result.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun fetchStaffById(staffId: String) = viewModelScope.launch {
        val result = staffRepository.getStaff(staffId)
        if (result.isSuccessful) {
            _Generic_response.postValue(SingleEvent(result.data?.body()))
        } else {
            val errorBody = result.exception
            _errorResponse.postValue(SingleEvent(errorBody))
        }
    }

    fun updateStaff(staffInfo: StaffInfo) {
        viewModelScope.launch {
            staffRepository.insertUser(staffInfo)
        }
    }
}