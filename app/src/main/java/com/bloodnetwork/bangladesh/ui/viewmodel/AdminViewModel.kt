package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.AdminAnalyticsDto
import com.bloodnetwork.bangladesh.data.model.AdminDashboardStats
import com.bloodnetwork.bangladesh.data.model.AdminUserDto
import com.bloodnetwork.bangladesh.data.model.AdminReportDto
import com.bloodnetwork.bangladesh.data.model.AdminAuditLogDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class AdminUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dashboard: AdminDashboardStats? = null,
    val analytics: AdminAnalyticsDto? = null,
    val isAnalyticsLoading: Boolean = false,
    val analyticsError: String? = null,
    val users: List<AdminUserDto> = emptyList(),
    val usersPage: Int = 1,
    val usersHasMore: Boolean = false,
    val usersLoadingMore: Boolean = false,
    val reports: List<AdminReportDto> = emptyList(),
    val reportsPage: Int = 1,
    val reportsHasMore: Boolean = false,
    val reportsLoadingMore: Boolean = false,
    val auditLogs: List<AdminAuditLogDto> = emptyList(),
    val auditLogsPage: Int = 1,
    val auditLogsHasMore: Boolean = false,
    val auditLogsLoadingMore: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

class AdminViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState(isLoading = true))
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var usersSearch: String? = null
    private var usersRole: String? = null
    private var reportsStatus: String? = null
    private var auditEntityType: String? = null

    init { loadDashboard() }

    fun loadDashboard() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val dashboard = repository.getAdminDashboard()
                val users = repository.getAdminUsers(pageSize = 10)
                dashboard to users
            }
                .onSuccess { (dashboard, users) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false,
                        dashboard = dashboard, users = users.items,
                        usersPage = 1, usersHasMore = users.items.size < users.totalCount,
                        error = null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load admin data")
                }
        }
    }

    fun refreshDashboard() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadDashboard()
    }

    fun loadAnalytics() {
        _uiState.value = _uiState.value.copy(isAnalyticsLoading = true, analyticsError = null)
        viewModelScope.launch {
            runCatching { repository.getAdminAnalytics() }
                .onSuccess { analytics -> _uiState.value = _uiState.value.copy(isAnalyticsLoading = false, analytics = analytics) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isAnalyticsLoading = false, analyticsError = e.message ?: "Failed to load analytics") }
        }
    }

    fun loadUsers(search: String? = usersSearch, role: String? = usersRole) {
        usersSearch = search
        usersRole = role
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminUsers(search, role, page = 1, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, users = result.items,
                        usersPage = 1, usersHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load users") }
        }
    }

    fun refreshUsers() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadUsers(usersSearch, usersRole)
    }

    fun loadMoreUsers() {
        val state = _uiState.value
        if (!state.usersHasMore || state.usersLoadingMore) return
        val nextPage = state.usersPage + 1
        _uiState.value = state.copy(usersLoadingMore = true)
        viewModelScope.launch {
            runCatching { repository.getAdminUsers(usersSearch, usersRole, page = nextPage, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    val merged = _uiState.value.users + result.items
                    _uiState.value = _uiState.value.copy(
                        users = merged, usersPage = nextPage, usersLoadingMore = false,
                        usersHasMore = merged.size < result.totalCount,
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(usersLoadingMore = false) }
        }
    }

    fun loadReports(status: String? = reportsStatus) {
        reportsStatus = status
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminReports(status, page = 1, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, reports = result.items,
                        reportsPage = 1, reportsHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load reports") }
        }
    }

    fun refreshReports() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadReports(reportsStatus)
    }

    fun loadMoreReports() {
        val state = _uiState.value
        if (!state.reportsHasMore || state.reportsLoadingMore) return
        val nextPage = state.reportsPage + 1
        _uiState.value = state.copy(reportsLoadingMore = true)
        viewModelScope.launch {
            runCatching { repository.getAdminReports(reportsStatus, page = nextPage, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    val merged = _uiState.value.reports + result.items
                    _uiState.value = _uiState.value.copy(
                        reports = merged, reportsPage = nextPage, reportsLoadingMore = false,
                        reportsHasMore = merged.size < result.totalCount,
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(reportsLoadingMore = false) }
        }
    }

    fun loadAuditLogs(entityType: String? = auditEntityType) {
        auditEntityType = entityType
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminAuditLogs(entityType, page = 1, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, auditLogs = result.items,
                        auditLogsPage = 1, auditLogsHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load logs") }
        }
    }

    fun refreshAuditLogs() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadAuditLogs(auditEntityType)
    }

    fun loadMoreAuditLogs() {
        val state = _uiState.value
        if (!state.auditLogsHasMore || state.auditLogsLoadingMore) return
        val nextPage = state.auditLogsPage + 1
        _uiState.value = state.copy(auditLogsLoadingMore = true)
        viewModelScope.launch {
            runCatching { repository.getAdminAuditLogs(auditEntityType, page = nextPage, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    val merged = _uiState.value.auditLogs + result.items
                    _uiState.value = _uiState.value.copy(
                        auditLogs = merged, auditLogsPage = nextPage, auditLogsLoadingMore = false,
                        auditLogsHasMore = merged.size < result.totalCount,
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(auditLogsLoadingMore = false) }
        }
    }

    fun toggleActive(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            runCatching { repository.toggleUserActive(userId, isActive) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        users = _uiState.value.users.map { if (it.id == updated.id) updated else it },
                        successMessage = if (isActive) "User activated" else "User deactivated",
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message ?: "Action failed") }
        }
    }

    fun verifyDonor(userId: String, status: String) {
        viewModelScope.launch {
            runCatching { repository.verifyDonor(userId, status) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        users = _uiState.value.users.map { if (it.id == updated.id) updated else it },
                        successMessage = "Donor $status",
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message ?: "Action failed") }
        }
    }

    fun resolveReport(reportId: String, status: String, resolution: String?) {
        viewModelScope.launch {
            runCatching { repository.resolveReport(reportId, status, resolution) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        reports = _uiState.value.reports.map { if (it.id == updated.id) updated else it },
                        successMessage = "Report $status",
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message ?: "Action failed") }
        }
    }
}
