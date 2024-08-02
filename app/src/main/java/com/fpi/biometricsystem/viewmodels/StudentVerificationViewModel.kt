package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.StudentDataInfo
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.data.repository.StudentRepository
import com.fpi.biometricsystem.data.request.ExamAttendanceRequest
import com.fpi.biometricsystem.data.request.StudentAttendanceRequest
import com.fpi.biometricsystem.utils.SingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentVerificationViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
) : ViewModel() {
    private val _allStudents = MutableLiveData<GenericResponse<List<StudentDataInfo>>?>()
    val allStudents get() = studentRepository.allStudentsUpdateFlow()

    private val _errorResponse: MutableLiveData<SingleEvent<GenericError?>> = MutableLiveData()
    val errorResponse: LiveData<SingleEvent<GenericError?>> get() = _errorResponse

    private val _Generic_response = MutableLiveData<GenericResponse<Any>>()
    val finalResponse get() = _Generic_response.asFlow()

    fun allStudents() = viewModelScope.launch { studentRepository.getAllUsers() }

    fun markAttendance(request: StudentAttendanceRequest) {
        viewModelScope.launch {
            val result = studentRepository.markStudentAttendance(request)

            println("Issueee: $result")
            if (result.isSuccessful){
                _Generic_response.postValue(result.data?.body())
            } else {
                val errorBody = result.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun markExaminationAttendance(request: ExamAttendanceRequest) {
        viewModelScope.launch {
            val result = studentRepository.markExamAttendance(request)

            if (result.isSuccessful){
                _Generic_response.postValue(result.data?.body())
            } else {
                val errorBody = result.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun getUser(userId: String): LiveData<StudentInfo?> = studentRepository.getUser(userId)
}