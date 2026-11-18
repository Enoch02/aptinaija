package com.enoch02.aptinaija.feature.companies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import com.enoch02.aptinaija.navigation.CompanyRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CompanyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    questions: QuestionRepository,
    templates: TestRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CompanyRoute>()
    val company: String = route.company

    val sections: StateFlow<List<SectionSummary>> = questions.observeSections(company).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val templates: StateFlow<List<TestTemplate>> = templates.observeTemplates()
        .map { list -> list.filter { it.company.equals(company, ignoreCase = true) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
