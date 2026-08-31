package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateDonorProfileRequest(
    @SerialName("bloodGroup") val bloodGroup: BloodGroup,
    @SerialName("gender") val gender: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    @SerialName("districtId") val districtId: String,
    @SerialName("upazilaId") val upazilaId: String,
    @SerialName("area") val area: String? = null,
    @SerialName("customAddress") val customAddress: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("lastDonationDate") val lastDonationDate: String? = null,
)

@Serializable
data class UpdateDonorProfileRequest(
    @SerialName("bloodGroup") val bloodGroup: BloodGroup,
    @SerialName("gender") val gender: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    @SerialName("districtId") val districtId: String,
    @SerialName("upazilaId") val upazilaId: String,
    @SerialName("area") val area: String? = null,
    @SerialName("customAddress") val customAddress: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("lastDonationDate") val lastDonationDate: String? = null,
)

@Serializable
data class DonorProfileDto(
    @SerialName("id") val id: String = "",
    @SerialName("userId") val userId: String = "",
    @SerialName("bloodGroup") val bloodGroup: BloodGroup = BloodGroup.OPositive,
    @SerialName("gender") val gender: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    @SerialName("districtId") val districtId: String = "",
    @SerialName("districtName") val districtName: String? = null,
    @SerialName("upazilaId") val upazilaId: String = "",
    @SerialName("upazilaName") val upazilaName: String? = null,
    @SerialName("area") val area: String? = null,
    @SerialName("customAddress") val customAddress: String? = null,
    @SerialName("lastDonationDate") val lastDonationDate: String? = null,
    @SerialName("availabilityStatus") val availabilityStatus: AvailabilityStatus = AvailabilityStatus.Unknown,
    @SerialName("verificationStatus") val verificationStatus: VerificationStatus = VerificationStatus.Unverified,
    @SerialName("totalDonationCount") val totalDonationCount: Int = 0,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("createdAt") val createdAt: String = "",
)

@Serializable
data class PublicDonorDto(
    @SerialName("id") val id: String = "",
    @SerialName("firstName") val firstName: String = "",
    @SerialName("bloodGroup") val bloodGroup: BloodGroup = BloodGroup.OPositive,
    @SerialName("districtName") val districtName: String = "",
    @SerialName("upazilaName") val upazilaName: String = "",
    @SerialName("area") val area: String? = null,
    @SerialName("availabilityStatus") val availabilityStatus: AvailabilityStatus = AvailabilityStatus.Unknown,
    @SerialName("verificationStatus") val verificationStatus: VerificationStatus = VerificationStatus.Unverified,
    @SerialName("distanceKm") val distanceKm: Double? = null,
    @SerialName("photoUrl") val photoUrl: String? = null,
    @SerialName("districtId") val districtId: String? = null,
    @SerialName("upazilaId") val upazilaId: String? = null,
)

@Serializable
data class ToggleAvailabilityRequest(
    @SerialName("availabilityStatus") val availabilityStatus: AvailabilityStatus,
)
