package com.enoch02.aptinaija.feature.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.ScoreSummary
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.usecase.ScoreAttemptUseCase
import com.enoch02.aptinaija.navigation.ResultsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ResultsError { MISSING_ATTEMPT }

data class ResultsUiState(
    val loading: Boolean = true,
    val summary: ScoreSummary? = null,
    val attempt: Attempt? = null,
    val timeLimitSeconds: Long? = null,
    val error: ResultsError? = null,
    /** A wrong-answer retest has no company, so "Try again" cannot rebuild a setup. */
    val showTryAgain: Boolean = true
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attempts: AttemptRepository,
    private val scoreAttempt: ScoreAttemptUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ResultsRoute>()
    private val attemptId: String = route.attemptId
    private val _state = MutableStateFlow(ResultsUiState())
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val summary = scoreAttempt(attemptId)
            val attempt = attempts.get(attemptId)?.attempt
            val showTryAgain = !route.fromWrongAnswers
            _state.value = if (summary == null) {
                ResultsUiState(
                    loading = false,
                    error = ResultsError.MISSING_ATTEMPT,
                    showTryAgain = showTryAgain
                )
            } else {
                ResultsUiState(
                    loading = false,
                    summary = summary,
                    attempt = attempt,
                    timeLimitSeconds = attempt?.deadlineAt?.let { deadline ->
                        ((deadline - attempt.startedAt) / 1000L).coerceAtLeast(0L)
                    },
                    showTryAgain = showTryAgain
                )
            }
        }
    }
}
