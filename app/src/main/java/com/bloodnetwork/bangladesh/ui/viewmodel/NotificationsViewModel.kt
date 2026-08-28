package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
)

class NotificationsViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadUnreadCount() {
        viewModelScope.launch {
            runCatching { repository.getUnreadCount() }
                .onSuccess { dto ->
                    _uiState.value = _uiState.value.copy(unreadCount = dto.count)
                }
                .onFailure { }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching { repository.getNotifications() }
                .onSuccess { list ->
                    _uiState.value = NotificationsUiState(
                        notifications = list,
                        unreadCount = list.count { !it.isRead },
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
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
