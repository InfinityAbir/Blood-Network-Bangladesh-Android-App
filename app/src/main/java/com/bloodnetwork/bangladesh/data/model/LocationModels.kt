package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DivisionDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("nameBn") val nameBn: String = "",
)

@Serializable
data class DistrictDto(
    @SerialName("id") val id: String = "",
    @SerialName("divisionId") val divisionId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("nameBn") val nameBn: String = "",
)

@Serializable
data class UpazilaDto(
    @SerialName("id") val id: String = "",
    @SerialName("districtId") val districtId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("nameBn") val nameBn: String = "",
)
