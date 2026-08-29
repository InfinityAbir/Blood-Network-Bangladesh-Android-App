package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.data.model.NotificationType
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
    val typeFilter: NotificationType? = null,
)

class NotificationsViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        // Instant badge updates while this screen/sheet is alive, without waiting for a poll.
        viewModelScope.launch {
            repository.notificationSocket.unreadCount.collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
        // A push tells us *something* changed; refetch so the new row (with its real id)
        // shows up in the list rather than fabricating one client-side.
        viewModelScope.launch {
            repository.notificationSocket.notifications.collect { load() }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            runCatching { repository.getUnreadCount() }
                .onSuccess { dto ->
                    _uiState.value = _uiState.value.copy(unreadCount = dto.count)
                }
                .onFailure { }
        }
    }

    fun setTypeFilter(type: NotificationType?) {
        _uiState.value = _uiState.value.copy(typeFilter = type)
        load()
    }

    fun load(isRefresh: Boolean = false) {
        val type = _uiState.value.typeFilter
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh, error = null)
            runCatching { repository.getNotifications(type) }
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false,
                        notifications = list,
                        unreadCount = list.count { !it.isRead },
                        typeFilter = type,
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.toDisplayMessage("Failed to load notifications")) }
        }
    }

    fun refresh() = load(isRefresh = true)

    fun markRead(id: String) {
        if (_uiState.value.notifications.find { it.id == id }?.isRead == true) return
        viewModelScope.launch {
            runCatching { repository.markNotificationRead(id) }
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { if (it.id == id) it.copy(isRead = true) else it },
                unreadCount = (_uiState.value.unreadCount - 1).coerceAtLeast(0),
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            runCatching { repository.markAllNotificationsRead() }
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { it.copy(isRead = true) },
                unreadCount = 0,
            )
        }
    }
}
