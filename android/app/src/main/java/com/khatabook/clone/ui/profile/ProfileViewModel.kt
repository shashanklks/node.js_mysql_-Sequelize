package com.khatabook.clone.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.clone.ServiceLocator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val businessName: String = "",
    val phone: String? = null,
    val language: String = "en",
    val baseUrl: String = "",
    val saving: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
    val loggedOut: Boolean = false,
)

class ProfileViewModel : ViewModel() {

    private val repo = ServiceLocator.repository
    private val session = ServiceLocator.session

    var state by mutableStateOf(ProfileUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(
                baseUrl = session.baseUrl.first(),
                language = session.language.first() ?: "en",
            )
            repo.profile().onSuccess {
                state = state.copy(
                    name = it.name.orEmpty(),
                    businessName = it.businessName.orEmpty(),
                    phone = it.phone,
                    language = it.language,
                )
            }
        }
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value, message = null)
    }

    fun onBusinessChange(value: String) {
        state = state.copy(businessName = value, message = null)
    }

    fun onBaseUrlChange(value: String) {
        state = state.copy(baseUrl = value, message = null)
    }

    fun saveProfile() {
        if (state.name.isBlank()) {
            state = state.copy(message = "Name cannot be empty", messageIsError = true)
            return
        }
        state = state.copy(saving = true, message = null)
        viewModelScope.launch {
            repo.updateProfile(name = state.name.trim(), businessName = state.businessName.trim())
                .onSuccess {
                    state = state.copy(saving = false, message = "Saved", messageIsError = false)
                }
                .onFailure {
                    state = state.copy(saving = false, message = it.message, messageIsError = true)
                }
        }
    }

    fun saveBaseUrl() {
        viewModelScope.launch {
            session.saveBaseUrl(state.baseUrl)
            val stored = session.baseUrl.first()
            state = state.copy(
                baseUrl = stored,
                message = "API server set to $stored",
                messageIsError = false,
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            state = state.copy(loggedOut = true)
        }
    }
}
