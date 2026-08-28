package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.EligibilityAnswerDto
import com.bloodnetwork.bangladesh.data.model.EligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.EligibilityResultDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EligibilityUiState(
    val isLoading: Boolean = false,
    val questions: List<EligibilityQuestionDto> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val result: EligibilityResultDto? = null,
    val error: String? = null,
)

class EligibilityViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EligibilityUiState())
    val uiState: StateFlow<EligibilityUiState> = _uiState.asStateFlow()

    fun loadQuestions() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { repository.getEligibilityQuestions() }
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, questions = it) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    fun setAnswer(questionId: Int, answer: String) {
        _uiState.value = _uiState.value.copy(answers = _uiState.value.answers + (questionId to answer))
    }

    fun checkEligibility() {
        val state = _uiState.value
        val answers = state.answers
        val missing = state.questions.any { !answers.containsKey(it.id) }
        if (missing) {
            _uiState.value = state.copy(error = "Please answer all questions")
            return
        }
        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val payload = state.questions.map { EligibilityAnswerDto(it.id, answers[it.id] ?: "") }
            runCatching { repository.checkEligibility(payload) }
                .onSuccess { _uiState.value = _uiState.value.copy(result = it, isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    fun reset() {
        _uiState.value = EligibilityUiState(questions = _uiState.value.questions)
    }
}
