package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.AttemptStatus
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.first

data class BuildAttemptRequest(
    val mode: AttemptMode,
    val company: String? = null,
    val section: String? = null,
    val templateId: String? = null,
    val questionCount: Int = 20
)

class BuildAttemptUseCase @Inject constructor(
    private val questions: QuestionRepository,
    private val templates: TestRepository,
    private val attempts: AttemptRepository,
    private val optionShuffler: OptionShuffler,
    private val settings: SettingsRepository
) {
    suspend operator fun invoke(request: BuildAttemptRequest): String {
        questions.ensurePackImported()
        val preferences = settings.settings.first()
        val now = System.currentTimeMillis()
        val seed = Random.nextLong()

        val selected = when (request.mode) {
            AttemptMode.MOCK -> mockQuestions(request, preferences.shuffleQuestions)
            AttemptMode.DRILL -> questions.randomQuestions(
                company = request.company,
                section = request.section,
                limit = request.questionCount
            ).ordered(preferences.shuffleQuestions).map { it.section to it }

            AttemptMode.PRACTICE -> questions.randomQuestions(
                company = request.company,
                section = null,
                limit = request.questionCount
            ).ordered(preferences.shuffleQuestions).map { it.section to it }
        }

        check(selected.isNotEmpty()) {
            "No questions are available for this selection yet. Import a question pack first."
        }

        val unique = selected.distinctBy { it.second.id }
        val items = unique.mapIndexed { index, (_, question) ->
            AttemptItem(
                position = index,
                question = question,
                optionOrder = if (preferences.shuffleOptions) {
                    optionShuffler.orderFor(seed, question)
                } else {
                    question.options.indices.toList()
                }
            )
        }

        val durationMinutes = if (request.mode == AttemptMode.MOCK) {
            templates.template(request.templateId.orEmpty())?.sections?.sumOf { it.minutes } ?: 0
        } else {
            0
        }

        val attempt = Attempt(
            id = UUID.randomUUID().toString(),
            templateId = request.templateId,
            mode = request.mode,
            company = request.company,
            section = request.section,
            startedAt = now,
            completedAt = null,
            deadlineAt = if (durationMinutes > 0) now + durationMinutes * 60_000L else null,
            totalQuestions = items.size,
            correctCount = 0,
            status = AttemptStatus.IN_PROGRESS
        )

        return attempts.create(attempt, items)
    }

    private suspend fun mockQuestions(
        request: BuildAttemptRequest,
        shuffleQuestions: Boolean
    ): List<Pair<String, Question>> {
        val templateId = requireNotNull(request.templateId) { "Mock attempts need a test template" }
        val template = requireNotNull(templates.template(templateId)) { "Unknown test template: $templateId" }
        return template.sections.flatMap { section ->
            questions.randomQuestions(
                company = null,
                section = section.section,
                limit = section.questionCount
            ).ordered(shuffleQuestions).map { section.section to it }
        }
    }

    private fun List<Question>.ordered(shuffle: Boolean): List<Question> =
        if (shuffle) this else sortedBy { it.id }
}
