package com.enoch02.aptinaija.feature.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
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

/** FR-06 timer states: normal above 20%, warning at 20%, critical at 10% remaining. */
enum class TimerLevel { NORMAL, WARNING, CRITICAL }

/** Timer thresholds crossed during a sitting; announced once each (NFR-08). */
enum class TimerMilestone { TWENTY_PERCENT, TEN_PERCENT }

data class TestOptionUi(
    val originalIndex: Int,
    val text: String
)

data class TestQuestionUi(
    val questionId: String,
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
    val totalSeconds: Long? = null,
    val bookmarkedIds: Set<String> = emptySet(),
    val timerMilestone: TimerMilestone? = null,
    val finishedAttemptId: String? = null
) {
    val current: TestQuestionUi? get() = questions.getOrNull(currentIndex)
    val total: Int get() = questions.size
    val isLast: Boolean get() = currentIndex >= questions.lastIndex
    val feedbackEnabled: Boolean get() = mode != AttemptMode.MOCK

    /** Practice and drill reveal the answer right after it is given (FR-03). */
    val revealFeedback: Boolean get() = feedbackEnabled && current?.selectedOriginal != null

    /** Mock keeps options editable until submit; practice and drill lock them once answered. */
    val canChangeAnswer: Boolean get() = mode == AttemptMode.MOCK || current?.selectedOriginal == null

    val progress: Float get() = if (total == 0) 0f else (currentIndex + 1f) / total

    val isBookmarked: Boolean get() = current?.questionId?.let { it in bookmarkedIds } == true

    val timerLevel: TimerLevel?
        get() {
            val remaining = remainingSeconds ?: return null
            val total = totalSeconds
            if (total == null || total <= 0L) return null
            return when {
                remaining * 10 <= total -> TimerLevel.CRITICAL
                remaining * 5 <= total -> TimerLevel.WARNING
                else -> TimerLevel.NORMAL
            }
        }
}

@HiltViewModel
class TestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attempts: AttemptRepository,
    private val bookmarks: BookmarkRepository,
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
        observeBookmarks()
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
            val deadlineAt = session.attempt.deadlineAt
            _state.update {
                it.copy(
                    loading = false,
                    title = session.attempt.title(),
                    mode = session.attempt.mode,
                    questions = questions,
                    currentIndex = if (firstUnanswered >= 0) firstUnanswered else questions.lastIndex.coerceAtLeast(0),
                    remainingSeconds = deadlineAt?.let { deadline ->
                        ((deadline - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                    },
                    totalSeconds = deadlineAt?.let { deadline ->
                        ((deadline - session.attempt.startedAt) / 1000L).coerceAtLeast(0L)
                    }
                )
            }
            startTimer(deadlineAt, _state.value.totalSeconds)
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            bookmarks.observeBookmarks().collect { list ->
                _state.update { state ->
                    state.copy(bookmarkedIds = list.mapTo(mutableSetOf()) { question -> question.id })
                }
            }
        }
    }

    private fun startTimer(deadlineAt: Long?, totalSeconds: Long?) {
        if (deadlineAt == null) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var announced = 0
            while (isActive && !finishing) {
                val remaining = ((deadlineAt - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                val reached = milestoneFor(remaining, totalSeconds)
                val crossed = reached > announced
                announced = maxOf(announced, reached)
                _state.update { state ->
                    state.copy(
                        remainingSeconds = remaining,
                        timerMilestone = when {
                            crossed && reached >= 2 -> TimerMilestone.TEN_PERCENT
                            crossed && reached == 1 -> TimerMilestone.TWENTY_PERCENT
                            else -> state.timerMilestone
                        }
                    )
                }
                if (remaining <= 0L) {
                    finish()
                    break
                }
                delay(500)
            }
        }
    }

    private fun milestoneFor(remaining: Long, totalSeconds: Long?): Int = when {
        totalSeconds == null || totalSeconds <= 0L -> 0
        remaining * 10 <= totalSeconds -> 2
        remaining * 5 <= totalSeconds -> 1
        else -> 0
    }

    fun selectOption(originalIndex: Int) {
        val state = _state.value
        val current = state.current ?: return
        if (state.finishedAttemptId != null || !state.canChangeAnswer) return
        viewModelScope.launch {
            submitAnswer(
                attemptId = attemptId,
                position = current.position,
                selectedOption = originalIndex,
                correctOption = current.correctOriginal
            )
            _state.update { latest ->
                latest.copy(
                    questions = latest.questions.map { question ->
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

    fun toggleBookmark() {
        val questionId = _state.value.current?.questionId ?: return
        viewModelScope.launch { bookmarks.toggle(questionId) }
    }

    fun previous() {
        _state.update { state ->
            if (state.currentIndex > 0) state.copy(currentIndex = state.currentIndex - 1) else state
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
            questionId = question.id,
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
