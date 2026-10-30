package com.bloodnetwork.bangladesh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.data.model.AvailabilityStatus
import com.bloodnetwork.bangladesh.data.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OnboardingStep { Eligibility, Profile, Availability, Done }

/**
 * Where a newly registered donor is in the "become an active donor" sequence:
 * 1) eligibility self-check, 2) donor profile, 3) availability.
 *
 * Existing donors are never nagged: having a profile counts as having passed step 1, and an
 * availability of Available *or* Unavailable counts as a deliberate choice for step 3 (only
 * the default "Unknown" a fresh profile starts with is treated as not yet set).
 */
data class DonorOnboardingState(
    /** First load in progress: the card shows a placeholder instead of guessing a step. */
    val loading: Boolean = true,
    /** False when the status couldn't be loaded (offline etc.) — the card stays hidden. */
    val known: Boolean = false,
    val eligibilityChecked: Boolean = false,
    val eligible: Boolean = false,
    val hasProfile: Boolean = false,
    val availability: AvailabilityStatus = AvailabilityStatus.Unknown,
) {
    val step: OnboardingStep
        get() = when {
            hasProfile -> if (availability == AvailabilityStatus.Unknown) OnboardingStep.Availability else OnboardingStep.Done
            eligible -> OnboardingStep.Profile
            else -> OnboardingStep.Eligibility
        }

    /** Checked, but the answers say they can't donate right now. */
    val notEligibleYet: Boolean get() = !hasProfile && eligibilityChecked && !eligible
}

class DonorOnboardingViewModel(
    private val repository: BloodNetworkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DonorOnboardingState())
    val state: StateFlow<DonorOnboardingState> = _state.asStateFlow()

    /** Re-reads server/local state; call whenever the dashboard comes back to the foreground. */
    fun refresh() {
        viewModelScope.launch {
            val profileResult = runCatching { repository.getMyDonorProfile() }
            val profile = profileResult.getOrNull()
            val noProfileYet = (profileResult.exceptionOrNull() as? ApiException)?.code == 404
            if (profile == null && !noProfileYet) {
                // Network / server problem: don't show a possibly wrong step.
                _state.value = _state.value.copy(loading = false)
                return@launch
            }

            var result = repository.getEligibilityBundle().result
            if (result == null && profile == null) {
                // Answered on another device / the website: the server keeps the signed-in copy.
                val server = repository.getEligibilityState()
                if (server != null) {
                    result = server.result
                    repository.saveEligibilityAnswers(server.answers.associate { it.questionId to it.answer })
                    repository.saveEligibilityResult(server.result)
                }
            }

            _state.value = DonorOnboardingState(
                loading = false,
                known = true,
                eligibilityChecked = result != null,
                eligible = result?.isEligible == true,
                hasProfile = profile != null,
                availability = profile?.availabilityStatus ?: AvailabilityStatus.Unknown,
            )
        }
    }
}
