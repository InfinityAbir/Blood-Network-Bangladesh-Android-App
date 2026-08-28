package com.bloodnetwork.bangladesh.data

import com.bloodnetwork.bangladesh.data.model.AuthResponse
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.BloodRequestDto
import com.bloodnetwork.bangladesh.data.model.BloodRequestMatchDto
import com.bloodnetwork.bangladesh.data.model.ChatRequest
import com.bloodnetwork.bangladesh.data.model.CreateBloodRequestRequest
import com.bloodnetwork.bangladesh.data.model.CreateDonorProfileRequest
import com.bloodnetwork.bangladesh.data.model.DistrictDto
import com.bloodnetwork.bangladesh.data.model.DivisionDto
import com.bloodnetwork.bangladesh.data.model.DonorProfileDto
import com.bloodnetwork.bangladesh.data.model.EligibilityAnswerDto
import com.bloodnetwork.bangladesh.data.model.EligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.EligibilityResultDto
import com.bloodnetwork.bangladesh.data.model.LoginRequest
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.data.model.PagedResult
import com.bloodnetwork.bangladesh.data.model.PublicBloodRequestDto
import com.bloodnetwork.bangladesh.data.model.PublicDonorDto
import com.bloodnetwork.bangladesh.data.model.RegisterRequest
import com.bloodnetwork.bangladesh.data.model.RespondToMatchRequest
import com.bloodnetwork.bangladesh.data.model.ToggleAvailabilityRequest
import com.bloodnetwork.bangladesh.data.model.UnreadCountDto
import com.bloodnetwork.bangladesh.data.model.UpdateDonorProfileRequest
import com.bloodnetwork.bangladesh.data.model.UpazilaDto
import com.bloodnetwork.bangladesh.data.model.UserDto
import com.bloodnetwork.bangladesh.data.network.BloodNetworkApi
import com.bloodnetwork.bangladesh.data.prefs.DonorProfileStore
import com.bloodnetwork.bangladesh.data.prefs.EligibilityStore
import com.bloodnetwork.bangladesh.data.prefs.RegistrationStore
import com.bloodnetwork.bangladesh.data.prefs.TokenStore
import kotlinx.coroutines.flow.Flow

class BloodNetworkRepository(
    private val api: BloodNetworkApi,
    private val tokenStore: TokenStore,
    private val eligibilityStore: EligibilityStore,
    private val registrationStore: RegistrationStore,
    private val donorProfileStore: DonorProfileStore,
) {

    val isLoggedIn: Flow<Boolean> = tokenStore.isLoggedIn
    val currentUserId: Flow<String?> = tokenStore.currentUserId
    val currentUserRole: Flow<com.bloodnetwork.bangladesh.data.model.UserRole?> = tokenStore.currentUserRole

    // ---- Auth ----
    suspend fun login(phone: String, password: String): AuthResponse {
        val auth = api.login(LoginRequest(phoneNumber = phone, password = password))
        tokenStore.saveSession(auth)
        registrationStore.clear()
        return auth
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        val auth = api.register(request)
        tokenStore.saveSession(auth)
        registrationStore.clear()
        return auth
    }

    suspend fun logout() {
        tokenStore.clear()
    }

    suspend fun me(): UserDto = api.me()

    // ---- Donors ----
    suspend fun createDonorProfile(request: CreateDonorProfileRequest): DonorProfileDto =
        api.createDonorProfile(request)

    suspend fun getMyDonorProfile(): DonorProfileDto = api.getMyDonorProfile()

    suspend fun updateDonorProfile(request: UpdateDonorProfileRequest): DonorProfileDto =
        api.updateDonorProfile(request)

    suspend fun toggleAvailability(status: ToggleAvailabilityRequest): DonorProfileDto =
        api.toggleAvailability(status)

    suspend fun searchDonors(
        bloodGroup: BloodGroup? = null,
        districtId: String? = null,
        upazilaId: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): PagedResult<PublicDonorDto> =
        api.searchDonors(bloodGroup, districtId, upazilaId, page, pageSize)

    // ---- Blood Requests ----
    suspend fun createBloodRequest(request: CreateBloodRequestRequest): BloodRequestDto =
        api.createBloodRequest(request)

    suspend fun getMyBloodRequests(): List<BloodRequestDto> = api.getMyBloodRequests()

    suspend fun getOpenBloodRequests(
        bloodGroup: BloodGroup? = null,
        districtId: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): PagedResult<PublicBloodRequestDto> =
        api.getOpenBloodRequests(bloodGroup, districtId, page, pageSize)

    suspend fun getMatchesForRequest(requestId: String): List<BloodRequestMatchDto> =
        api.getMatchesForRequest(requestId)

    suspend fun getDonorMatches(): List<BloodRequestMatchDto> = api.getDonorMatches()

    suspend fun respondToMatch(matchId: String, response: RespondToMatchRequest): BloodRequestMatchDto =
        api.respondToMatch(matchId, response)

    // ---- Locations ----
    suspend fun getDivisions(): List<DivisionDto> = api.getDivisions()
    suspend fun getDistricts(divisionId: String? = null): List<DistrictDto> = api.getDistricts(divisionId)
    suspend fun getUpazilas(districtId: String? = null): List<UpazilaDto> = api.getUpazilas(districtId)

    // ---- Notifications ----
    suspend fun getNotifications(): List<NotificationDto> = api.getNotifications()
    suspend fun getUnreadCount(): UnreadCountDto = api.getUnreadCount()
    suspend fun markNotificationRead(id: String) = api.markNotificationRead(id, com.bloodnetwork.bangladesh.data.model.MarkNotificationReadRequest(isRead = true))
    suspend fun markAllNotificationsRead() = api.markAllNotificationsRead()

    // ---- Eligibility ----
    suspend fun getEligibilityQuestions(): List<EligibilityQuestionDto> = api.getEligibilityQuestions()
    suspend fun checkEligibility(answers: List<EligibilityAnswerDto>): EligibilityResultDto =
        api.checkEligibility(answers)

    // ---- AI Chatbot ----
    suspend fun chat(request: ChatRequest): String = api.chat(request).reply

    // ---- Eligibility Store ----
    val eligibilityAnswers: Flow<Map<Int, String>> = eligibilityStore.answers
    suspend fun saveEligibilityAnswers(answers: Map<Int, String>) = eligibilityStore.saveAnswers(answers)
    suspend fun clearEligibilityAnswers() = eligibilityStore.clearAnswers()

    // ---- Registration Store ----
    val registrationData: Flow<RegistrationStore.RegistrationData> = registrationStore.data
    suspend fun saveRegistrationData(data: RegistrationStore.RegistrationData) = registrationStore.save(data)
    suspend fun clearRegistrationData() = registrationStore.clear()

    // ---- Donor Profile Store ----
    val donorProfileData: Flow<DonorProfileStore.DonorProfileData> = donorProfileStore.data
    suspend fun saveDonorProfileData(data: DonorProfileStore.DonorProfileData) = donorProfileStore.save(data)
    suspend fun clearDonorProfileData() = donorProfileStore.clear()
}
