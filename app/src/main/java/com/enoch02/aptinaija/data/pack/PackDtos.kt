package com.enoch02.aptinaija.data.pack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
data class QuestionPackDto(
    @SerialName("schema_version") val schemaVersion: Int,
    val questions: List<QuestionDto>
)

@Serializable
data class QuestionDto(
    val id: String,
    @SerialName("company_id") val companyId: String,
    val section: String,
    val difficulty: String = "medium",
    val stem: String,
    val options: List<String>,
    val answer: Int,
    val explanation: String,
    val passage: String? = null,
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val status: String = "draft"
)

@Serializable
data class TemplatesPackDto(
    @SerialName("schema_version") val schemaVersion: Int,
    val defaults: TemplateFlagsDto = TemplateFlagsDto(),
    val templates: Map<String, TemplateDto> = emptyMap()
)

@Serializable
data class TemplateFlagsDto(
    val shuffle: Boolean = true,
    @SerialName("feedback_during") val feedbackDuring: Boolean = false,
    @SerialName("auto_submit") val autoSubmit: Boolean = true,
    val pause: Boolean = false
)

@Serializable
data class TemplateDto(
    val title: String,
    @SerialName("company_id") val companyId: String,
    val company: String,
    val provider: String,
    val sections: List<TemplateSectionDto>,
    val source: String? = null
)

@Serializable
data class TemplateSectionDto(
    val section: String,
    val count: Int,
    val minutes: Int
)
