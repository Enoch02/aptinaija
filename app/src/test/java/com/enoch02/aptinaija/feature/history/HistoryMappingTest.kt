package com.enoch02.aptinaija.feature.history

import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryMappingTest {

    @Test
    fun trends_compareEachAttemptWithTheOlderOne() {
        // Newest first, as returned by observeHistory().
        val attempts = listOf(
            attempt("a4", 78),
            attempt("a3", 54),
            attempt("a2", 71),
            attempt("a1", 66)
        )

        val entries = buildHistoryEntries(attempts, emptyList())

        assertEquals(
            listOf(
                HistoryTrend.UP,
                HistoryTrend.DOWN,
                HistoryTrend.UP,
                HistoryTrend.NONE
            ),
            entries.map { it.trend }
        )
        assertEquals(listOf(78, 54, 71, 66), entries.map { it.percent })
    }

    @Test
    fun equalScores_areFlatAndEmptyHistory_isEmpty() {
        val attempts = listOf(attempt("a2", 70), attempt("a1", 70))

        val entries = buildHistoryEntries(attempts, emptyList())

        assertEquals(listOf(HistoryTrend.FLAT, HistoryTrend.NONE), entries.map { it.trend })
        assertTrue(buildHistoryEntries(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun mockTitle_usesTemplateTitleWithoutTheRedundantMockSuffix() {
        val attempts = listOf(
            attempt("a1", 70, mode = AttemptMode.MOCK).copy(templateId = "SEPLAT_TECH_TRAINEE")
        )
        val templates = listOf(
            TestTemplate(
                id = "SEPLAT_TECH_TRAINEE",
                title = "Seplat Technical Trainee Mock",
                companyId = "SEP",
                company = "Seplat Energy",
                provider = "HCP",
                sourceUrl = null,
                defaults = TestDefaults(),
                sections = emptyList()
            )
        )

        val subject = buildHistoryEntries(attempts, templates).single().subject

        assertEquals("Seplat Technical Trainee", subject)
    }

    @Test
    fun mockSuffix_isStrippedCaseInsensitivelyAndOnlyAtTheEnd() {
        val titles = listOf("UBA GMAP MOCK", "UBA GMAP Mock", "Mock", "Mock Aptitude Test")
        val attempts = titles.mapIndexed { index, _ ->
            attempt("a$index", 70, mode = AttemptMode.MOCK).copy(templateId = "t$index")
        }
        val templates = titles.mapIndexed { index, title -> template("t$index", title) }

        val subjects = buildHistoryEntries(attempts, templates).map { it.subject }

        assertEquals(listOf("UBA GMAP", "UBA GMAP", "", "Mock Aptitude Test"), subjects)
    }

    @Test
    fun drillAndPractice_useSectionAndCompany() {
        val entries = buildHistoryEntries(
            listOf(
                attempt("a2", 60, mode = AttemptMode.DRILL).copy(section = "Numerical Reasoning"),
                attempt("a1", 50, mode = AttemptMode.PRACTICE).copy(company = "UBA")
            ),
            emptyList()
        )

        assertEquals("Numerical Reasoning", entries[0].subject)
        assertEquals("UBA", entries[1].subject)
    }

    @Test
    fun dateLabel_isDayAndShortMonth() {
        val entries = buildHistoryEntries(listOf(attempt("a1", 70)), emptyList())

        assertTrue(Regex("\\d{1,2} [A-Z][a-z]{2}").matches(entries.single().dateLabel))
    }

    private fun template(id: String, title: String) = TestTemplate(
        id = id,
        title = title,
        companyId = "CO",
        company = "Company",
        provider = "Provider",
        sourceUrl = null,
        defaults = TestDefaults(),
        sections = emptyList()
    )

    private fun attempt(
        id: String,
        correct: Int,
        mode: AttemptMode = AttemptMode.MOCK
    ) = Attempt(
        id = id,
        templateId = null,
        mode = mode,
        company = null,
        section = null,
        startedAt = 1_764_000_000_000L,
        completedAt = 1_764_000_000_000L,
        deadlineAt = null,
        totalQuestions = 100,
        correctCount = correct,
        status = AttemptStatus.COMPLETED
    )
}
