package com.enoch02.aptinaija.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.QuestionCount
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import com.enoch02.aptinaija.domain.usecase.BuildAttemptRequest
import com.enoch02.aptinaija.domain.usecase.BuildAttemptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val lastAttempt: Attempt? = null,
    val weakSections: List<SectionWeakness> = emptyList(),
    val templates: List<TestTemplate> = emptyList(),
    val passMarkPercent: Int = 50,
    val startingDrill: Boolean = false,
    val drillAttemptId: String? = null,
    val error: String? = null
)

private data class DrillState(
    val starting: Boolean = false,
    val attemptId: String? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questions: QuestionRepository,
    attempts: AttemptRepository,
    templates: TestRepository,
    private val settings: SettingsRepository,
    private val buildAttempt: BuildAttemptUseCase
) : ViewModel() {

    private val drillState = MutableStateFlow(DrillState())

    val state: StateFlow<HomeUiState> = combine(
        attempts.observeLastCompleted(),
        attempts.observeWeakSections(limit = 3),
        templates.observeTemplates(),
        settings.settings,
        drillState
    ) { lastAttempt, weakSections, testTemplates, appSettings, drill ->
        HomeUiState(
            loading = false,
            lastAttempt = lastAttempt,
            weakSections = weakSections,
            templates = testTemplates,
            passMarkPercent = appSettings.passMarkPercent,
            startingDrill = drill.starting,
            drillAttemptId = drill.attemptId,
            error = drill.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            runCatching { questions.ensurePackImported() }
        }
    }

    /** FR-04: the weak-section shortcut has no options, so build and launch the drill directly. */
    fun startDrill(section: String) {
        if (drillState.value.starting) return
        drillState.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val defaultCount = QuestionCount.normalize(settings.settings.first().defaultQuestionCount)
                buildAttempt(
                    BuildAttemptRequest(
                        mode = AttemptMode.DRILL,
                        section = section,
                        questionCount = defaultCount
                    )
                )
            }.onSuccess { attemptId ->
                drillState.update { it.copy(starting = false, attemptId = attemptId) }
            }.onFailure { error ->
                drillState.update {
                    it.copy(
                        starting = false,
                        error = error.message ?: "Could not start the drill."
                    )
                }
            }
        }
    }

    fun consumeDrillAttemptId() {
        drillState.update { it.copy(attemptId = null) }
    }

    fun dismissError() {
        drillState.update { it.copy(error = null) }
    }
}
