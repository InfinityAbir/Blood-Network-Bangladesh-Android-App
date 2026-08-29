package com.bloodnetwork.bangladesh.data.network

import com.bloodnetwork.bangladesh.data.model.AdminEligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.AuthResponse
import com.bloodnetwork.bangladesh.data.model.BloodGroup
import com.bloodnetwork.bangladesh.data.model.ChatRequest
import com.bloodnetwork.bangladesh.data.model.ChatResponse
import com.bloodnetwork.bangladesh.data.model.BloodRequestDto
import com.bloodnetwork.bangladesh.data.model.BloodRequestMatchDto
import com.bloodnetwork.bangladesh.data.model.CreateBloodRequestRequest
import com.bloodnetwork.bangladesh.data.model.CreateDonorProfileRequest
import com.bloodnetwork.bangladesh.data.model.DistrictDto
import com.bloodnetwork.bangladesh.data.model.DivisionDto
import com.bloodnetwork.bangladesh.data.model.DonorProfileDto
import com.bloodnetwork.bangladesh.data.model.EligibilityAnswerDto
import com.bloodnetwork.bangladesh.data.model.EligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.EligibilityResultDto
import com.bloodnetwork.bangladesh.data.model.EligibilityStateDto
import com.bloodnetwork.bangladesh.data.model.FirstLoginChangeRequest
import com.bloodnetwork.bangladesh.data.model.LoginRequest
import com.bloodnetwork.bangladesh.data.model.MarkNotificationReadRequest
import com.bloodnetwork.bangladesh.data.model.NotificationDto
import com.bloodnetwork.bangladesh.data.model.PagedResult
import com.bloodnetwork.bangladesh.data.model.PublicBloodRequestDto
import com.bloodnetwork.bangladesh.data.model.PublicDonorDto
import com.bloodnetwork.bangladesh.data.model.RefreshTokenRequest
import com.bloodnetwork.bangladesh.data.model.RegisterRequest
import com.bloodnetwork.bangladesh.data.model.RespondToMatchRequest
import com.bloodnetwork.bangladesh.data.model.SaveEligibilityQuestionRequest
import com.bloodnetwork.bangladesh.data.model.ToggleAvailabilityRequest
import com.bloodnetwork.bangladesh.data.model.ToggleEligibilityQuestionActiveRequest
import com.bloodnetwork.bangladesh.data.model.UnreadCountDto
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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BloodNetworkApi {

    // ---- Auth ----
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Unit

    @POST("api/auth/first-login-change")
    suspend fun firstLoginChange(@Body request: FirstLoginChangeRequest): Unit

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto

    @GET("api/auth/me")
    suspend fun me(): UserDto

    // ---- Donors ----
    @POST("api/donors/me/profile")
    suspend fun createDonorProfile(@Body request: CreateDonorProfileRequest): DonorProfileDto

    @PUT("api/donors/me/profile")
    suspend fun updateDonorProfile(@Body request: UpdateDonorProfileRequest): DonorProfileDto

    @GET("api/donors/me/profile")
    suspend fun getMyDonorProfile(): DonorProfileDto

    @PATCH("api/donors/me/availability")
    suspend fun toggleAvailability(@Body request: ToggleAvailabilityRequest): DonorProfileDto

    @GET("api/donors/search")
    suspend fun searchDonors(
        @Query("bloodGroup") bloodGroup: BloodGroup? = null,
        @Query("districtId") districtId: String? = null,
        @Query("upazilaId") upazilaId: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): PagedResult<PublicDonorDto>

    // ---- Blood Requests ----
    @POST("api/blood-requests")
    suspend fun createBloodRequest(@Body request: CreateBloodRequestRequest): BloodRequestDto

    @GET("api/blood-requests/{id}")
    suspend fun getBloodRequest(@Path("id") id: String): BloodRequestDto

    @GET("api/blood-requests/my")
    suspend fun getMyBloodRequests(): List<BloodRequestDto>

    @GET("api/blood-requests/open")
    suspend fun getOpenBloodRequests(
        @Query("bloodGroup") bloodGroup: BloodGroup? = null,
        @Query("districtId") districtId: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): PagedResult<PublicBloodRequestDto>

    @PATCH("api/blood-requests/{id}/cancel")
    suspend fun cancelBloodRequest(@Path("id") id: String): Unit

    @PATCH("api/blood-requests/{id}/fulfill")
    suspend fun fulfillBloodRequest(@Path("id") id: String): Unit

    // ---- Locations ----
    @GET("api/locations/divisions")
    suspend fun getDivisions(): List<DivisionDto>

    @GET("api/locations/districts")
    suspend fun getDistricts(@Query("divisionId") divisionId: String? = null): List<DistrictDto>

    @GET("api/locations/upazilas")
    suspend fun getUpazilas(@Query("districtId") districtId: String? = null): List<UpazilaDto>

    // ---- Matches ----
    @GET("api/matches/request/{requestId}")
    suspend fun getMatchesForRequest(@Path("requestId") requestId: String): List<BloodRequestMatchDto>

    @GET("api/matches/donor")
    suspend fun getDonorMatches(): List<BloodRequestMatchDto>

    @POST("api/matches/{matchId}/respond")
    suspend fun respondToMatch(
        @Path("matchId") matchId: String,
        @Body request: RespondToMatchRequest,
    ): BloodRequestMatchDto

    @POST("api/matches/request/{requestId}/trigger-match")
    suspend fun triggerMatch(@Path("requestId") requestId: String): Unit

    // ---- Notifications ----
    @GET("api/notifications")
    suspend fun getNotifications(@Query("type") type: String? = null): List<NotificationDto>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountDto

    @POST("api/notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: String,
        @Body request: MarkNotificationReadRequest,
    ): NotificationDto

    @POST("api/notifications/read-all")
    suspend fun markAllNotificationsRead(): Unit

    @DELETE("api/notifications/clear-all")
    suspend fun clearAllNotifications(): Unit

    @DELETE("api/notifications/{notificationId}")
    suspend fun deleteNotification(@Path("notificationId") notificationId: String): Unit

    // ---- Eligibility (public) ----
    // ---- Developer info (public "About" content, admin-editable) ----
    @GET("api/developer-info")
    suspend fun getDeveloperInfo(): DeveloperInfoDto

    @PUT("api/developer-info")
    suspend fun updateDeveloperInfo(@Body request: UpdateDeveloperInfoRequest): DeveloperInfoDto

    @GET("api/ai/eligibility/questions")
    suspend fun getEligibilityQuestions(): List<EligibilityQuestionDto>

    @POST("api/ai/eligibility/check")
    suspend fun checkEligibility(@Body answers: List<EligibilityAnswerDto>): EligibilityResultDto

    @GET("api/ai/eligibility/state")
    suspend fun getEligibilityState(): EligibilityStateDto?

    @DELETE("api/ai/eligibility/state")
    suspend fun clearEligibilityState(): Unit

    // ---- Eligibility (admin) ----
    @GET("api/admin/eligibility-questions")
    suspend fun getAdminEligibilityQuestions(): List<AdminEligibilityQuestionDto>

    @POST("api/admin/eligibility-questions")
    suspend fun createEligibilityQuestion(@Body request: SaveEligibilityQuestionRequest): AdminEligibilityQuestionDto

    @PUT("api/admin/eligibility-questions/{questionId}")
    suspend fun updateEligibilityQuestion(@Path("questionId") questionId: String, @Body request: SaveEligibilityQuestionRequest): AdminEligibilityQuestionDto

    @POST("api/admin/eligibility-questions/{questionId}/toggle-active")
    suspend fun toggleEligibilityQuestionActive(@Path("questionId") questionId: String, @Body request: ToggleEligibilityQuestionActiveRequest): AdminEligibilityQuestionDto

    @DELETE("api/admin/eligibility-questions/{questionId}")
    suspend fun deleteEligibilityQuestion(@Path("questionId") questionId: String): Unit

    // ---- AI chatbot (proxies Groq server-side; no API key in the app) ----
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    // ---- Admin ----
    @GET("api/admin/dashboard")
    suspend fun getAdminDashboard(): AdminDashboardStats

    @GET("api/admin/analytics")
    suspend fun getAdminAnalytics(): AdminAnalyticsDto

    @GET("api/admin/users")
    suspend fun getAdminUsers(
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("isActive") isActive: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): PagedResult<AdminUserDto>

    @POST("api/admin/users/{userId}/toggle-active")
    suspend fun toggleUserActive(
        @Path("userId") userId: String,
        @Body request: ToggleUserActiveRequest,
    ): AdminUserDto

    @POST("api/admin/users/{userId}/verify-donor")
    suspend fun verifyDonor(
        @Path("userId") userId: String,
        @Body request: VerifyDonorRequest,
    ): AdminUserDto

    @GET("api/admin/reports")
    suspend fun getAdminReports(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): PagedResult<AdminReportDto>

    @POST("api/admin/reports/{reportId}/resolve")
    suspend fun resolveReport(
        @Path("reportId") reportId: String,
        @Body request: ResolveReportRequest,
    ): AdminReportDto

    @GET("api/admin/audit-logs")
    suspend fun getAdminAuditLogs(
        @Query("entityType") entityType: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): PagedResult<AdminAuditLogDto>
}
