package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptSession
import com.enoch02.aptinaija.domain.model.CompanySummary
import com.enoch02.aptinaija.domain.model.Difficulty
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.model.SectionScore
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TemplateSection
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildAttemptUseCaseTest {

    @Test
    fun mockTest_drawsQuestionsFromTheTemplateCompany() = runBlocking {
        val questions = RecordingQuestionRepository()
        val templates = FakeTestRepository(
            TestTemplate(
                id = "SEP_MOCK",
                title = "Seplat Mock",
                companyId = "SEP",
                company = "Seplat Energy",
                provider = "HCP",
                sourceUrl = null,
                defaults = TestDefaults(),
                sections = listOf(
                    TemplateSection(section = "Numerical Reasoning", questionCount = 2, minutes = 10)
                )
            )
        )
        val attempts = FakeAttemptRepository()
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = templates,
            attempts = attempts,
            optionShuffler = OptionShuffler(),
            settings = FakeSettingsRepository()
        )

        useCase(BuildAttemptRequest(mode = AttemptMode.MOCK, templateId = "SEP_MOCK"))

        assertEquals(
            listOf(QuestionQuery("SEP", "Numerical Reasoning", 2)),
            questions.queries
        )
    }

    @Test
    fun practiceTest_usesTheRequestedCompanyId() = runBlocking {
        val questions = RecordingQuestionRepository()
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = FakeTestRepository(),
            attempts = FakeAttemptRepository(),
            optionShuffler = OptionShuffler(),
            settings = FakeSettingsRepository()
        )

        useCase(
            BuildAttemptRequest(
                mode = AttemptMode.PRACTICE,
                company = "FCSC",
                questionCount = 5
            )
        )

        assertEquals(listOf(QuestionQuery("FCSC", null, 5)), questions.queries)
    }

    private data class QuestionQuery(
        val company: String?,
        val section: String?,
        val limit: Int
    )

    private class RecordingQuestionRepository : QuestionRepository {
        val queries = mutableListOf<QuestionQuery>()

        override suspend fun ensurePackImported() = Unit

        override fun observeCompanies(): Flow<List<CompanySummary>> = flowOf(emptyList())

        override fun observeSections(company: String): Flow<List<SectionSummary>> =
            flowOf(emptyList())

        override fun observeQuestions(company: String, section: String?): Flow<List<Question>> =
            flowOf(emptyList())

        override suspend fun questionById(id: String): Question? = null

        override suspend fun randomQuestions(
            company: String?,
            section: String?,
            limit: Int
        ): List<Question> {
            queries += QuestionQuery(company, section, limit)
            return listOf(question(id = "Q-${queries.size}"))
        }

        private fun question(id: String) = Question(
            id = id,
            company = "SEP",
            section = "Numerical Reasoning",
            difficulty = Difficulty.EASY,
            stem = "Question $id",
            options = listOf("A", "B"),
            answerIndex = 0,
            explanation = "Because"
        )
    }

    private class FakeTestRepository(
        private val template: TestTemplate? = null
    ) : TestRepository {
        override fun observeTemplates(): Flow<List<TestTemplate>> =
            flowOf(listOfNotNull(template))

        override suspend fun template(id: String): TestTemplate? = template
    }

    private class FakeAttemptRepository : AttemptRepository {
        override suspend fun create(attempt: Attempt, items: List<AttemptItem>): String = attempt.id

        override suspend fun get(id: String): AttemptSession? = null

        override suspend fun recordAnswer(
            attemptId: String,
            position: Int,
            selectedOption: Int,
            isCorrect: Boolean
        ) = Unit

        override suspend fun complete(attemptId: String, correctCount: Int) = Unit

        override fun observeHistory(): Flow<List<Attempt>> = flowOf(emptyList())

        override fun observeLastCompleted(): Flow<Attempt?> = flowOf(null)

        override fun observeWeakSections(limit: Int): Flow<List<SectionWeakness>> =
            flowOf(emptyList())

        override suspend fun sectionScores(attemptId: String): List<SectionScore> =
            emptyList()

        override fun observeWrongQuestions(): Flow<List<Question>> = flowOf(emptyList())

        override suspend fun resolveWrong(questionId: String) = Unit
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val settings: Flow<AppSettings> = flowOf(
            AppSettings(shuffleQuestions = false, shuffleOptions = false)
        )

        override suspend fun setFontScale(value: Float) = Unit

        override suspend fun setShuffleQuestions(value: Boolean) = Unit

        override suspend fun setShuffleOptions(value: Boolean) = Unit

        override suspend fun setDefaultQuestionCount(value: Int) = Unit

        override suspend fun setPassMarkPercent(value: Int) = Unit
    }
}
