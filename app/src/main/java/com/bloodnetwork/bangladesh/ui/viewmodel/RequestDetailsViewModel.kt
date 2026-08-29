package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.BloodRequestDto
import com.bloodnetwork.bangladesh.data.model.UpdateBloodRequestRequest
import com.bloodnetwork.bangladesh.data.model.Urgency
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RequestDetailsUiState(
    val isLoading: Boolean = false,
    val request: BloodRequestDto? = null,
    val isSaving: Boolean = false,
    val isCancelling: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val cancelled: Boolean = false,
)

class RequestDetailsViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestDetailsUiState())
    val uiState: StateFlow<RequestDetailsUiState> = _uiState.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { repository.getBloodRequest(id) }
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, request = it) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load request")) }
        }
    }

    fun save(
        id: String,
        bloodGroup: BloodGroup,
        unitsRequired: Int,
        hospitalName: String,
        hospitalAddress: String,
        districtId: String,
        upazilaId: String,
        area: String?,
        requiredBy: String,
        urgency: Urgency,
        patientName: String?,
        patientRelation: String?,
        contactPhone: String,
        additionalInformation: String?,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saved = false)
            runCatching {
                repository.updateBloodRequest(
                    id,
                    UpdateBloodRequestRequest(
                        bloodGroup = bloodGroup,
                        unitsRequired = unitsRequired.coerceAtLeast(1),
                        hospitalName = hospitalName.trim(),
                        hospitalAddress = hospitalAddress.trim(),
                        districtId = districtId,
                        upazilaId = upazilaId,
                        area = area?.ifBlank { null },
                        requiredBy = requiredBy,
                        urgency = urgency,
                        patientName = patientName?.ifBlank { null },
                        patientRelation = patientRelation?.ifBlank { null },
                        contactPhone = contactPhone.trim(),
                        additionalInformation = additionalInformation?.ifBlank { null },
                    ),
                )
            }
                .onSuccess { updated -> _uiState.value = _uiState.value.copy(isSaving = false, request = updated, saved = true) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, error = e.toDisplayMessage("Failed to update request")) }
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, error = null)
            runCatching { repository.cancelBloodRequest(id) }
                .onSuccess { _uiState.value = _uiState.value.copy(isCancelling = false, cancelled = true) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isCancelling = false, error = e.toDisplayMessage("Failed to cancel request")) }
        }
    }

    fun clearSaved() {
        _uiState.value = _uiState.value.copy(saved = false)
    }
}
