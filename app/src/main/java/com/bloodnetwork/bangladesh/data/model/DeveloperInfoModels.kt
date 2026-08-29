package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeveloperInfoDto(
    @SerialName("name") val name: String = "",
    @SerialName("role") val role: String = "",
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("linkedInUrl") val linkedInUrl: String? = null,
    @SerialName("githubUrl") val githubUrl: String? = null,
)

@Serializable
data class UpdateDeveloperInfoRequest(
    @SerialName("name") val name: String,
    @SerialName("role") val role: String,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("linkedInUrl") val linkedInUrl: String? = null,
    @SerialName("githubUrl") val githubUrl: String? = null,
)
