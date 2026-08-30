package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterPushTokenRequest(
    val token: String,
    val platform: String = "Android",
)