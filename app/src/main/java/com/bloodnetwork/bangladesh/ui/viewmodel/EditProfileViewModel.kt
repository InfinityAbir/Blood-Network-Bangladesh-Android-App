package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.UpdateProfileRequest
import com.bloodnetwork.bangladesh.data.network.ApiException
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

data class DeleteAccountUiState(
    val isDeleting: Boolean = false,
    val error: String? = null,
    /** The server has no self-service deletion endpoint (404/405): fall back to the email/web request path. */
    val selfServiceUnavailable: Boolean = false,
    val deleted: Boolean = false,
)

class EditProfileViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow(DeleteAccountUiState())
    val deleteState: StateFlow<DeleteAccountUiState> = _deleteState.asStateFlow()

    fun resetDeleteState() {
        _deleteState.value = DeleteAccountUiState()
    }

    fun deleteAccount(currentPassword: String) {
        if (_deleteState.value.isDeleting) return
        _deleteState.value = DeleteAccountUiState(isDeleting = true)
        viewModelScope.launch {
            runCatching { repository.deleteAccount(currentPassword) }
                .onSuccess { _deleteState.value = DeleteAccountUiState(deleted = true) }
                .onFailure { e ->
                    val unavailable = e is ApiException && (e.code == 404 || e.code == 405 || e.code == 501)
                    _deleteState.value = DeleteAccountUiState(
                        error = if (unavailable) null else e.toDisplayMessage("Could not delete the account. Please try again."),
                        selfServiceUnavailable = unavailable,
                    )
                }
        }
    }

    fun updateProfile(currentPassword: String, newPhone: String?, newEmail: String?, newPassword: String?) {
        _uiState.value = EditProfileUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                repository.updateProfile(
                    UpdateProfileRequest(
                        currentPassword = currentPassword,
                        newPhoneNumber = newPhone,
                        newEmail = newEmail,
                        newPassword = newPassword,
                    )
                )
            }
                .onSuccess { _uiState.value = EditProfileUiState(success = true) }
                .onFailure { e -> _uiState.value = EditProfileUiState(error = e.toDisplayMessage("Update failed")) }
        }
    }
}
