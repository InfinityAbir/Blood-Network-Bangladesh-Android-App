package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.BloodRequestDto
import com.bloodnetwork.bangladesh.data.model.CreateBloodRequestRequest
import com.bloodnetwork.bangladesh.data.model.Urgency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RequestBloodUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val myRequests: List<BloodRequestDto> = emptyList(),
)

class RequestBloodViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestBloodUiState())
    val uiState: StateFlow<RequestBloodUiState> = _uiState.asStateFlow()

    fun submit(
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = false)
            runCatching {
                repository.createBloodRequest(
                    CreateBloodRequestRequest(
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
                    )
                )
            }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                    loadMyRequests()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to submit request")
                }
        }
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            runCatching { repository.getMyBloodRequests() }
                .onSuccess { _uiState.value = _uiState.value.copy(myRequests = it) }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }
}
