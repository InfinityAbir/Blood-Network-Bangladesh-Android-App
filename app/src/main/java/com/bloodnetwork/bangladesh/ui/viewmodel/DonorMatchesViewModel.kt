package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.BloodRequestMatchDto
import com.bloodnetwork.bangladesh.data.model.DonorResponse
import com.bloodnetwork.bangladesh.data.model.RespondToMatchRequest
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DonorMatchesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val matches: List<BloodRequestMatchDto> = emptyList(),
    val respondingMatchId: String? = null,
)

class DonorMatchesViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonorMatchesUiState(isLoading = true))
    val uiState: StateFlow<DonorMatchesUiState> = _uiState.asStateFlow()

    fun loadMatches() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getDonorMatches() }
                .onSuccess { matches ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, matches = matches.sortedByDescending { it.createdAt })
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load matches")) }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadMatches()
    }

    fun respond(matchId: String, response: DonorResponse) {
        _uiState.value = _uiState.value.copy(respondingMatchId = matchId)
        viewModelScope.launch {
            runCatching { repository.respondToMatch(matchId, RespondToMatchRequest(response)) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        respondingMatchId = null,
                        matches = _uiState.value.matches.map { if (it.id == updated.id) updated else it },
                    )
                    // Accepting commits the donor (the backend flips availability to Unavailable for
                    // urgent requests), so reload the profile and push it into the shared donor-profile
                    // state — the availability toggle on the dashboard/profile screens flips off
                    // right away instead of waiting for a manual refresh.
                    if (updated.donorResponse == DonorResponse.Accepted) {
                        runCatching { repository.getMyDonorProfile() }
                    }
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(respondingMatchId = null, error = e.toDisplayMessage("Failed to respond")) }
        }
    }
}
