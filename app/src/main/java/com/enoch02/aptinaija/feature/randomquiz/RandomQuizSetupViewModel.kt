package com.enoch02.aptinaija.feature.randomquiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.QuestionCount
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.usecase.BuildAttemptRequest
import com.enoch02.aptinaija.domain.usecase.BuildAttemptUseCase
import com.enoch02.aptinaija.navigation.RandomQuizSetupRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RandomQuizSetupUiState(
    val loading: Boolean = true,
    val questionCount: Int = 20,
    val starting: Boolean = false,
    val attemptId: String? = null,
    val error: String? = null
) {
    val minutes: Int
        get() = questionCount
}

@HiltViewModel
class RandomQuizSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buildAttempt: BuildAttemptUseCase,
    settings: SettingsRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RandomQuizSetupRoute>()

    private val _state = MutableStateFlow(RandomQuizSetupUiState())
    val state: StateFlow<RandomQuizSetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val defaultCount = settings.settings.first().defaultQuestionCount
            _state.update {
                it.copy(
                    loading = false,
                    questionCount = QuestionCount.normalize(route.questionCount ?: defaultCount)
                )
            }
        }
    }

    fun selectQuestionCount(count: Int) {
        _state.update { it.copy(questionCount = QuestionCount.normalize(count), error = null) }
    }

    fun start() {
        if (_state.value.starting) return
        val count = _state.value.questionCount
        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                buildAttempt(
                    BuildAttemptRequest(
                        mode = AttemptMode.RANDOM_QUIZ,
                        questionCount = count
                    )
                )
            }.onSuccess { attemptId ->
                _state.update { it.copy(starting = false, attemptId = attemptId) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        starting = false,
                        error = error.message ?: "Could not start the quiz."
                    )
                }
            }
        }
    }
}
