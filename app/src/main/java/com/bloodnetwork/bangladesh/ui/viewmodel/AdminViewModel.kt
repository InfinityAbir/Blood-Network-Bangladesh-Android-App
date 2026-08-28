package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.AdminDashboardStats
import com.bloodnetwork.bangladesh.data.model.AdminUserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val dashboard: AdminDashboardStats? = null,
    val users: List<AdminUserDto> = emptyList(),
    val error: String? = null,
)

class AdminViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadDashboard() {
        _uiState.value = AdminUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val dashboard = repository.getAdminDashboard()
                val users = repository.getAdminUsers(pageSize = 10).items
                dashboard to users
            }
                .onSuccess { (dashboard, users) ->
                    _uiState.value = AdminUiState(dashboard = dashboard, users = users)
                }
                .onFailure { e ->
                    _uiState.value = AdminUiState(error = e.message ?: "Failed to load admin data")
                }
        }
    }
}
