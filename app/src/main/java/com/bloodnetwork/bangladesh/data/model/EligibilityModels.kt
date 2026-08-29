package com.bloodnetwork.bangladesh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EligibilityQuestionDto(
    @SerialName("id") val id: String = "",
    @SerialName("questionBn") val questionBn: String = "",
    @SerialName("questionEn") val questionEn: String = "",
    @SerialName("questionBanglish") val questionBanglish: String = "",
    @SerialName("questionType") val questionType: String = "",
    @SerialName("unit") val unit: String? = null,
    @SerialName("minValue") val minValue: Int? = null,
    @SerialName("maxValue") val maxValue: Int? = null,
)

@Serializable
data class EligibilityAnswerDto(
    @SerialName("questionId") val questionId: String,
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
    @SerialName("questionId") val questionId: String = "",
    @SerialName("passed") val passed: Boolean = false,
    @SerialName("message") val message: String = "",
    @SerialName("messageBn") val messageBn: String = "",
)

/** Admin CRUD view of a question — includes the pass/fail rule fields and [isActive], which the
 * public [EligibilityQuestionDto] omits. */
@Serializable
data class AdminEligibilityQuestionDto(
    @SerialName("id") val id: String = "",
    @SerialName("questionEn") val questionEn: String = "",
    @SerialName("questionBn") val questionBn: String = "",
    @SerialName("questionBanglish") val questionBanglish: String = "",
    @SerialName("questionType") val questionType: String = "",
    @SerialName("unit") val unit: String? = null,
    @SerialName("minValue") val minValue: Int? = null,
    @SerialName("maxValue") val maxValue: Int? = null,
    @SerialName("passOnYes") val passOnYes: Boolean? = null,
    @SerialName("isCritical") val isCritical: Boolean = false,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("displayOrder") val displayOrder: Int = 0,
    @SerialName("passMessageEn") val passMessageEn: String = "",
    @SerialName("passMessageBn") val passMessageBn: String = "",
    @SerialName("failMessageEn") val failMessageEn: String = "",
    @SerialName("failMessageBn") val failMessageBn: String = "",
)

@Serializable
data class SaveEligibilityQuestionRequest(
    @SerialName("questionEn") val questionEn: String,
    @SerialName("questionBn") val questionBn: String,
    @SerialName("questionBanglish") val questionBanglish: String,
    @SerialName("questionType") val questionType: String,
    @SerialName("unit") val unit: String? = null,
    @SerialName("minValue") val minValue: Int? = null,
    @SerialName("maxValue") val maxValue: Int? = null,
    @SerialName("passOnYes") val passOnYes: Boolean? = null,
    @SerialName("isCritical") val isCritical: Boolean,
    @SerialName("displayOrder") val displayOrder: Int,
    @SerialName("passMessageEn") val passMessageEn: String,
    @SerialName("passMessageBn") val passMessageBn: String,
    @SerialName("failMessageEn") val failMessageEn: String,
    @SerialName("failMessageBn") val failMessageBn: String,
)

@Serializable
data class ToggleEligibilityQuestionActiveRequest(
    @SerialName("isActive") val isActive: Boolean,
)

@Serializable
data class EligibilityStateDto(
    @SerialName("answers") val answers: List<EligibilityAnswerDto> = emptyList(),
    @SerialName("result") val result: EligibilityResultDto = EligibilityResultDto(),
    @SerialName("updatedAt") val updatedAt: String = "",
)
