package com.fpi.biometricsystem.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(): ViewModel() {

    private var _loginFormData: MutableStateFlow<LoginFormData> = MutableStateFlow(LoginFormData())
    val loginFormData: StateFlow<LoginFormData> get() = _loginFormData

    private var _loginResponse: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val loginResponse: SharedFlow<Boolean> get() = _loginResponse

    fun validateEmail(email: String) {
        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        _loginFormData.value = loginFormData.value.copy(isValidEmail = isValidEmail)
    }

    fun validatePassword(password: String) {
        _loginFormData.value = loginFormData.value.copy(isValidPassword = password.isNotBlank())
    }

    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        _loginResponse.emit(true)
    }
}

data class LoginFormData(
    val isValidEmail: Boolean = false,
    val isValidPassword: Boolean = false,
) {
    val isValidForm: Boolean get() = isValidEmail && isValidPassword
}