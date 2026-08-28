package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("password") val password: String,
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: UserRole = UserRole.Requester,
)

@Serializable
data class LoginRequest(
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("password") val password: String,
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken") val refreshToken: String,
)

@Serializable
data class AuthResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("user") val user: UserDto,
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: UserRole = UserRole.Requester,
    @SerialName("isPhoneVerified") val isPhoneVerified: Boolean = false,
    @SerialName("mustChangePassword") val mustChangePassword: Boolean = false,
    @SerialName("createdAt") val createdAt: String = "",
) {
    val fullName: String get() = "$firstName $lastName"
}

@Serializable
data class FirstLoginChangeRequest(
    @SerialName("newEmail") val newEmail: String,
    @SerialName("newPassword") val newPassword: String,
    @SerialName("currentPassword") val currentPassword: String,
)

@Serializable
data class ApiError(
    @SerialName("message") val message: String? = null,
)
