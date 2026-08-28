package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminDashboardStats(
    @SerialName("totalUsers") val totalUsers: Int = 0,
    @SerialName("totalDonors") val totalDonors: Int = 0,
    @SerialName("totalRequests") val totalRequests: Int = 0,
    @SerialName("totalMatches") val totalMatches: Int = 0,
    @SerialName("pendingReports") val pendingReports: Int = 0,
    @SerialName("recentActivity") val recentActivity: List<String> = emptyList(),
)

@Serializable
data class AdminUserDto(
    @SerialName("id") val id: String = "",
    @SerialName("firstName") val firstName: String = "",
    @SerialName("lastName") val lastName: String = "",
    @SerialName("phoneNumber") val phoneNumber: String = "",
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: UserRole = UserRole.Requester,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("createdAt") val createdAt: String = "",
)

@Serializable
data class AdminReportDto(
    @SerialName("id") val id: String = "",
    @SerialName("reporterUserId") val reporterUserId: String = "",
    @SerialName("reportedUserId") val reportedUserId: String = "",
    @SerialName("reason") val reason: String = "",
    @SerialName("status") val status: String = "Pending",
    @SerialName("createdAt") val createdAt: String = "",
)

@Serializable
data class AdminAuditLogDto(
    @SerialName("id") val id: String = "",
    @SerialName("userId") val userId: String = "",
    @SerialName("action") val action: String = "",
    @SerialName("entityType") val entityType: String = "",
    @SerialName("entityId") val entityId: String = "",
    @SerialName("timestamp") val timestamp: String = "",
)

@Serializable
data class ToggleUserActiveRequest(
    @SerialName("isActive") val isActive: Boolean,
)

@Serializable
data class ResolveReportRequest(
    @SerialName("status") val status: String,
    @SerialName("resolution") val resolution: String? = null,
)
