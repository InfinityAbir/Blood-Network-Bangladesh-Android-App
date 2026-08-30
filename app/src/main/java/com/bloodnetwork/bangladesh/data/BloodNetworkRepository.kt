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
import com.bloodnetwork.bangladesh.data.model.NotificationType
import com.bloodnetwork.bangladesh.data.model.PagedResult
import com.bloodnetwork.bangladesh.data.model.PublicBloodRequestDto
import com.bloodnetwork.bangladesh.data.model.PublicDonorDto
import com.bloodnetwork.bangladesh.data.model.RefreshTokenRequest
import com.bloodnetwork.bangladesh.data.model.RegisterRequest
import com.bloodnetwork.bangladesh.data.model.RespondToMatchRequest
import com.bloodnetwork.bangladesh.data.model.ToggleAvailabilityRequest
import com.bloodnetwork.bangladesh.data.model.UnreadCountDto
import com.bloodnetwork.bangladesh.data.model.UpdateBloodRequestRequest
import com.bloodnetwork.bangladesh.data.model.UpdateDonorProfileRequest
import com.bloodnetwork.bangladesh.data.model.UpazilaDto
import com.bloodnetwork.bangladesh.data.model.UpdateProfileRequest
import com.bloodnetwork.bangladesh.data.model.UserDto
import com.bloodnetwork.bangladesh.data.model.AdminAnalyticsDto
import com.bloodnetwork.bangladesh.data.model.DeveloperInfoDto
import com.bloodnetwork.bangladesh.data.model.UpdateDeveloperInfoRequest
import com.bloodnetwork.bangladesh.data.model.AdminDashboardStats
import com.bloodnetwork.bangladesh.data.model.AdminUserDto
import com.bloodnetwork.bangladesh.data.model.AdminReportDto
import com.bloodnetwork.bangladesh.data.model.AdminAuditLogDto
import com.bloodnetwork.bangladesh.data.model.ToggleUserActiveRequest
import com.bloodnetwork.bangladesh.data.model.VerifyDonorRequest
import com.bloodnetwork.bangladesh.data.model.ResolveReportRequest
import com.bloodnetwork.bangladesh.data.network.BloodNetworkApi
import com.bloodnetwork.bangladesh.data.network.NotificationSocket
import com.bloodnetwork.bangladesh.data.prefs.DonorProfileStore
import com.bloodnetwork.bangladesh.data.prefs.EligibilityStore
import com.bloodnetwork.bangladesh.data.prefs.RegistrationStore
import com.bloodnetwork.bangladesh.data.prefs.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BloodNetworkRepository(
    private val api: BloodNetworkApi,
    private val tokenStore: TokenStore,
    private val eligibilityStore: EligibilityStore,
    private val registrationStore: RegistrationStore,
    private val donorProfileStore: DonorProfileStore,
    val notificationSocket: NotificationSocket,
) {

    val isLoggedIn: Flow<Boolean> = tokenStore.isLoggedIn
    val currentUserId: Flow<String?> = tokenStore.currentUserId
    val currentUserRole: Flow<com.bloodnetwork.bangladesh.data.model.UserRole?> = tokenStore.currentUserRole
    fun isLoggedInSync(): Boolean = tokenStore.isLoggedInSync()
    fun currentUserRoleSync(): com.bloodnetwork.bangladesh.data.model.UserRole? = tokenStore.currentUserRoleSync()

    // ---- Auth ----
    suspend fun login(phone: String, password: String): AuthResponse {
        val auth = api.login(LoginRequest(phoneNumber = phone, password = password))
        tokenStore.saveSession(auth)
        // Eligibility is now per-user (keyed by userId), so no need to clear it on login —
        // each user sees only their own bundle (guest="guest", user="id"). This preserves
        // a user's own answers+result across logout/login while still isolating from other users.
        registrationStore.clear()
        donorProfileStore.clear()
        return auth
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        val auth = api.register(request)
        tokenStore.saveSession(auth)
        registrationStore.clear()
        donorProfileStore.clear()
        return auth
    }

    suspend fun logout() {
        // Best-effort server-side revocation so a captured refresh token can't outlive
        // the user's own logout; local tokens are cleared regardless of the outcome.
        val refreshToken = tokenStore.refreshToken.first()
        if (!refreshToken.isNullOrEmpty()) {
            runCatching { api.logout(RefreshTokenRequest(refreshToken)) }
        }
        tokenStore.clear()
        // Registration/donor drafts are device-keyed and cleared on logout to prevent next user seeing previous draft.
        // Eligibility is per-user (guest vs userId) so we KEEP it — same user logging back in will find their own bundle.
        registrationStore.clear()
        donorProfileStore.clear()
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

    suspend fun getMyBloodRequests(page: Int = 1, pageSize: Int = 20) = api.getMyBloodRequests(page, pageSize)

    suspend fun getBloodRequest(id: String): BloodRequestDto = api.getBloodRequest(id)

    suspend fun updateBloodRequest(id: String, request: UpdateBloodRequestRequest): BloodRequestDto =
        api.updateBloodRequest(id, request)

    suspend fun cancelBloodRequest(id: String) = api.cancelBloodRequest(id)

    suspend fun fulfillBloodRequest(id: String, unitsFulfilled: Int, notes: String? = null): BloodRequestDto =
        api.fulfillBloodRequest(id, com.bloodnetwork.bangladesh.data.model.FulfillBloodRequestRequest(unitsFulfilled, notes))

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
    suspend fun getNotifications(type: NotificationType? = null): List<NotificationDto> = api.getNotifications(type?.name)
    suspend fun getUnreadCount(): UnreadCountDto = api.getUnreadCount()
    suspend fun markNotificationRead(id: String) = api.markNotificationRead(id, com.bloodnetwork.bangladesh.data.model.MarkNotificationReadRequest(isRead = true))
    suspend fun markAllNotificationsRead() = api.markAllNotificationsRead()
    suspend fun deleteNotification(id: String) = api.deleteNotification(id)
    suspend fun clearAllNotifications() = api.clearAllNotifications()

    // ---- Eligibility ----
    suspend fun getEligibilityQuestions(): List<EligibilityQuestionDto> = api.getEligibilityQuestions()
    suspend fun checkEligibility(answers: List<EligibilityAnswerDto>): EligibilityResultDto =
        api.checkEligibility(answers)

    suspend fun getEligibilityState(): com.bloodnetwork.bangladesh.data.model.EligibilityStateDto? =
        try { api.getEligibilityState() } catch (_: Exception) { null }
    suspend fun clearServerEligibilityState() {
        try { api.clearEligibilityState() } catch (_: Exception) { }
    }

    // ---- AI Chatbot ----
    suspend fun chat(request: ChatRequest): String = api.chat(request).reply

    // ---- Eligibility Store — per-user (guest="guest", authenticated=userId) ----
    // Legacy flows (guest only) kept for one migration release; new code uses per-user bundle.
    val eligibilityAnswers: Flow<Map<String, String>> = eligibilityStore.answers
    val eligibilityResult: Flow<EligibilityResultDto?> = eligibilityStore.result
    val eligibilityLastCheckedAnswers: Flow<Map<String, String>?> = eligibilityStore.lastCheckedAnswers
    val eligibilityOwnerId: Flow<String?> = eligibilityStore.ownerUserId

    suspend fun getEligibilityBundle() = eligibilityStore.getBundle(tokenStore.currentUserId.first())

    suspend fun saveEligibilityAnswers(answers: Map<String, String>) {
        eligibilityStore.saveAnswers(tokenStore.currentUserId.first(), answers)
    }

    suspend fun saveEligibilityResult(result: EligibilityResultDto) {
        eligibilityStore.saveResult(tokenStore.currentUserId.first(), result)
    }

    suspend fun saveEligibilityLastCheckedAnswers(answers: Map<String, String>) {
        eligibilityStore.saveLastCheckedAnswers(tokenStore.currentUserId.first(), answers)
    }

    suspend fun clearEligibilityAnswers() {
        eligibilityStore.clearAnswersForUser(tokenStore.currentUserId.first())
    }

    suspend fun clearEligibilityResult() {
        // clear only result/lastChecked for current user, keep answers if needed; use bundle copy
        val bundle = eligibilityStore.getBundle(tokenStore.currentUserId.first())
        eligibilityStore.saveBundleForCurrentUser(bundle.copy(result = null, lastCheckedAnswers = null), tokenStore.currentUserId.first())
    }

    suspend fun clearEligibilityLastCheckedAnswers() {
        val bundle = eligibilityStore.getBundle(tokenStore.currentUserId.first())
        eligibilityStore.saveBundleForCurrentUser(bundle.copy(lastCheckedAnswers = null), tokenStore.currentUserId.first())
    }

    suspend fun clearEligibilityAll() {
        // Only clear current user's bundle, not all users, to preserve other users' data.
        eligibilityStore.clearForUser(tokenStore.currentUserId.first())
    }

    suspend fun clearAllEligibilityForAllUsers() = eligibilityStore.clearAll()

    suspend fun clearEligibilityIfOwnerMismatch() {
        // No-op now that store is per-user; isolation is automatic. Kept for ViewModel compat.
    }

    // ---- Admin: Eligibility Questions ----
    suspend fun getAdminEligibilityQuestions(): List<com.bloodnetwork.bangladesh.data.model.AdminEligibilityQuestionDto> =
        api.getAdminEligibilityQuestions()
    suspend fun createEligibilityQuestion(request: com.bloodnetwork.bangladesh.data.model.SaveEligibilityQuestionRequest) =
        api.createEligibilityQuestion(request)
    suspend fun updateEligibilityQuestion(id: String, request: com.bloodnetwork.bangladesh.data.model.SaveEligibilityQuestionRequest) =
        api.updateEligibilityQuestion(id, request)
    suspend fun toggleEligibilityQuestionActive(id: String, isActive: Boolean) =
        api.toggleEligibilityQuestionActive(id, com.bloodnetwork.bangladesh.data.model.ToggleEligibilityQuestionActiveRequest(isActive))
    suspend fun deleteEligibilityQuestion(id: String) = api.deleteEligibilityQuestion(id)

    // ---- Registration Store ----
    val registrationData: Flow<RegistrationStore.RegistrationData> = registrationStore.data
    suspend fun saveRegistrationData(data: RegistrationStore.RegistrationData) = registrationStore.save(data)
    suspend fun clearRegistrationData() = registrationStore.clear()

    // ---- Donor Profile Store ----
    val donorProfileData: Flow<DonorProfileStore.DonorProfileData> = donorProfileStore.data
    suspend fun saveDonorProfileData(data: DonorProfileStore.DonorProfileData) = donorProfileStore.save(data)
    suspend fun clearDonorProfileData() = donorProfileStore.clear()

    // ---- Profile ----
    suspend fun updateProfile(request: UpdateProfileRequest): UserDto = api.updateProfile(request)

    // ---- Admin ----
    suspend fun getAdminDashboard(): AdminDashboardStats = api.getAdminDashboard()
    suspend fun getAdminAnalytics(): AdminAnalyticsDto = api.getAdminAnalytics()

    // ---- Developer info ----
    suspend fun getDeveloperInfo(): DeveloperInfoDto = api.getDeveloperInfo()
    suspend fun updateDeveloperInfo(request: UpdateDeveloperInfoRequest): DeveloperInfoDto = api.updateDeveloperInfo(request)
    suspend fun getAdminUsers(search: String? = null, role: String? = null, isActive: Boolean? = null, page: Int = 1, pageSize: Int = 10) = api.getAdminUsers(search, role, isActive, page, pageSize)
    suspend fun toggleUserActive(userId: String, isActive: Boolean): AdminUserDto = api.toggleUserActive(userId, ToggleUserActiveRequest(isActive))
    suspend fun verifyDonor(userId: String, status: String): AdminUserDto = api.verifyDonor(userId, VerifyDonorRequest(status))
    suspend fun getAdminReports(status: String? = null, page: Int = 1, pageSize: Int = 10) = api.getAdminReports(status, page, pageSize)
    suspend fun resolveReport(reportId: String, status: String, resolution: String? = null): AdminReportDto = api.resolveReport(reportId, ResolveReportRequest(status, resolution))
    suspend fun getAdminAuditLogs(entityType: String? = null, page: Int = 1, pageSize: Int = 10) = api.getAdminAuditLogs(entityType, page, pageSize)
    suspend fun getAdminMatches(response: String? = null, page: Int = 1, pageSize: Int = 10) = api.getAdminMatches(response, page, pageSize)
    suspend fun getAdminBloodRequests(status: String? = null, bloodGroup: BloodGroup? = null, page: Int = 1, pageSize: Int = 10) = api.getAdminBloodRequests(status, bloodGroup, page, pageSize)
}
