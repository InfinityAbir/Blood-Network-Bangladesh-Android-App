package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.DistrictDto
import com.bloodnetwork.bangladesh.data.model.DivisionDto
import com.bloodnetwork.bangladesh.data.model.UpazilaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationUiState(
    val divisions: List<DivisionDto> = emptyList(),
    val districts: List<DistrictDto> = emptyList(),
    val upazilas: List<UpazilaDto> = emptyList(),
    val loading: Boolean = false,
)

class LocationViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    fun loadDivisions() {
        viewModelScope.launch {
            runCatching { repository.getDivisions() }
                .onSuccess { list -> _uiState.value = _uiState.value.copy(divisions = list) }
        }
    }

    fun loadDistricts(divisionId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, districts = emptyList(), upazilas = emptyList())
            runCatching { repository.getDistricts(divisionId) }
                .onSuccess { list -> _uiState.value = _uiState.value.copy(districts = list, loading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(loading = false) }
        }
    }

    fun loadAllDistricts() {
        viewModelScope.launch {
            runCatching { repository.getDistricts() }
                .onSuccess { list -> _uiState.value = _uiState.value.copy(districts = list) }
        }
    }

    fun loadUpazilas(districtId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, upazilas = emptyList())
            runCatching { repository.getUpazilas(districtId) }
                .onSuccess { list -> _uiState.value = _uiState.value.copy(upazilas = list, loading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(loading = false) }
        }
    }
}
