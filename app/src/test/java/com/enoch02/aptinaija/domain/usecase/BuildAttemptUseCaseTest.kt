package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.AppThemeMode
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
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
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
            bookmarks = FakeBookmarkRepository(),
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
    fun wrongBankRetest_buildsPracticeAttemptFromUnresolvedQuestions() = runBlocking {
        val questions = RecordingQuestionRepository()
        val attempts = FakeAttemptRepository(
            wrongQuestions = listOf(
                testQuestion("WRONG-1"),
                testQuestion("WRONG-2")
            )
        )
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = FakeTestRepository(),
            attempts = attempts,
            bookmarks = FakeBookmarkRepository(),
            optionShuffler = OptionShuffler(),
            settings = FakeSettingsRepository()
        )

        val attemptId = useCase(
            BuildAttemptRequest(mode = AttemptMode.PRACTICE, wrongBankOnly = true)
        )

        assertEquals(emptyList<QuestionQuery>(), questions.queries)
        assertEquals(attemptId, attempts.created?.first?.id)
        assertEquals(AttemptMode.PRACTICE, attempts.created?.first?.mode)
        assertEquals(
            listOf("WRONG-1", "WRONG-2"),
            attempts.created?.second?.map { it.question.id }
        )
        assertEquals(
            listOf(0, 1),
            attempts.created?.second?.map { it.position }
        )
    }

    @Test
    fun bookmarkRetest_buildsPracticeAttemptFromBookmarkedQuestions() = runBlocking {
        val questions = RecordingQuestionRepository()
        val attempts = FakeAttemptRepository()
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = FakeTestRepository(),
            attempts = attempts,
            bookmarks = FakeBookmarkRepository(
                listOf(testQuestion("BM-1"), testQuestion("BM-2"))
            ),
            optionShuffler = OptionShuffler(),
            settings = FakeSettingsRepository()
        )

        useCase(BuildAttemptRequest(mode = AttemptMode.PRACTICE, bookmarksOnly = true))

        assertEquals(emptyList<QuestionQuery>(), questions.queries)
        assertEquals(AttemptMode.PRACTICE, attempts.created?.first?.mode)
        assertEquals(
            listOf("BM-1", "BM-2"),
            attempts.created?.second?.map { it.question.id }
        )
    }

    @Test
    fun practiceTest_usesTheRequestedCompanyId() = runBlocking {
        val questions = RecordingQuestionRepository()
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = FakeTestRepository(),
            attempts = FakeAttemptRepository(),
            bookmarks = FakeBookmarkRepository(),
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

    @Test
    fun randomQuiz_drawsFromWholeBankAndAllotsOneMinutePerQuestion() = runBlocking {
        val questions = RecordingQuestionRepository()
        val attempts = FakeAttemptRepository()
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = FakeTestRepository(),
            attempts = attempts,
            bookmarks = FakeBookmarkRepository(),
            optionShuffler = OptionShuffler(),
            settings = FakeSettingsRepository()
        )

        useCase(
            BuildAttemptRequest(
                mode = AttemptMode.RANDOM_QUIZ,
                company = "FCSC",
                questionCount = 10
            )
        )

        assertEquals(listOf(QuestionQuery(null, null, 10)), questions.queries)
        val attempt = checkNotNull(attempts.created?.first)
        assertEquals(AttemptMode.RANDOM_QUIZ, attempt.mode)
        assertEquals(10 * 60_000L, checkNotNull(attempt.deadlineAt) - attempt.startedAt)
    }

    @Test
    fun randomQuiz_withShuffleDisabled_ordersQuestionsDeterministically() = runBlocking {
        val recording = RecordingQuestionRepository()
        val questions = object : QuestionRepository by recording {
            override suspend fun randomQuestions(
                company: String?,
                section: String?,
                limit: Int
            ): List<Question> = listOf(
                testQuestion("Q-3"),
                testQuestion("Q-1"),
                testQuestion("Q-2")
            )
        }
        val attempts = FakeAttemptRepository()
        val useCase = BuildAttemptUseCase(
            questions = questions,
            templates = FakeTestRepository(),
            attempts = attempts,
            bookmarks = FakeBookmarkRepository(),
            optionShuffler = OptionShuffler(),
            settings = FakeSettingsRepository()
        )

        useCase(BuildAttemptRequest(mode = AttemptMode.RANDOM_QUIZ, questionCount = 3))

        assertEquals(
            listOf("Q-1", "Q-2", "Q-3"),
            attempts.created?.second?.map { it.question.id }
        )
    }

    private data class QuestionQuery(
        val company: String?,
        val section: String?,
        val limit: Int
    )

    private fun testQuestion(id: String) = Question(
        id = id,
        company = "SEP",
        section = "Numerical Reasoning",
        difficulty = Difficulty.EASY,
        stem = "Question $id",
        options = listOf("A", "B"),
        answerIndex = 0,
        explanation = "Because"
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

    private class FakeAttemptRepository(
        private val wrongQuestions: List<Question> = emptyList()
    ) : AttemptRepository {
        var created: Pair<Attempt, List<AttemptItem>>? = null
            private set

        override suspend fun create(attempt: Attempt, items: List<AttemptItem>): String {
            created = attempt to items
            return attempt.id
        }

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

        override fun observeWrongQuestions(): Flow<List<Question>> = flowOf(wrongQuestions)

        override suspend fun resolveWrong(questionId: String) = Unit
    }

    private class FakeBookmarkRepository(
        private val bookmarked: List<Question> = emptyList()
    ) : BookmarkRepository {
        override fun observeBookmarks(): Flow<List<Question>> = flowOf(bookmarked)
        override fun observeCount(): Flow<Int> = flowOf(bookmarked.size)
        override fun observeIsBookmarked(questionId: String): Flow<Boolean> =
            flowOf(bookmarked.any { it.id == questionId })
        override suspend fun toggle(questionId: String) = Unit
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val settings: Flow<AppSettings> = flowOf(
            AppSettings(shuffleQuestions = false, shuffleOptions = false)
        )

        override suspend fun setThemeMode(value: AppThemeMode) = Unit

        override suspend fun setShuffleQuestions(value: Boolean) = Unit

        override suspend fun setShuffleOptions(value: Boolean) = Unit

        override suspend fun setDefaultQuestionCount(value: Int) = Unit

        override suspend fun setPassMarkPercent(value: Int) = Unit
    }
}
