package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.AdminEligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.SaveEligibilityQuestionRequest
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminEligibilityUiState(
    val isLoading: Boolean = false,
    val questions: List<AdminEligibilityQuestionDto> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

class AdminEligibilityViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminEligibilityUiState())
    val uiState: StateFlow<AdminEligibilityUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getAdminEligibilityQuestions() }
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        questions = list.sortedBy { it.displayOrder },
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.toDisplayMessage("Failed to load questions")) }
        }
    }

    fun create(request: SaveEligibilityQuestionRequest, onDone: (Boolean) -> Unit) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching { repository.createEligibilityQuestion(request) }
                .onSuccess { created ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        questions = (_uiState.value.questions + created).sortedBy { it.displayOrder },
                        successMessage = "Question added",
                    )
                    onDone(true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = e.toDisplayMessage("Failed to add question"))
                    onDone(false)
                }
        }
    }

    fun update(id: String, request: SaveEligibilityQuestionRequest, onDone: (Boolean) -> Unit) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching { repository.updateEligibilityQuestion(id, request) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        questions = _uiState.value.questions.map { if (it.id == id) updated else it }.sortedBy { it.displayOrder },
                        successMessage = "Question updated",
                    )
                    onDone(true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = e.toDisplayMessage("Failed to update question"))
                    onDone(false)
                }
        }
    }

    fun toggleActive(id: String, isActive: Boolean) {
        val previous = _uiState.value.questions
        _uiState.value = _uiState.value.copy(
            questions = previous.map { if (it.id == id) it.copy(isActive = isActive) else it },
        )
        viewModelScope.launch {
            runCatching { repository.toggleEligibilityQuestionActive(id, isActive) }
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        questions = _uiState.value.questions.map { if (it.id == id) updated else it },
                        successMessage = if (isActive) "Question activated" else "Question deactivated",
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(questions = previous, error = e.toDisplayMessage("Action failed")) }
        }
    }

    fun delete(id: String) {
        val previous = _uiState.value.questions
        _uiState.value = _uiState.value.copy(questions = previous.filter { it.id != id })
        viewModelScope.launch {
            runCatching { repository.deleteEligibilityQuestion(id) }
                .onSuccess { _uiState.value = _uiState.value.copy(successMessage = "Question deleted") }
                .onFailure { e -> _uiState.value = _uiState.value.copy(questions = previous, error = e.toDisplayMessage("Failed to delete question")) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
