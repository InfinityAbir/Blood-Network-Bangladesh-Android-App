package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String,
)

@Serializable
data class ChatRequest(
    @SerialName("message") val message: String,
    @SerialName("history") val history: List<ChatMessage>? = null,
)

@Serializable
data class ChatResponse(
    @SerialName("reply") val reply: String = "",
)
