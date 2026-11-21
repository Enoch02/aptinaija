package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.repository.AttemptRepository
import javax.inject.Inject

class SubmitAnswerUseCase @Inject constructor(
    private val attempts: AttemptRepository
) {
    suspend operator fun invoke(
        attemptId: String,
        position: Int,
        selectedOption: Int,
        correctOption: Int
    ) {
        attempts.recordAnswer(
            attemptId = attemptId,
            position = position,
            selectedOption = selectedOption,
            isCorrect = selectedOption == correctOption
        )
    }
}
