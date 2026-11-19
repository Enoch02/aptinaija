package com.enoch02.aptinaija.feature.history

import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.TestTemplate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HistoryTrend { UP, DOWN, FLAT, NONE }

data class HistoryEntry(
    val id: String,
    val subject: String,
    val mode: AttemptMode,
    val dateLabel: String,
    val percent: Int,
    val trend: HistoryTrend
)

data class HistoryUiState(
    val loading: Boolean = true,
    val entries: List<HistoryEntry> = emptyList()
)

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

/** Strips a trailing "Mock" from a template title, case-insensitively; preserves mid-title uses. */
private val mockSuffix = Regex("(?i)(^|\\s+)mock$")

/**
 * `attempts` arrives newest-first from AttemptRepository.observeHistory().
 * A trend compares an attempt with the immediately older one; the oldest has none.
 */
fun buildHistoryEntries(
    attempts: List<Attempt>,
    templates: List<TestTemplate>
): List<HistoryEntry> {
    val templateById = templates.associateBy { it.id }
    return attempts.mapIndexed { index, attempt ->
        val older = attempts.getOrNull(index + 1)
        HistoryEntry(
            id = attempt.id,
            subject = attempt.subject(templateById[attempt.templateId]),
            mode = attempt.mode,
            dateLabel = Instant.ofEpochMilli(attempt.completedAt ?: attempt.startedAt)
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter),
            percent = attempt.percent,
            trend = when {
                older == null -> HistoryTrend.NONE
                attempt.percent > older.percent -> HistoryTrend.UP
                attempt.percent < older.percent -> HistoryTrend.DOWN
                else -> HistoryTrend.FLAT
            }
        )
    }
}

private fun Attempt.subject(template: TestTemplate?): String = when (mode) {
    AttemptMode.MOCK -> template?.title
        ?.replace(mockSuffix, "")
        ?.trim()
        ?: company
        ?: templateId?.replace('_', ' ')
        ?: ""
    AttemptMode.DRILL -> section ?: company.orEmpty()
    AttemptMode.PRACTICE -> company ?: section.orEmpty()
}
