package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.UpdateProfileRequest
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

class EditProfileViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

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
