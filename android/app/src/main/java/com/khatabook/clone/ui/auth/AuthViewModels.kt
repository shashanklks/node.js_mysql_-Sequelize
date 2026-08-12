package com.khatabook.clone.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.clone.ServiceLocator
import kotlinx.coroutines.launch

data class LoginUiState(
    val phone: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    /** Set once the server accepts the number; carries the dev OTP when exposed. */
    val otpSent: Boolean = false,
    val devOtp: String? = null,
) {
    val canSubmit: Boolean get() = phone.length == 10 && !loading
}

class LoginViewModel : ViewModel() {

    private val repo = ServiceLocator.repository

    var state by mutableStateOf(LoginUiState())
        private set

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        state = state.copy(phone = digits, error = null)
    }

    fun sendOtp() {
        if (!state.canSubmit) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repo.sendOtp(state.phone)
                .onSuccess { state = state.copy(loading = false, otpSent = true, devOtp = it.otp) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun consumeOtpSent() {
        state = state.copy(otpSent = false)
    }
}

data class OtpUiState(
    val code: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val resendSeconds: Int = 30,
    val verified: Boolean = false,
    val needsProfile: Boolean = false,
) {
    val canSubmit: Boolean get() = code.length == 6 && !loading
}

class OtpViewModel : ViewModel() {

    private val repo = ServiceLocator.repository

    var state by mutableStateOf(OtpUiState())
        private set

    fun onCodeChange(value: String) {
        state = state.copy(code = value.filter { it.isDigit() }.take(6), error = null)
    }

    fun startResendTimer() {
        state = state.copy(resendSeconds = 30)
        viewModelScope.launch {
            while (state.resendSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                state = state.copy(resendSeconds = state.resendSeconds - 1)
            }
        }
    }

    fun verify(phone: String) {
        if (!state.canSubmit) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repo.verifyOtp(phone, state.code)
                .onSuccess { data ->
                    val user = data.user
                    state = state.copy(
                        loading = false,
                        verified = true,
                        needsProfile = data.isNewUser || user?.profileCompleted != true,
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun resend(phone: String) {
        if (state.resendSeconds > 0 || state.loading) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repo.sendOtp(phone)
                .onSuccess {
                    state = state.copy(loading = false, code = it.otp ?: "")
                    startResendTimer()
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }
}

data class ProfileSetupUiState(
    val name: String = "",
    val businessName: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !loading
}

class ProfileSetupViewModel : ViewModel() {

    private val repo = ServiceLocator.repository

    var state by mutableStateOf(ProfileSetupUiState())
        private set

    fun onNameChange(value: String) {
        state = state.copy(name = value, error = null)
    }

    fun onBusinessChange(value: String) {
        state = state.copy(businessName = value, error = null)
    }

    fun save() {
        if (!state.canSubmit) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repo.updateProfile(name = state.name.trim(), businessName = state.businessName.trim())
                .onSuccess { state = state.copy(loading = false, saved = true) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }
}

class LanguageViewModel : ViewModel() {

    private val session = ServiceLocator.session

    fun choose(code: String, onDone: () -> Unit) {
        viewModelScope.launch {
            session.saveLanguage(code)
            onDone()
        }
    }
}
