package com.enoch02.aptinaija.feature.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.usecase.ScoreAttemptUseCase
import com.enoch02.aptinaija.domain.usecase.SubmitAnswerUseCase
import com.enoch02.aptinaija.navigation.TestRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TestOptionUi(
    val originalIndex: Int,
    val text: String
)

data class TestQuestionUi(
    val position: Int,
    val section: String,
    val passage: String?,
    val stem: String,
    val options: List<TestOptionUi>,
    val correctOriginal: Int,
    val explanation: String,
    val selectedOriginal: Int?,
    val isCorrect: Boolean?
)

data class TestUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val title: String = "Test",
    val mode: AttemptMode = AttemptMode.PRACTICE,
    val questions: List<TestQuestionUi> = emptyList(),
    val currentIndex: Int = 0,
    val remainingSeconds: Long? = null,
    val finishedAttemptId: String? = null
) {
    val current: TestQuestionUi? get() = questions.getOrNull(currentIndex)
    val total: Int get() = questions.size
    val isLast: Boolean get() = currentIndex >= questions.lastIndex
    val feedbackEnabled: Boolean get() = mode != AttemptMode.MOCK
}

@HiltViewModel
class TestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attempts: AttemptRepository,
    private val submitAnswer: SubmitAnswerUseCase,
    private val scoreAttempt: ScoreAttemptUseCase
) : ViewModel() {

    private val attemptId: String = savedStateHandle.toRoute<TestRoute>().attemptId
    private val _state = MutableStateFlow(TestUiState())
    val state: StateFlow<TestUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var finishing = false

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val session = attempts.get(attemptId)
            if (session == null) {
                _state.update { it.copy(loading = false, error = "This attempt no longer exists.") }
                return@launch
            }
            val questions = session.items.map { it.toUi() }
            val firstUnanswered = questions.indexOfFirst { it.selectedOriginal == null }
            _state.update {
                it.copy(
                    loading = false,
                    title = session.attempt.title(),
                    mode = session.attempt.mode,
                    questions = questions,
                    currentIndex = if (firstUnanswered >= 0) firstUnanswered else questions.lastIndex.coerceAtLeast(0),
                    remainingSeconds = session.attempt.deadlineAt?.let { deadline ->
                        ((deadline - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                    }
                )
            }
            startTimer(session.attempt.deadlineAt)
        }
    }

    private fun startTimer(deadlineAt: Long?) {
        if (deadlineAt == null) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && !finishing) {
                val remaining = ((deadlineAt - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                _state.update { it.copy(remainingSeconds = remaining) }
                if (remaining <= 0L) {
                    finish()
                    break
                }
                delay(500)
            }
        }
    }

    fun selectOption(originalIndex: Int) {
        val current = _state.value.current ?: return
        if (_state.value.finishedAttemptId != null) return
        viewModelScope.launch {
            submitAnswer(
                attemptId = attemptId,
                position = current.position,
                selectedOption = originalIndex,
                correctOption = current.correctOriginal
            )
            _state.update { state ->
                state.copy(
                    questions = state.questions.map { question ->
                        if (question.position == current.position) {
                            question.copy(
                                selectedOriginal = originalIndex,
                                isCorrect = originalIndex == question.correctOriginal
                            )
                        } else {
                            question
                        }
                    }
                )
            }
        }
    }

    fun next() {
        val current = _state.value
        if (current.isLast) {
            finish()
        } else {
            _state.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

    fun finish() {
        if (finishing) return
        finishing = true
        timerJob?.cancel()
        viewModelScope.launch {
            val summary = scoreAttempt(attemptId)
            if (summary == null) {
                finishing = false
                _state.update { it.copy(error = "Could not score this attempt.") }
                return@launch
            }
            attempts.complete(attemptId, summary.correct)
            _state.update { it.copy(finishedAttemptId = attemptId) }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }

    private fun AttemptItem.toUi(): TestQuestionUi {
        val order = optionOrder.ifEmpty { question.options.indices.toList() }
        return TestQuestionUi(
            position = position,
            section = question.section,
            passage = question.passage,
            stem = question.stem,
            options = order.mapNotNull { original ->
                question.options.getOrNull(original)?.let { TestOptionUi(originalIndex = original, text = it) }
            },
            correctOriginal = question.answerIndex,
            explanation = question.explanation,
            selectedOriginal = selectedOption,
            isCorrect = isCorrect
        )
    }

    private fun Attempt.title(): String = when (mode) {
        AttemptMode.MOCK -> company?.let { "$it mock test" } ?: "Mock test"
        AttemptMode.DRILL -> section?.let { "$it drill" } ?: "Section drill"
        AttemptMode.PRACTICE -> "Practice"
    }
}
