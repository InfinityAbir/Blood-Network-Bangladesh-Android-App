package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.SystemSettingsDto
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SystemSettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val settings: SystemSettingsDto? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

class AdminSystemSettingsViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemSettingsUiState(isLoading = true))
    val uiState: StateFlow<SystemSettingsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getSystemSettings() }
                .onSuccess { s -> _uiState.value = _uiState.value.copy(isLoading = false, settings = s) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load settings")) }
        }
    }

    fun save(settings: SystemSettingsDto) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null, successMessage = null)
        viewModelScope.launch {
            runCatching { repository.updateSystemSettings(settings) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(isSaving = false, settings = updated, successMessage = "Settings saved")
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, error = e.toDisplayMessage("Failed to save settings")) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
