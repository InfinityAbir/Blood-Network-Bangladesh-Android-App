package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.ChatMessage
import com.bloodnetwork.bangladesh.data.model.ChatRequest
import com.bloodnetwork.bangladesh.data.network.ApiException
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiMessage(
    val id: Long,
    val role: String, // "user" or "assistant"
    val content: String,
    val loading: Boolean = false,
    val isError: Boolean = false,
)

data class ChatbotUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<ChatUiMessage> = emptyList(),
    /** Seconds until the next message may be sent (spam throttle / server rate limit). 0 = ready. */
    val cooldownSeconds: Int = 0,
    val isSignedIn: Boolean = true,
)

class ChatbotViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState(isSignedIn = repository.isLoggedInSync()))
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private var nextId = 0L
    private var cooldownJob: Job? = null

    /** The last user text that failed, so "Retry" can resend it without retyping. */
    private var lastFailedText: String? = null

    fun refreshSignInState() {
        val signedIn = repository.isLoggedInSync()
        if (signedIn != _uiState.value.isSignedIn) _uiState.value = _uiState.value.copy(isSignedIn = signedIn)
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim().take(MAX_MESSAGE_LENGTH)
        val state = _uiState.value
        if (trimmed.isEmpty() || state.isLoading || state.cooldownSeconds > 0 || !state.isSignedIn) return

        val userMessage = ChatUiMessage(id = nextId++, role = "user", content = trimmed)
        val loadingMessage = ChatUiMessage(id = nextId++, role = "assistant", content = "", loading = true)

        // History sent to the server: only real turns (no error bubbles), bounded in count and
        // size, taken before the new user message is appended.
        val history = state.messages
            .filterNot { it.loading || it.isError }
            .takeLast(MAX_HISTORY_MESSAGES)
            .map { ChatMessage(role = it.role, content = it.content.take(MAX_HISTORY_CHARS)) }

        lastFailedText = null
        _uiState.value = state.copy(
            messages = state.messages + userMessage + loadingMessage,
            isLoading = true,
            error = null,
        )
        startCooldown(MIN_SEND_INTERVAL_SECONDS)

        viewModelScope.launch {
            runCatching { repository.chat(ChatRequest(message = trimmed, history = history)) }
                .onSuccess { reply ->
                    replaceLoading(
                        ChatUiMessage(
                            id = nextId++,
                            role = "assistant",
                            content = reply.trim().take(MAX_REPLY_CHARS).ifBlank { EMPTY_REPLY },
                        ),
                    )
                }
                .onFailure { e ->
                    lastFailedText = trimmed
                    val rateLimited = e is ApiException && e.code == 429
                    val unauthorized = e is ApiException && e.code == 401
                    if (rateLimited) startCooldown(RATE_LIMIT_COOLDOWN_SECONDS)
                    replaceLoading(
                        ChatUiMessage(id = nextId++, role = "assistant", content = "", isError = true),
                        error = when {
                            rateLimited -> RATE_LIMITED
                            unauthorized -> SESSION_EXPIRED
                            else -> e.toDisplayMessage("Failed to get a response")
                        },
                    )
                    if (unauthorized) refreshSignInState()
                }
        }
    }

    /** Re-sends the message that last failed (the failed turn is dropped first). */
    fun retryLast() {
        val text = lastFailedText ?: return
        val msgs = _uiState.value.messages.toMutableList()
        // drop the error bubble and the user message that produced it
        if (msgs.lastOrNull()?.isError == true) msgs.removeAt(msgs.size - 1)
        if (msgs.lastOrNull()?.role == "user") msgs.removeAt(msgs.size - 1)
        _uiState.value = _uiState.value.copy(messages = msgs, error = null)
        sendMessage(text)
    }

    fun clearChat() {
        lastFailedText = null
        _uiState.value = _uiState.value.copy(messages = emptyList(), error = null, isLoading = false)
    }

    private fun replaceLoading(replacement: ChatUiMessage, error: String? = null) {
        val current = _uiState.value.messages.toMutableList()
        if (current.isNotEmpty() && current.last().loading) current.removeAt(current.size - 1)
        current.add(replacement)
        _uiState.value = _uiState.value.copy(messages = current, isLoading = false, error = error)
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var left = seconds
            while (left > 0) {
                _uiState.value = _uiState.value.copy(cooldownSeconds = left)
                delay(1000)
                left--
            }
            _uiState.value = _uiState.value.copy(cooldownSeconds = 0)
        }
    }

    companion object {
        const val MAX_MESSAGE_LENGTH = 500
        private const val MAX_HISTORY_MESSAGES = 10
        private const val MAX_HISTORY_CHARS = 1000
        private const val MAX_REPLY_CHARS = 4000
        private const val MIN_SEND_INTERVAL_SECONDS = 2
        private const val RATE_LIMIT_COOLDOWN_SECONDS = 30
        private const val EMPTY_REPLY = "…"
        private const val RATE_LIMITED = "You're sending messages too fast. Please wait a moment."
        private const val SESSION_EXPIRED = "Your session expired. Please sign in again."
    }
}
