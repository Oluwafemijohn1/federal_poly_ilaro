package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.data.repository.StaffRepository
import com.fpi.biometricsystem.data.repository.StudentRepository
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
    val gettingAllUsers get() = _gettingAllUsers
    val studentsUpdateFlow = studentRepository.allStudentsUpdateFlow()
    val staffUpdateFlow = staffRepository.allStaffUpdateFlow()

    fun getAllUsers() {
        viewModelScope.launch {
            staffRepository.getAllUsers()
            studentRepository.getAllUsers()
        }.invokeOnCompletion {
            _gettingAllUsers.value = true
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