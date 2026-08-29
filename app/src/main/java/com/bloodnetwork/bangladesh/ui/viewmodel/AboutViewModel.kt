package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.DeveloperInfoDto
import com.bloodnetwork.bangladesh.data.model.UpdateDeveloperInfoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AboutUiState(
    val isLoading: Boolean = false,
    val info: DeveloperInfoDto? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

class AboutViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getDeveloperInfo() }
                .onSuccess { info -> _uiState.value = _uiState.value.copy(isLoading = false, info = info) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load") }
        }
    }

    fun save(request: UpdateDeveloperInfoRequest, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
        viewModelScope.launch {
            runCatching { repository.updateDeveloperInfo(request) }
                .onSuccess { info ->
                    _uiState.value = _uiState.value.copy(isSaving = false, info = info)
                    onSuccess()
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message ?: "Failed to save") }
        }
    }
}
