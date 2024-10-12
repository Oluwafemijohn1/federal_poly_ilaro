package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.BaseUrlResponse
import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.data.repository.StaffRepository
import com.fpi.biometricsystem.data.repository.StudentRepository
import com.fpi.biometricsystem.utils.SingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val studentRepository: StudentRepository,
    private val pref: PreferenceStore,
) : ViewModel() {
    private val _gettingAllUsers = MutableLiveData(false)


    private val _baseUrl = MutableLiveData<GenericResponse<BaseUrlResponse>?>()
    val baseUrl get() = _baseUrl.asFlow()
    private val _errorResponse: MutableLiveData<SingleEvent<GenericError?>> = MutableLiveData()
    val errorResponse: LiveData<SingleEvent<GenericError?>> get() = _errorResponse


    fun fetchBaseUrl() {
        viewModelScope.launch {
            val urlData = staffRepository.fetchBaseUrl()
            if (urlData.isSuccessful) {
                _baseUrl.postValue(urlData.data?.body())
            } else {
                val errorBody = urlData.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun getAllUsersFresh() {
        viewModelScope.launch {
            staffRepository.getAllUsers(1)
            studentRepository.getAllUsers(1)
        }.invokeOnCompletion {
            _gettingAllUsers.value = true
        }
    }

    fun clearAndUpdateDatabase() {
        _gettingAllUsers.value = false
        viewModelScope.launch {
            studentRepository.clearDatabase()
            staffRepository.clearDatabase()
            pref.clearUserData()
            getAllUsersFresh()
        }.invokeOnCompletion {
            _gettingAllUsers.value = true
        }
    }
}