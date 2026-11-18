package com.enoch02.aptinaija.domain.model

enum class Difficulty {
    EASY, MEDIUM, HARD;

    companion object {
        fun fromRaw(value: String): Difficulty =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

enum class AttemptMode { PRACTICE, DRILL, MOCK }

enum class AttemptStatus { IN_PROGRESS, COMPLETED, ABANDONED }

data class Question(
    val id: String,
    val company: String,
    val section: String,
    val difficulty: Difficulty,
    val stem: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String,
    val passage: String? = null,
    val sourceUrl: String? = null,
    val tags: List<String> = emptyList(),
    val status: String = "published"
)

data class TestDefaults(
    val shuffle: Boolean = true,
    val feedbackDuring: Boolean = false,
    val autoSubmit: Boolean = true,
    val pause: Boolean = false
)

data class TemplateSection(
    val section: String,
    val questionCount: Int,
    val minutes: Int
)

data class TestTemplate(
    val id: String,
    val title: String,
    val company: String,
    val provider: String,
    val sourceUrl: String?,
    val defaults: TestDefaults,
    val sections: List<TemplateSection>
)

data class Attempt(
    val id: String,
    val templateId: String?,
    val mode: AttemptMode,
    val company: String?,
    val section: String?,
    val startedAt: Long,
    val completedAt: Long?,
    val deadlineAt: Long?,
    val totalQuestions: Int,
    val correctCount: Int,
    val status: AttemptStatus
) {
    val percent: Int
        get() = if (totalQuestions == 0) 0 else (correctCount * 100) / totalQuestions
}

data class AttemptItem(
    val position: Int,
    val question: Question,
    val optionOrder: List<Int>,
    val selectedOption: Int? = null,
    val isCorrect: Boolean? = null
)

data class AttemptSession(
    val attempt: Attempt,
    val items: List<AttemptItem>
)

data class SectionScore(
    val section: String,
    val total: Int,
    val correct: Int
) {
    val percent: Int
        get() = if (total == 0) 0 else (correct * 100) / total
}

data class ScoreSummary(
    val attemptId: String,
    val total: Int,
    val correct: Int,
    val percent: Int,
    val sections: List<SectionScore>,
    val timeUsedSeconds: Long,
    val passMarkPercent: Int
) {
    val passed: Boolean
        get() = percent >= passMarkPercent
}

data class CompanySummary(
    val company: String,
    val questionCount: Int
)

data class SectionSummary(
    val section: String,
    val questionCount: Int
)

data class SectionWeakness(
    val section: String,
    val wrong: Int,
    val total: Int
)

data class AppSettings(
    val fontScale: Float = 1f,
    val shuffleQuestions: Boolean = true,
    val shuffleOptions: Boolean = true,
    val defaultQuestionCount: Int = 20,
    val passMarkPercent: Int = 50
)
