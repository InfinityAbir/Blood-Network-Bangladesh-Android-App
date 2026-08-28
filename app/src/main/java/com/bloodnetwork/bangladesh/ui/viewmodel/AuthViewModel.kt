package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.UserDto
import com.bloodnetwork.bangladesh.data.prefs.RegistrationStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: UserDto? = null,
    val isLoggedIn: Boolean = false,
)

class AuthViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _registrationData = MutableStateFlow(RegistrationStore.RegistrationData())
    val registrationData: StateFlow<RegistrationStore.RegistrationData> = _registrationData.asStateFlow()

    final val loggedInState = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.isLoggedIn.collect { loggedInState.value = it }
        }
        viewModelScope.launch {
            val saved = repository.registrationData.first()
            if (saved.fullName.isNotEmpty() || saved.phoneNumber.isNotEmpty()) {
                _registrationData.value = saved
            }
        }
    }

    fun saveRegistrationData(data: RegistrationStore.RegistrationData) {
        _registrationData.value = data
        viewModelScope.launch { repository.saveRegistrationData(data) }
    }

    fun login(phone: String, password: String) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.login(phone.trim(), password) }
                .onSuccess { auth ->
                    _uiState.value = AuthUiState(
                        isLoggedIn = true,
                        user = auth.user,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = AuthUiState(
                        error = throwable.toUserMessage(),
                    )
                }
        }
    }

    fun register(firstName: String, lastName: String, phone: String, password: String, email: String) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val request = com.bloodnetwork.bangladesh.data.model.RegisterRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phone.trim(),
                password = password,
                email = email.trim().ifBlank { null },
            )
            runCatching { repository.register(request) }
                .onSuccess { auth ->
                    _uiState.value = AuthUiState(isLoggedIn = true, user = auth.user)
                }
                .onFailure { throwable ->
                    val msg = throwable.toUserMessage()
                    // If "already exists", account may have been created but response was lost — try login
                    if (msg.contains("already exists", ignoreCase = true)) {
                        runCatching { repository.login(phone.trim(), password) }
                            .onSuccess { auth ->
                                _uiState.value = AuthUiState(isLoggedIn = true, user = auth.user)
                            }
                            .onFailure {
                                _uiState.value = AuthUiState(error = msg)
                            }
                    } else {
                        _uiState.value = AuthUiState(error = msg)
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun Throwable.toUserMessage(): String {
        val raw = message ?: return "Something went wrong"
        if (raw.contains("408") || raw.contains("timeout")) return "Connection timeout. Please try again."
        if (raw.contains("401")) return "Invalid phone number or password."
        if (this is com.bloodnetwork.bangladesh.data.network.ApiException) return message
        return raw
    }
}
