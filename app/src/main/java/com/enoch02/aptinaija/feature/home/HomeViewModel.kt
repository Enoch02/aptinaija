package com.enoch02.aptinaija.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val lastAttempt: Attempt? = null,
    val weakSections: List<SectionWeakness> = emptyList(),
    val templates: List<TestTemplate> = emptyList(),
    val passMarkPercent: Int = 50
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questions: QuestionRepository,
    attempts: AttemptRepository,
    templates: TestRepository,
    settings: SettingsRepository
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        attempts.observeLastCompleted(),
        attempts.observeWeakSections(limit = 3),
        templates.observeTemplates(),
        settings.settings
    ) { lastAttempt, weakSections, testTemplates, appSettings ->
        HomeUiState(
            loading = false,
            lastAttempt = lastAttempt,
            weakSections = weakSections,
            templates = testTemplates,
            passMarkPercent = appSettings.passMarkPercent
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
}
