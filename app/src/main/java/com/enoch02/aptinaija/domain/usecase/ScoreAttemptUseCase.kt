package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.model.ScoreSummary
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class ScoreAttemptUseCase @Inject constructor(
    private val attempts: AttemptRepository,
    private val settings: SettingsRepository
) {
    suspend operator fun invoke(attemptId: String): ScoreSummary? {
        val session = attempts.get(attemptId) ?: return null
        val total = session.items.size
        val correct = session.items.count { it.isCorrect == true }
        val passMark = settings.settings.first().passMarkPercent
        val finishedAt = session.attempt.completedAt ?: System.currentTimeMillis()
        return ScoreSummary(
            attemptId = attemptId,
            total = total,
            correct = correct,
            percent = if (total == 0) 0 else (correct * 100) / total,
            sections = attempts.sectionScores(attemptId),
            timeUsedSeconds = ((finishedAt - session.attempt.startedAt) / 1000L).coerceAtLeast(0L),
            passMarkPercent = passMark
        )
    }
}
