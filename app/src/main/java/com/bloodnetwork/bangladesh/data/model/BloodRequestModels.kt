package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBloodRequestRequest(
    @SerialName("bloodGroup") val bloodGroup: BloodGroup,
    @SerialName("unitsRequired") val unitsRequired: Int,
    @SerialName("hospitalName") val hospitalName: String,
    @SerialName("hospitalAddress") val hospitalAddress: String,
    @SerialName("districtId") val districtId: String,
    @SerialName("upazilaId") val upazilaId: String,
    @SerialName("area") val area: String? = null,
    @SerialName("requiredBy") val requiredBy: String,
    @SerialName("urgency") val urgency: Urgency,
    @SerialName("patientName") val patientName: String? = null,
    @SerialName("patientRelation") val patientRelation: String? = null,
    @SerialName("contactPhone") val contactPhone: String,
    @SerialName("additionalInformation") val additionalInformation: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
)

@Serializable
data class BloodRequestDto(
    @SerialName("id") val id: String = "",
    @SerialName("requesterId") val requesterId: String = "",
    @SerialName("requesterName") val requesterName: String = "",
    @SerialName("bloodGroup") val bloodGroup: BloodGroup = BloodGroup.OPositive,
    @SerialName("unitsRequired") val unitsRequired: Int = 0,
    @SerialName("unitsFulfilled") val unitsFulfilled: Int = 0,
    @SerialName("hospitalName") val hospitalName: String = "",
    @SerialName("hospitalAddress") val hospitalAddress: String = "",
    @SerialName("districtId") val districtId: String = "",
    @SerialName("districtName") val districtName: String? = null,
    @SerialName("upazilaId") val upazilaId: String = "",
    @SerialName("upazilaName") val upazilaName: String? = null,
    @SerialName("area") val area: String? = null,
    @SerialName("requiredBy") val requiredBy: String = "",
    @SerialName("urgency") val urgency: Urgency = Urgency.Normal,
    @SerialName("patientName") val patientName: String? = null,
    @SerialName("patientRelation") val patientRelation: String? = null,
    @SerialName("contactPhone") val contactPhone: String = "",
    @SerialName("additionalInformation") val additionalInformation: String? = null,
    @SerialName("status") val status: RequestStatus = RequestStatus.Open,
    @SerialName("completedAt") val completedAt: String? = null,
    @SerialName("cancelledAt") val cancelledAt: String? = null,
    @SerialName("createdAt") val createdAt: String = "",
)

@Serializable
data class PublicBloodRequestDto(
    @SerialName("id") val id: String = "",
    @SerialName("bloodGroup") val bloodGroup: BloodGroup = BloodGroup.OPositive,
    @SerialName("unitsRequired") val unitsRequired: Int = 0,
    @SerialName("unitsFulfilled") val unitsFulfilled: Int = 0,
    @SerialName("hospitalName") val hospitalName: String = "",
    @SerialName("hospitalAddress") val hospitalAddress: String = "",
    @SerialName("districtId") val districtId: String = "",
    @SerialName("districtName") val districtName: String? = null,
    @SerialName("upazilaId") val upazilaId: String = "",
    @SerialName("upazilaName") val upazilaName: String? = null,
    @SerialName("area") val area: String? = null,
    @SerialName("requiredBy") val requiredBy: String = "",
    @SerialName("urgency") val urgency: Urgency = Urgency.Normal,
    @SerialName("additionalInformation") val additionalInformation: String? = null,
    @SerialName("status") val status: RequestStatus = RequestStatus.Open,
    @SerialName("createdAt") val createdAt: String = "",
)

@Serializable
data class FulfillBloodRequestRequest(
    @SerialName("unitsFulfilled") val unitsFulfilled: Int,
    @SerialName("notes") val notes: String? = null,
)
