package com.enoch02.aptinaija.feature.testsetup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import com.enoch02.aptinaija.domain.usecase.BuildAttemptRequest
import com.enoch02.aptinaija.domain.usecase.BuildAttemptUseCase
import com.enoch02.aptinaija.navigation.TestSetupRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TestSetupUiState(
    val loading: Boolean = true,
    val mode: AttemptMode = AttemptMode.PRACTICE,
    val company: String? = null,
    val section: String? = null,
    val template: TestTemplate? = null,
    val questionCount: Int = 20,
    val starting: Boolean = false,
    val attemptId: String? = null,
    val error: String? = null
) {
    val isMock: Boolean get() = mode == AttemptMode.MOCK
    val mockQuestionCount: Int get() = template?.sections?.sumOf { it.questionCount } ?: 0
    val mockMinutes: Int get() = template?.sections?.sumOf { it.minutes } ?: 0
}

@HiltViewModel
class TestSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buildAttempt: BuildAttemptUseCase,
    private val templates: TestRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TestSetupRoute>()
    private val mode = runCatching { AttemptMode.valueOf(route.mode) }.getOrDefault(AttemptMode.PRACTICE)

    private val _state = MutableStateFlow(
        TestSetupUiState(
            mode = mode,
            company = route.company,
            section = route.section
        )
    )
    val state: StateFlow<TestSetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val defaultCount = settings.settings.first().defaultQuestionCount
            val template = route.templateId?.let { templates.template(it) }
            _state.update {
                it.copy(
                    loading = false,
                    questionCount = defaultCount,
                    template = template,
                    company = if (mode == AttemptMode.MOCK) template?.companyId else it.company
                )
            }
        }
    }

    fun changeQuestionCount(delta: Int) {
        _state.update {
            it.copy(questionCount = (it.questionCount + delta).coerceIn(5, 100))
        }
    }

    fun start() {
        val current = _state.value
        if (current.starting || current.attemptId != null) return
        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                buildAttempt(
                    BuildAttemptRequest(
                        mode = mode,
                        company = current.company,
                        section = current.section,
                        templateId = route.templateId,
                        questionCount = current.questionCount
                    )
                )
            }.onSuccess { attemptId ->
                _state.update { it.copy(starting = false, attemptId = attemptId) }
            }.onFailure { error ->
                _state.update {
                    it.copy(starting = false, error = error.message ?: "Could not start the test")
                }
            }
        }
    }
}
