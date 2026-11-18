package com.enoch02.aptinaija.feature.testsetup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.QuestionRepository
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
    val companyId: String? = null,
    val companyName: String = "",
    val sections: List<SectionSummary> = emptyList(),
    val templates: List<TestTemplate> = emptyList(),
    val selectedSection: String? = null,
    val selectedTemplateId: String? = null,
    val questionCount: Int = 20,
    val starting: Boolean = false,
    val attemptId: String? = null,
    val error: String? = null
) {
    val selectedTemplate: TestTemplate?
        get() = templates.firstOrNull { it.id == selectedTemplateId }

    val practiceQuestionCount: Int
        get() = sections.sumOf { it.questionCount }

    val drillQuestionCount: Int
        get() = sections.firstOrNull { it.section == selectedSection }?.questionCount ?: 0

    val mockQuestionCount: Int
        get() = selectedTemplate?.sections?.sumOf { it.questionCount } ?: 0

    val mockMinutes: Int
        get() = selectedTemplate?.sections?.sumOf { it.minutes } ?: 0

    val canBegin: Boolean
        get() = !starting && when (mode) {
            AttemptMode.PRACTICE -> companyId != null && sections.isNotEmpty()
            AttemptMode.DRILL -> selectedSection != null
            AttemptMode.MOCK -> selectedTemplate?.sections?.isNotEmpty() == true
        }
}

@HiltViewModel
class TestSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buildAttempt: BuildAttemptUseCase,
    questions: QuestionRepository,
    templates: TestRepository,
    settings: SettingsRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TestSetupRoute>()
    private val initialMode = runCatching {
        AttemptMode.valueOf(route.mode)
    }.getOrDefault(AttemptMode.PRACTICE)

    private val _state = MutableStateFlow(TestSetupUiState(mode = initialMode))
    val state: StateFlow<TestSetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val defaultCount = settings.settings.first().defaultQuestionCount
            val routeTemplate = route.templateId?.let { templates.template(it) }
            val companyId = route.company ?: routeTemplate?.companyId
            val companyTemplates = if (companyId != null) {
                templates.observeTemplates().first()
                    .filter { it.companyId.equals(companyId, ignoreCase = true) }
            } else {
                listOfNotNull(routeTemplate)
            }
            val sections = if (companyId != null) {
                questions.observeSections(companyId).first()
            } else {
                emptyList()
            }
            val companyName = routeTemplate?.company
                ?.trim()
                ?.ifBlank { companyId.orEmpty() }
                ?: companyTemplates.firstOrNull()?.company
                    ?.trim()
                    ?.ifBlank { companyId.orEmpty() }
                ?: companyId.orEmpty()

            _state.update {
                it.copy(
                    loading = false,
                    companyId = companyId,
                    companyName = companyName,
                    sections = sections,
                    templates = companyTemplates,
                    selectedSection = route.section ?: sections.firstOrNull()?.section,
                    selectedTemplateId = route.templateId
                        ?: companyTemplates.firstOrNull()?.id,
                    questionCount = defaultCount.normalizedQuestionCount()
                )
            }
        }
    }

    fun selectMode(mode: AttemptMode) {
        _state.update { current ->
            current.copy(
                mode = mode,
                selectedSection = if (mode == AttemptMode.DRILL) {
                    current.selectedSection ?: current.sections.firstOrNull()?.section
                } else {
                    current.selectedSection
                },
                selectedTemplateId = if (mode == AttemptMode.MOCK) {
                    current.selectedTemplateId ?: current.templates.firstOrNull()?.id
                } else {
                    current.selectedTemplateId
                },
                error = null
            )
        }
    }

    fun selectSection(section: String) {
        _state.update { it.copy(selectedSection = section, error = null) }
    }

    fun selectTemplate(templateId: String) {
        _state.update { it.copy(selectedTemplateId = templateId, error = null) }
    }

    fun selectQuestionCount(count: Int) {
        _state.update {
            it.copy(questionCount = count.normalizedQuestionCount(), error = null)
        }
    }

    fun start() {
        val current = _state.value
        if (!current.canBegin) return
        val section = if (current.mode == AttemptMode.DRILL) {
            current.selectedSection
        } else {
            null
        }
        val templateId = if (current.mode == AttemptMode.MOCK) {
            current.selectedTemplateId
        } else {
            null
        }

        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                buildAttempt(
                    BuildAttemptRequest(
                        mode = current.mode,
                        company = current.companyId,
                        section = section,
                        templateId = templateId,
                        questionCount = current.questionCount
                    )
                )
            }.onSuccess { attemptId ->
                _state.update { it.copy(starting = false, attemptId = attemptId) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        starting = false,
                        error = error.message ?: "Could not start the test"
                    )
                }
            }
        }
    }

    private fun Int.normalizedQuestionCount(): Int =
        if (this in QUESTION_COUNTS) this else 20

    private companion object {
        val QUESTION_COUNTS = setOf(10, 20, 40)
    }
}
