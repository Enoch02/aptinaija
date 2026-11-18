package com.enoch02.aptinaija.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.CompanySummary
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
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
    val companies: List<CompanySummary> = emptyList(),
    val templates: List<TestTemplate> = emptyList(),
    val bookmarkCount: Int = 0,
    val passMarkPercent: Int = 50,
    val error: String? = null
) {
    val questionCount: Int get() = companies.sumOf { it.questionCount }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questions: QuestionRepository,
    attempts: AttemptRepository,
    templates: TestRepository,
    bookmarks: BookmarkRepository,
    settings: SettingsRepository
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        attempts.observeLastCompleted(),
        attempts.observeWeakSections(limit = 3),
        questions.observeCompanies(),
        templates.observeTemplates(),
        settings.settings
    ) { lastAttempt, weakSections, companies, testTemplates, appSettings ->
        HomeUiState(
            loading = false,
            lastAttempt = lastAttempt,
            weakSections = weakSections,
            companies = companies,
            templates = testTemplates,
            passMarkPercent = appSettings.passMarkPercent
        )
    }.combine(bookmarks.observeBookmarks()) { uiState, savedQuestions ->
        uiState.copy(bookmarkCount = savedQuestions.size)
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
