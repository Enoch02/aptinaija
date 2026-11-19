package com.enoch02.aptinaija.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    attempts: AttemptRepository,
    templates: TestRepository
) : ViewModel() {

    val state: StateFlow<HistoryUiState> = combine(
        attempts.observeHistory(),
        templates.observeTemplates()
    ) { attemptList, templateList ->
        HistoryUiState(
            loading = false,
            entries = buildHistoryEntries(attemptList, templateList)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )
}
