package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.UserDto
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
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
    val meLoadFailed: Boolean = false,
    val isProfileRefreshing: Boolean = false,
)

class AuthViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _registrationData = MutableStateFlow(RegistrationStore.RegistrationData())
    val registrationData: StateFlow<RegistrationStore.RegistrationData> = _registrationData.asStateFlow()

    final val loggedInState = MutableStateFlow(false)

    /** Set by a screen that sent the user to Login/Register because an action needed an
     * account (e.g. tapping "Request Blood" while signed out) — consumed once, in AppRoot's
     * onLoggedIn, to land back on that screen instead of the default post-login dashboard. */
    var pendingRedirectRoute: String? = null

    /** The specific donor a "Request Blood" tap came from, carried across the Login/Register
     * detour (if any) so RequestBloodScreen can pre-fill instead of opening blank. Consumed
     * once by RequestBloodScreen. */
    data class PendingRequestPrefill(
        val bloodGroup: BloodGroup,
        val districtId: String,
        val districtName: String,
        val upazilaId: String,
        val upazilaName: String,
    )
    var pendingRequestPrefill: PendingRequestPrefill? = null

    init {
        viewModelScope.launch {
            repository.isLoggedIn.collect { isLoggedIn ->
                loggedInState.value = isLoggedIn
                if (isLoggedIn) {
                    repository.notificationSocket.start()
                    if (_uiState.value.user == null) loadMe()
                } else {
                    repository.notificationSocket.stop()
                    _uiState.value = AuthUiState()
                }
            }
        }
        viewModelScope.launch {
            val saved = repository.registrationData.first()
            if (saved.firstName.isNotEmpty() || saved.phoneNumber.isNotEmpty()) {
                _registrationData.value = saved
            }
        }
    }

    private fun loadMe() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProfileRefreshing = true)
            runCatching { repository.me() }
                .onSuccess { user -> _uiState.value = _uiState.value.copy(user = user, isLoggedIn = true, meLoadFailed = false, isProfileRefreshing = false) }
                .onFailure { _uiState.value = _uiState.value.copy(meLoadFailed = true, isProfileRefreshing = false) }
        }
    }

    /** Retries fetching the profile after a failed cold-start load (e.g. no network yet). */
    fun retryLoadUser() {
        if (_uiState.value.user != null) return
        loadMe()
    }

    /** Explicit refresh (e.g. pull-to-refresh) — reloads even if a profile is already cached. */
    fun refreshProfile() = loadMe()

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

    fun register(firstName: String, lastName: String, phone: String, password: String, email: String, role: com.bloodnetwork.bangladesh.data.model.UserRole = com.bloodnetwork.bangladesh.data.model.UserRole.Requester) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val request = com.bloodnetwork.bangladesh.data.model.RegisterRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phone.trim(),
                password = password,
                email = email.trim().ifBlank { null },
                role = role,
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

    fun updateProfile(
        currentPassword: String,
        newEmail: String?,
        newPhoneNumber: String?,
        newPassword: String?,
        newPhotoUrl: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                repository.updateProfile(
                    com.bloodnetwork.bangladesh.data.model.UpdateProfileRequest(
                        currentPassword = currentPassword,
                        newEmail = newEmail,
                        newPhoneNumber = newPhoneNumber,
                        newPassword = newPassword,
                        newPhotoUrl = newPhotoUrl,
                    )
                )
            }
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(user = user)
                    onSuccess()
                }
                .onFailure { e -> onError(e.toDisplayMessage("Update failed")) }
        }
    }

    /** Photo-only update — doesn't require the current password (see backend AuthService). */
    fun updatePhoto(photoUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        updateProfile(
            currentPassword = "",
            newEmail = null,
            newPhoneNumber = null,
            newPassword = null,
            newPhotoUrl = photoUrl,
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    private fun Throwable.toUserMessage(): String {
        if (this is com.bloodnetwork.bangladesh.data.network.ApiException && code == 401) {
            return "Invalid phone number or password."
        }
        return toDisplayMessage("Something went wrong. Please try again.")
    }
}
