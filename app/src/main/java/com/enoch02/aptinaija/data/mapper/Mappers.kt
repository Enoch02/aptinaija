package com.enoch02.aptinaija.data.mapper

import com.enoch02.aptinaija.data.local.dao.AttemptItemRow
import com.enoch02.aptinaija.data.local.entity.AttemptEntity
import com.enoch02.aptinaija.data.local.entity.QuestionEntity
import com.enoch02.aptinaija.data.local.entity.TemplateSectionEntity
import com.enoch02.aptinaija.data.local.entity.TestTemplateEntity
import com.enoch02.aptinaija.data.pack.AppJson
import com.enoch02.aptinaija.data.pack.QuestionDto
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.Difficulty
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.model.TemplateSection
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

private val stringListSerializer = ListSerializer(String.serializer())

fun encodeStringList(values: List<String>): String =
    AppJson.encodeToString(stringListSerializer, values)

fun decodeStringList(value: String): List<String> =
    runCatching { AppJson.decodeFromString(stringListSerializer, value) }.getOrDefault(emptyList())

fun QuestionEntity.toDomain(): Question = Question(
    id = id,
    company = company,
    section = section,
    difficulty = Difficulty.fromRaw(difficulty),
    stem = stem,
    options = decodeStringList(optionsJson),
    answerIndex = answerIndex,
    explanation = explanation,
    passage = passage,
    sourceUrl = sourceUrl,
    tags = decodeStringList(tagsJson),
    status = status
)

fun QuestionDto.toEntity(): QuestionEntity = QuestionEntity(
    id = id,
    company = companyId.ifBlank { id.substringBefore('-').ifBlank { "General" } },
    section = section,
    difficulty = difficulty,
    stem = stem,
    optionsJson = encodeStringList(options),
    answerIndex = answer,
    explanation = explanation,
    passage = passage,
    sourceUrl = source,
    tagsJson = encodeStringList(tags),
    status = status
)

fun AttemptItemRow.toDomain(): AttemptItem = AttemptItem(
    position = position,
    question = Question(
        id = id,
        company = company,
        section = section,
        difficulty = Difficulty.fromRaw(difficulty),
        stem = stem,
        options = decodeStringList(optionsJson),
        answerIndex = answerIndex,
        explanation = explanation,
        passage = passage,
        sourceUrl = sourceUrl,
        tags = decodeStringList(tagsJson),
        status = status
    ),
    optionOrder = decodeStringList(optionOrderJson).mapNotNull { it.toIntOrNull() },
    selectedOption = selectedOption,
    isCorrect = isCorrect
)

fun AttemptEntity.toDomain(): Attempt = Attempt(
    id = id,
    templateId = templateId,
    mode = AttemptMode.entries.firstOrNull { it.name == mode } ?: AttemptMode.PRACTICE,
    company = company,
    section = section,
    startedAt = startedAt,
    completedAt = completedAt,
    deadlineAt = deadlineAt,
    totalQuestions = totalQuestions,
    correctCount = correctCount,
    status = AttemptStatus.entries.firstOrNull { it.name == status } ?: AttemptStatus.IN_PROGRESS
)

fun TestTemplateEntity.toDomain(sections: List<TemplateSectionEntity>): TestTemplate = TestTemplate(
    id = id,
    title = title,
    companyId = companyId,
    company = company,
    provider = provider,
    sourceUrl = sourceUrl,
    defaults = TestDefaults(
        shuffle = shuffle,
        feedbackDuring = feedbackDuring,
        autoSubmit = autoSubmit,
        pause = pause
    ),
    sections = sections
        .sortedBy { it.position }
        .map { TemplateSection(section = it.section, questionCount = it.questionCount, minutes = it.minutes) }
)
