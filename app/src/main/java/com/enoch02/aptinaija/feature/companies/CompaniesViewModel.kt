package com.enoch02.aptinaija.feature.companies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.CompanySummary
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CompaniesViewModel @Inject constructor(
    private val questions: QuestionRepository
) : ViewModel() {

    val companies: StateFlow<List<CompanySummary>> = questions.observeCompanies().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch { runCatching { questions.ensurePackImported() } }
    }
}
