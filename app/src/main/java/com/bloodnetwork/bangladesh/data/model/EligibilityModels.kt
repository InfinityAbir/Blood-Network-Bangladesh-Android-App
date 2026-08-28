package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EligibilityQuestionDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("questionBn") val questionBn: String = "",
    @SerialName("questionEn") val questionEn: String = "",
    @SerialName("questionBanglish") val questionBanglish: String = "",
    @SerialName("questionType") val questionType: String = "",
    @SerialName("unit") val unit: String? = null,
    @SerialName("minValue") val minValue: Int = 0,
    @SerialName("maxValue") val maxValue: Int = 0,
)

@Serializable
data class EligibilityAnswerDto(
    @SerialName("questionId") val questionId: Int,
    @SerialName("answer") val answer: String,
)

@Serializable
data class EligibilityResultDto(
    @SerialName("isEligible") val isEligible: Boolean = false,
    @SerialName("score") val score: Int = 0,
    @SerialName("checks") val checks: List<EligibilityCheckDto> = emptyList(),
    @SerialName("recommendationBn") val recommendationBn: String = "",
    @SerialName("recommendationEn") val recommendationEn: String = "",
)

@Serializable
data class EligibilityCheckDto(
    @SerialName("questionId") val questionId: Int = 0,
    @SerialName("passed") val passed: Boolean = false,
    @SerialName("message") val message: String = "",
    @SerialName("messageBn") val messageBn: String = "",
)
