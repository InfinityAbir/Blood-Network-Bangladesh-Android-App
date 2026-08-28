package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.ChatMessage
import com.bloodnetwork.bangladesh.data.model.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val loading: Boolean = false,
)

data class ChatbotUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<ChatUiMessage> = emptyList(),
)

class ChatbotViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isLoading) return

        val userMessage = ChatUiMessage(role = "user", content = trimmed)
        val loadingMessage = ChatUiMessage(role = "assistant", content = "", loading = true)

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage + loadingMessage,
            isLoading = true,
            error = null,
        )

        viewModelScope.launch {
            val history = _uiState.value.messages
                .filterNot { it.loading }
                .dropLast(1)
                .map { ChatMessage(role = it.role, content = it.content) }

            runCatching { repository.chat(ChatRequest(message = trimmed, history = history)) }
                .onSuccess { reply ->
                    val current = _uiState.value.messages.toMutableList()
                    // replace the loading bubble
                    if (current.isNotEmpty() && current.last().loading) current.removeAt(current.size - 1)
                    current.add(ChatUiMessage(role = "assistant", content = reply))
                    _uiState.value = _uiState.value.copy(messages = current, isLoading = false)
                }
                .onFailure { e ->
                    val current = _uiState.value.messages.toMutableList()
                    if (current.isNotEmpty() && current.last().loading) current.removeAt(current.size - 1)
                    current.add(ChatUiMessage(role = "assistant", content = "Sorry, I couldn't reach the service. Please try again."))
                    _uiState.value = _uiState.value.copy(messages = current, isLoading = false, error = e.message)
                }
        }
    }
}
