package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    @SerialName("id") val id: String = "",
    @SerialName("type") val type: NotificationType = NotificationType.System,
    @SerialName("title") val title: String = "",
    @SerialName("message") val message: String = "",
    @SerialName("relatedEntityId") val relatedEntityId: String? = null,
    @SerialName("isRead") val isRead: Boolean = false,
    @SerialName("readAt") val readAt: String? = null,
    @SerialName("createdAt") val createdAt: String = "",
    @SerialName("metadata") val metadata: String? = null,
)

/** Best-effort read of the `availabilityStatus` field from an Availability notification's
 * JSON metadata (see backend DonorService.NotifyRequestersOfAvailabilityAsync). */
fun NotificationDto.metadataAvailabilityStatus(): String? =
    metadata?.let { runCatching { org.json.JSONObject(it).opt("availabilityStatus") as? String }.getOrNull() }

fun NotificationDto.metadataBloodGroup(): String? =
    metadata?.let { runCatching { org.json.JSONObject(it).opt("bloodGroup") as? String }.getOrNull() }

@Serializable
data class MarkNotificationReadRequest(
    @SerialName("isRead") val isRead: Boolean = true,
)

@Serializable
data class UnreadCountDto(
    @SerialName("count") val count: Int = 0,
)

@Serializable
data class BloodRequestMatchDto(
    @SerialName("id") val id: String = "",
    @SerialName("bloodRequestId") val bloodRequestId: String = "",
    @SerialName("donorId") val donorId: String = "",
    @SerialName("donorName") val donorName: String = "",
    @SerialName("donorPhone") val donorPhone: String = "",
    @SerialName("donorBloodGroup") val donorBloodGroup: String = "",
    @SerialName("donorPhotoUrl") val donorPhotoUrl: String? = null,
    @SerialName("hospitalName") val hospitalName: String = "",
    @SerialName("requesterName") val requesterName: String = "",
    @SerialName("requesterPhone") val requesterPhone: String = "",
    @SerialName("matchScore") val matchScore: Int = 0,
    @SerialName("distanceKm") val distanceKm: Double? = null,
    @SerialName("donorResponse") val donorResponse: DonorResponse = DonorResponse.Pending,
    @SerialName("contactedAt") val contactedAt: String? = null,
    @SerialName("respondedAt") val respondedAt: String? = null,
    @SerialName("acceptedAt") val acceptedAt: String? = null,
    @SerialName("createdAt") val createdAt: String = "",
)

@Serializable
data class RespondToMatchRequest(
    @SerialName("response") val response: DonorResponse,
)

@Serializable
data class PagedResult<T>(
    @SerialName("items") val items: List<T> = emptyList(),
    @SerialName("totalCount") val totalCount: Int = 0,
    @SerialName("page") val page: Int = 1,
    @SerialName("pageSize") val pageSize: Int = 20,
    @SerialName("totalPages") val totalPages: Int = 0,
    @SerialName("hasPrevious") val hasPrevious: Boolean = false,
    @SerialName("hasNext") val hasNext: Boolean = false,
)
