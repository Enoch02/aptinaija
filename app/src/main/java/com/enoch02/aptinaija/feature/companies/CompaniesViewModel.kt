package com.enoch02.aptinaija.feature.companies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompanyListItem(
    val company: String,
    val initials: String,
    val templateCount: Int,
    val questionCount: Int,
    val totalMinutes: Int,
    val sections: Set<String>,
    val providers: Set<String>
)

data class CompaniesUiState(
    val loading: Boolean = true,
    val companies: List<CompanyListItem> = emptyList(),
    val sections: List<String> = emptyList(),
    val providers: List<String> = emptyList(),
    val selectedSections: Set<String> = emptySet(),
    val selectedProviders: Set<String> = emptySet()
) {
    val hasActiveFilters: Boolean
        get() = selectedSections.isNotEmpty() || selectedProviders.isNotEmpty()
}

internal data class CompaniesFilters(
    val sections: Set<String> = emptySet(),
    val providers: Set<String> = emptySet()
)

@HiltViewModel
class CompaniesViewModel @Inject constructor(
    private val questions: QuestionRepository,
    templates: TestRepository
) : ViewModel() {

    private val filters = MutableStateFlow(CompaniesFilters())
    private val imported = MutableStateFlow(false)

    val state: StateFlow<CompaniesUiState> = combine(
        templates.observeTemplates(),
        filters,
        imported
    ) { testTemplates, currentFilters, importComplete ->
        val allCompanies = buildCompanyItems(testTemplates)
        CompaniesUiState(
            loading = !importComplete,
            companies = filterCompanies(allCompanies, currentFilters),
            sections = sectionOptions(testTemplates),
            providers = providerOptions(testTemplates),
            selectedSections = currentFilters.sections,
            selectedProviders = currentFilters.providers
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CompaniesUiState()
    )

    init {
        viewModelScope.launch {
            runCatching { questions.ensurePackImported() }
            imported.value = true
        }
    }

    fun toggleSection(section: String) {
        filters.update { current ->
            current.copy(
                sections = if (section in current.sections) {
                    current.sections - section
                } else {
                    current.sections + section
                }
            )
        }
    }

    fun toggleProvider(provider: String) {
        filters.update { current ->
            current.copy(
                providers = if (provider in current.providers) {
                    current.providers - provider
                } else {
                    current.providers + provider
                }
            )
        }
    }

    fun clearProviders() {
        filters.update { it.copy(providers = emptySet()) }
    }

    fun clearFilters() {
        filters.value = CompaniesFilters()
    }
}

internal fun buildCompanyItems(templates: List<TestTemplate>): List<CompanyListItem> =
    templates
        .groupBy { it.company.trim().lowercase() }
        .values
        .map { group ->
            val company = group.first().company.trim().ifBlank { "General" }
            val sections = group
                .flatMap { it.sections }
                .map { it.section }
                .toSet()
            val providers = group.map { it.provider }.toSet()
            CompanyListItem(
                company = company,
                initials = companyInitials(company),
                templateCount = group.size,
                questionCount = group.sumOf { template ->
                    template.sections.sumOf { it.questionCount }
                },
                totalMinutes = group.sumOf { template ->
                    template.sections.sumOf { it.minutes }
                },
                sections = sections,
                providers = providers
            )
        }
        .sortedBy { it.company.lowercase() }

internal fun filterCompanies(
    companies: List<CompanyListItem>,
    filters: CompaniesFilters
): List<CompanyListItem> = companies.filter { company ->
    val matchesSection = filters.sections.isEmpty() ||
        company.sections.any { it in filters.sections }
    val matchesProvider = filters.providers.isEmpty() ||
        company.providers.any { it in filters.providers }
    matchesSection && matchesProvider
}

internal fun sectionOptions(templates: List<TestTemplate>): List<String> =
    templates
        .flatMap { it.sections }
        .map { it.section }
        .distinct()
        .sorted()

internal fun providerOptions(templates: List<TestTemplate>): List<String> =
    templates
        .map { it.provider }
        .distinct()
        .sorted()

internal fun companyInitials(company: String): String {
    val words = company
        .substringBefore('(')
        .trim()
        .split(Regex("[^A-Za-z0-9]+"))
        .filter { it.isNotEmpty() }

    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(2).uppercase()
        else -> words.take(2).joinToString("") { it.first().uppercase() }
    }
}
