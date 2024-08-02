package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.Message
import com.fpi.biometricsystem.data.MessageType
import com.fpi.biometricsystem.data.StudentDataInfo
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.data.repository.StudentRepository
import com.fpi.biometricsystem.data.request.StudentRegistrationRequest
import com.fpi.biometricsystem.utils.SingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentRegistrationViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
) : ViewModel() {
    private val _messageResponseLiveData: MutableLiveData<SingleEvent<Message>> = MutableLiveData()
    val messageResponseLiveData: LiveData<SingleEvent<Message>> = _messageResponseLiveData

    private val _errorResponse: MutableLiveData<SingleEvent<GenericError?>> = MutableLiveData()
    val errorResponse: LiveData<SingleEvent<GenericError?>> get() = _errorResponse

    private val _Generic_response =
        MutableLiveData<SingleEvent<GenericResponse<StudentDataInfo>?>>()
    val finalResponse: LiveData<SingleEvent<GenericResponse<StudentDataInfo>?>> get() = _Generic_response
    fun createUser(studentRegistrationRequest: StudentRegistrationRequest) {
        viewModelScope.launch {
            val result = studentRepository.registerStudent(studentRegistrationRequest)
            println(result)
            if (result.isSuccessful) {
                println("Success :${result.data?.body()}")
                _messageResponseLiveData.value =
                    SingleEvent(Message(MessageType.SUCCESS, "Data saved successfully"))
            } else {
                val errorBody = result.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun fetchStudentById(matricNo: String) = viewModelScope.launch {
        val result = studentRepository.fetchStudent(matricNo)
        if (result.isSuccessful) {
            _Generic_response.postValue(SingleEvent(result.data?.body()))
        } else {
            val errorBody = result.exception
            _errorResponse.postValue(SingleEvent(errorBody))
        }
    }

    fun updateStudentInfo(studentInfo: StudentInfo) {
        viewModelScope.launch {
            studentRepository.updateStudentInfo(studentInfo)
        }
    }

    fun getUser(userId: String): LiveData<StudentInfo?> = studentRepository.getUser(userId)
}