package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.EligibilityAnswerDto
import com.bloodnetwork.bangladesh.data.model.EligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.EligibilityResultDto
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EligibilityUiState(
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val questions: List<EligibilityQuestionDto> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val result: EligibilityResultDto? = null,
    val error: String? = null,
    val lastCheckedAnswers: Map<String, String>? = null,
)

class EligibilityViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EligibilityUiState())
    val uiState: StateFlow<EligibilityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Per-user bundle: each user (and guest) has isolated answers+result.
            // This survives logout/login for same user, but is never shown to other users.
            val bundle = repository.getEligibilityBundle()
            var answers = bundle.answers
            var result = bundle.result
            var lastChecked = bundle.lastCheckedAnswers

            // If local per-user bundle is empty and user is authenticated, try server (cross-device / website parity).
            if ((answers.isEmpty() && result == null) && repository.isLoggedInSync()) {
                val server = repository.getEligibilityState()
                if (server != null) {
                    val serverAnswers = server.answers.associate { it.questionId to it.answer }
                    answers = serverAnswers
                    result = server.result
                    lastChecked = serverAnswers
                    // Cache server state locally for same user
                    repository.saveEligibilityAnswers(serverAnswers)
                    repository.saveEligibilityResult(server.result)
                    repository.saveEligibilityLastCheckedAnswers(serverAnswers)
                }
            }

            _uiState.value = _uiState.value.copy(
                answers = answers,
                result = result,
                lastCheckedAnswers = lastChecked
            )
        }
    }

    fun loadQuestions() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getEligibilityQuestions() }
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, questions = it) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load eligibility questions")) }
        }
    }

    fun setAnswer(questionId: String, answer: String) {
        val newAnswers = _uiState.value.answers + (questionId to answer)
        _uiState.value = _uiState.value.copy(answers = newAnswers)
        viewModelScope.launch { repository.saveEligibilityAnswers(newAnswers) }
    }

    fun checkEligibility() {
        val state = _uiState.value
        val answers = state.answers
        val missing = state.questions.any { !answers.containsKey(it.id) }
        if (missing) {
            _uiState.value = state.copy(error = "Please answer all questions")
            return
        }
        _uiState.value = state.copy(isChecking = true, error = null)
        viewModelScope.launch {
            val payload = state.questions.map { EligibilityAnswerDto(it.id, answers[it.id] ?: "") }
            runCatching { repository.checkEligibility(payload) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(result = it, isChecking = false, lastCheckedAnswers = answers)
                    // Persist so result survives navigation / process death and user can review score again.
                    repository.saveEligibilityResult(it)
                    repository.saveEligibilityLastCheckedAnswers(answers)
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isChecking = false, error = e.toDisplayMessage("Failed to check eligibility")) }
        }
    }

    fun reset() {
        _uiState.value = EligibilityUiState(questions = _uiState.value.questions)
        viewModelScope.launch {
            repository.clearEligibilityAll()
            if (repository.isLoggedInSync()) repository.clearServerEligibilityState()
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null, lastCheckedAnswers = null)
        viewModelScope.launch {
            repository.clearEligibilityResult()
            repository.clearEligibilityLastCheckedAnswers()
            if (repository.isLoggedInSync()) repository.clearServerEligibilityState()
        }
    }
}
