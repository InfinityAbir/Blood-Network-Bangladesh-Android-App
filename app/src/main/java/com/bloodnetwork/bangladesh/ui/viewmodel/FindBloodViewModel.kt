package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.PublicDonorDto
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class FindBloodUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val donors: List<PublicDonorDto> = emptyList(),
    val error: String? = null,
    val searched: Boolean = false,
    val totalCount: Int = 0,
    val page: Int = 1,
    val hasMore: Boolean = false,
)

class FindBloodViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindBloodUiState())
    val uiState: StateFlow<FindBloodUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn.let { flow ->
        MutableStateFlow(false).also { state ->
            viewModelScope.launch { flow.collect { state.value = it } }
        }
    }

    private var lastBloodGroup: BloodGroup? = null
    private var lastDistrictId: String? = null
    private var lastUpazilaId: String? = null

    fun search(bloodGroup: BloodGroup?, districtId: String?, upazilaId: String?, isRefresh: Boolean = false) {
        lastBloodGroup = bloodGroup
        lastDistrictId = districtId
        lastUpazilaId = upazilaId
        _uiState.value = FindBloodUiState(isLoading = !isRefresh, isRefreshing = isRefresh)
        viewModelScope.launch {
            runCatching { repository.searchDonors(bloodGroup = bloodGroup, districtId = districtId, upazilaId = upazilaId, page = 1, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    _uiState.value = FindBloodUiState(
                        donors = result.items,
                        searched = true,
                        totalCount = result.totalCount,
                        page = 1,
                        hasMore = result.items.size < result.totalCount,
                        error = if (result.items.isEmpty()) "No matching donors found." else null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = FindBloodUiState(
                        searched = true,
                        error = e.toDisplayMessage("Search failed"),
                    )
                }
        }
    }

    fun refresh() {
        if (!_uiState.value.searched) return
        search(lastBloodGroup, lastDistrictId, lastUpazilaId, isRefresh = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoadingMore || state.isLoading) return
        val nextPage = state.page + 1
        _uiState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            runCatching {
                repository.searchDonors(
                    bloodGroup = lastBloodGroup, districtId = lastDistrictId, upazilaId = lastUpazilaId,
                    page = nextPage, pageSize = PAGE_SIZE,
                )
            }
                .onSuccess { result ->
                    val merged = _uiState.value.donors + result.items
                    _uiState.value = _uiState.value.copy(
                        donors = merged, page = nextPage, isLoadingMore = false,
                        hasMore = merged.size < result.totalCount,
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(isLoadingMore = false) }
        }
    }
}
