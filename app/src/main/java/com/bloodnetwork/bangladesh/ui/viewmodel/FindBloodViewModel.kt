package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.PublicDonorDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FindBloodUiState(
    val isLoading: Boolean = false,
    val donors: List<PublicDonorDto> = emptyList(),
    val error: String? = null,
    val searched: Boolean = false,
    val totalCount: Int = 0,
)

class FindBloodViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindBloodUiState())
    val uiState: StateFlow<FindBloodUiState> = _uiState.asStateFlow()

    fun search(bloodGroup: BloodGroup?, districtId: String?, upazilaId: String?) {
        _uiState.value = FindBloodUiState(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.searchDonors(bloodGroup = bloodGroup, districtId = districtId, upazilaId = upazilaId) }
                .onSuccess { result ->
                    _uiState.value = FindBloodUiState(
                        donors = result.items,
                        searched = true,
                        totalCount = result.totalCount,
                        error = if (result.items.isEmpty()) "No matching donors found." else null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = FindBloodUiState(
                        searched = true,
                        error = e.message ?: "Search failed",
                    )
                }
        }
    }
}
