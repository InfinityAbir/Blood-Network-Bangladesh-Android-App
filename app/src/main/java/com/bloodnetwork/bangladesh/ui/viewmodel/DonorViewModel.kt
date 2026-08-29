package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.AvailabilityStatus
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.CreateDonorProfileRequest
import com.bloodnetwork.bangladesh.data.model.DonorProfileDto
import com.bloodnetwork.bangladesh.data.model.UpdateDonorProfileRequest
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import com.bloodnetwork.bangladesh.data.prefs.DonorProfileStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DonorUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: DonorProfileDto? = null,
    val hasProfile: Boolean = false,
    val saved: Boolean = false,
)

class DonorViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonorUiState())
    val uiState: StateFlow<DonorUiState> = _uiState.asStateFlow()

    private val _draftData = MutableStateFlow(DonorProfileStore.DonorProfileData())
    val draftData: StateFlow<DonorProfileStore.DonorProfileData> = _draftData.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = repository.donorProfileData.first()
            if (saved.bloodGroup.isNotEmpty()) {
                _draftData.value = saved
            }
        }
    }

    fun saveDraftData(data: DonorProfileStore.DonorProfileData) {
        _draftData.value = data
        viewModelScope.launch { repository.saveDonorProfileData(data) }
    }

    fun loadProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val profile = repository.getMyDonorProfile()
                _uiState.value = DonorUiState(
                    profile = profile,
                    hasProfile = true,
                )
            } catch (e: com.bloodnetwork.bangladesh.data.network.ApiException) {
                if (e.code == 404) {
                    _uiState.value = DonorUiState(hasProfile = false, error = null)
                } else {
                    _uiState.value = DonorUiState(hasProfile = false, error = e.message)
                }
            } catch (e: Exception) {
                _uiState.value = DonorUiState(
                    hasProfile = false,
                    error = e.toDisplayMessage("Failed to load profile"),
                )
            }
        }
    }

    fun saveOrUpdate(
        bloodGroup: BloodGroup,
        gender: String?,
        dateOfBirth: String?,
        districtId: String,
        upazilaId: String,
        area: String?,
        customAddress: String?,
        lastDonationDate: String?,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = if (_uiState.value.hasProfile || _uiState.value.profile != null) {
                    repository.updateDonorProfile(
                        UpdateDonorProfileRequest(
                            bloodGroup = bloodGroup,
                            gender = gender,
                            dateOfBirth = dateOfBirth,
                            districtId = districtId,
                            upazilaId = upazilaId,
                            area = area,
                            customAddress = customAddress,
                            lastDonationDate = lastDonationDate,
                        )
                    )
                } else {
                    repository.createDonorProfile(
                        CreateDonorProfileRequest(
                            bloodGroup = bloodGroup,
                            gender = gender,
                            dateOfBirth = dateOfBirth,
                            districtId = districtId,
                            upazilaId = upazilaId,
                            area = area,
                            customAddress = customAddress,
                            lastDonationDate = lastDonationDate,
                        )
                    )
                }
                _uiState.value = DonorUiState(
                    profile = result,
                    hasProfile = true,
                    saved = true,
                )
                repository.clearDonorProfileData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.toDisplayMessage("Failed to save profile"),
                )
            }
        }
    }

    fun updateAvailability(status: AvailabilityStatus) {
        viewModelScope.launch {
            if (_uiState.value.profile == null && !_uiState.value.hasProfile) {
                _uiState.value = _uiState.value.copy(error = "Please save your donor profile first")
                return@launch
            }
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val profile = repository.toggleAvailability(com.bloodnetwork.bangladesh.data.model.ToggleAvailabilityRequest(status))
                _uiState.value = _uiState.value.copy(profile = profile, error = null, hasProfile = true)
            } catch (e: com.bloodnetwork.bangladesh.data.network.ApiException) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.toDisplayMessage("Failed to update availability"))
            }
        }
    }

    fun clearSaved() {
        _uiState.value = _uiState.value.copy(saved = false)
    }
}
