package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.AdminAnalyticsDto
import com.bloodnetwork.bangladesh.data.model.AdminDashboardStats
import com.bloodnetwork.bangladesh.data.model.AdminUserDto
import com.bloodnetwork.bangladesh.data.model.AdminReportDto
import com.bloodnetwork.bangladesh.data.model.AdminAuditLogDto
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.BloodRequestDto
import com.bloodnetwork.bangladesh.data.model.BloodRequestMatchDto
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

data class AdminUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dashboard: AdminDashboardStats? = null,
    val analytics: AdminAnalyticsDto? = null,
    val isAnalyticsLoading: Boolean = false,
    val analyticsError: String? = null,
    val users: List<AdminUserDto> = emptyList(),
    val usersPage: Int = 1,
    val usersPageSize: Int = PAGE_SIZE,
    val usersTotalCount: Int = 0,
    val usersHasMore: Boolean = false,
    val usersLoadingMore: Boolean = false,
    val reports: List<AdminReportDto> = emptyList(),
    val reportsPage: Int = 1,
    val reportsPageSize: Int = PAGE_SIZE,
    val reportsTotalCount: Int = 0,
    val reportsHasMore: Boolean = false,
    val reportsLoadingMore: Boolean = false,
    val auditLogs: List<AdminAuditLogDto> = emptyList(),
    val auditLogsPage: Int = 1,
    val auditLogsPageSize: Int = PAGE_SIZE,
    val auditLogsTotalCount: Int = 0,
    val auditLogsHasMore: Boolean = false,
    val auditLogsLoadingMore: Boolean = false,
    val bloodRequests: List<BloodRequestDto> = emptyList(),
    val bloodRequestsPage: Int = 1,
    val bloodRequestsPageSize: Int = PAGE_SIZE,
    val bloodRequestsTotalCount: Int = 0,
    val matches: List<BloodRequestMatchDto> = emptyList(),
    val matchesPage: Int = 1,
    val matchesPageSize: Int = PAGE_SIZE,
    val matchesTotalCount: Int = 0,
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
    private var usersIsActive: Boolean? = null
    private var usersPageSize: Int = PAGE_SIZE
    private var reportsStatus: String? = null
    private var reportsPageSize: Int = PAGE_SIZE
    private var auditEntityType: String? = null
    private var auditPageSize: Int = PAGE_SIZE
    private var bloodRequestsStatus: String? = null
    private var bloodRequestsGroup: BloodGroup? = null
    private var bloodRequestsPageSize: Int = PAGE_SIZE
    private var matchesResponse: String? = null
    private var matchesPageSize: Int = PAGE_SIZE

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
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load admin data"))
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
                .onFailure { e -> _uiState.value = _uiState.value.copy(isAnalyticsLoading = false, analyticsError = e.toDisplayMessage("Failed to load analytics")) }
        }
    }

    fun loadUsers(search: String? = usersSearch, role: String? = usersRole, isActive: Boolean? = usersIsActive, pageSize: Int? = null) {
        usersSearch = search
        usersRole = role
        usersIsActive = isActive
        if (pageSize != null) usersPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminUsers(search, role, isActive, page = 1, pageSize = usersPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, users = result.items,
                        usersPage = 1, usersPageSize = usersPageSize, usersTotalCount = result.totalCount,
                        usersHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load users")) }
        }
    }

    fun gotoUsersPage(page: Int, pageSize: Int? = null) {
        if (pageSize != null) usersPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminUsers(usersSearch, usersRole, usersIsActive, page = page, pageSize = usersPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, users = result.items,
                        usersPage = page, usersPageSize = usersPageSize, usersTotalCount = result.totalCount,
                        usersHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load users")) }
        }
    }

    fun refreshUsers() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadUsers(usersSearch, usersRole, usersIsActive)
    }

    fun loadMoreUsers() {
        val state = _uiState.value
        if (!state.usersHasMore || state.usersLoadingMore) return
        val nextPage = state.usersPage + 1
        _uiState.value = state.copy(usersLoadingMore = true)
        viewModelScope.launch {
            runCatching { repository.getAdminUsers(usersSearch, usersRole, usersIsActive, page = nextPage, pageSize = PAGE_SIZE) }
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

    fun loadReports(status: String? = reportsStatus, pageSize: Int? = null) {
        reportsStatus = status
        if (pageSize != null) reportsPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminReports(status, page = 1, pageSize = reportsPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, reports = result.items,
                        reportsPage = 1, reportsPageSize = reportsPageSize, reportsTotalCount = result.totalCount,
                        reportsHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load reports")) }
        }
    }

    fun gotoReportsPage(page: Int, pageSize: Int? = null) {
        if (pageSize != null) reportsPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminReports(reportsStatus, page = page, pageSize = reportsPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, reports = result.items,
                        reportsPage = page, reportsPageSize = reportsPageSize, reportsTotalCount = result.totalCount,
                        reportsHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load reports")) }
        }
    }

    fun refreshReports() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadReports(reportsStatus)
    }

    fun loadAuditLogs(entityType: String? = auditEntityType, pageSize: Int? = null) {
        auditEntityType = entityType
        if (pageSize != null) auditPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminAuditLogs(entityType, page = 1, pageSize = auditPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, auditLogs = result.items,
                        auditLogsPage = 1, auditLogsPageSize = auditPageSize, auditLogsTotalCount = result.totalCount,
                        auditLogsHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load logs")) }
        }
    }

    fun gotoAuditLogsPage(page: Int, pageSize: Int? = null) {
        if (pageSize != null) auditPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminAuditLogs(auditEntityType, page = page, pageSize = auditPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, auditLogs = result.items,
                        auditLogsPage = page, auditLogsPageSize = auditPageSize, auditLogsTotalCount = result.totalCount,
                        auditLogsHasMore = result.items.size < result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load logs")) }
        }
    }

    fun refreshAuditLogs() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadAuditLogs(auditEntityType)
    }

    fun loadBloodRequests(status: String? = bloodRequestsStatus, bloodGroup: BloodGroup? = bloodRequestsGroup, pageSize: Int? = null) {
        bloodRequestsStatus = status
        bloodRequestsGroup = bloodGroup
        if (pageSize != null) bloodRequestsPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminBloodRequests(status, bloodGroup, page = 1, pageSize = bloodRequestsPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, bloodRequests = result.items,
                        bloodRequestsPage = 1, bloodRequestsPageSize = bloodRequestsPageSize, bloodRequestsTotalCount = result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load blood requests")) }
        }
    }

    fun gotoBloodRequestsPage(page: Int, pageSize: Int? = null) {
        if (pageSize != null) bloodRequestsPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminBloodRequests(bloodRequestsStatus, bloodRequestsGroup, page = page, pageSize = bloodRequestsPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, bloodRequests = result.items,
                        bloodRequestsPage = page, bloodRequestsPageSize = bloodRequestsPageSize, bloodRequestsTotalCount = result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load blood requests")) }
        }
    }

    fun refreshBloodRequests() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadBloodRequests(bloodRequestsStatus, bloodRequestsGroup)
    }

    fun loadMatches(response: String? = matchesResponse, pageSize: Int? = null) {
        matchesResponse = response
        if (pageSize != null) matchesPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminMatches(response, page = 1, pageSize = matchesPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false, matches = result.items,
                        matchesPage = 1, matchesPageSize = matchesPageSize, matchesTotalCount = result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load matches")) }
        }
    }

    fun gotoMatchesPage(page: Int, pageSize: Int? = null) {
        if (pageSize != null) matchesPageSize = pageSize
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getAdminMatches(matchesResponse, page = page, pageSize = matchesPageSize) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, matches = result.items,
                        matchesPage = page, matchesPageSize = matchesPageSize, matchesTotalCount = result.totalCount,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load matches")) }
        }
    }

    fun refreshMatches() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadMatches(matchesResponse)
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
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.toDisplayMessage("Action failed")) }
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
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.toDisplayMessage("Action failed")) }
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
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.toDisplayMessage("Action failed")) }
        }
    }
}
