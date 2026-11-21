package com.enoch02.aptinaija.feature.wronganswers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.usecase.BuildAttemptRequest
import com.enoch02.aptinaija.domain.usecase.BuildAttemptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WrongAnswersUiState(
    val loading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val starting: Boolean = false,
    val attemptId: String? = null,
    val error: String? = null
)

@HiltViewModel
class WrongAnswersViewModel @Inject constructor(
    private val attempts: AttemptRepository,
    private val buildAttempt: BuildAttemptUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WrongAnswersUiState())
    val state: StateFlow<WrongAnswersUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            attempts.observeWrongQuestions().collect { questions ->
                _state.update { it.copy(loading = false, questions = questions) }
            }
        }
    }

    /** FR-09: a practice-style attempt over the unresolved wrong-answer bank. */
    fun startRetest() {
        val current = _state.value
        if (current.starting || current.questions.isEmpty()) return
        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                buildAttempt(
                    BuildAttemptRequest(
                        mode = AttemptMode.PRACTICE,
                        wrongBankOnly = true
                    )
                )
            }.onSuccess { attemptId ->
                _state.update { it.copy(starting = false, attemptId = attemptId) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        starting = false,
                        error = error.message ?: "Could not start the retest."
                    )
                }
            }
        }
    }

    fun consumeAttemptId() {
        _state.update { it.copy(attemptId = null) }
    }
}
