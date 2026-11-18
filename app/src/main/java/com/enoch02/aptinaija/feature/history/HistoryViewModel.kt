package com.enoch02.aptinaija.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryEntry(
    val id: String,
    val title: String,
    val dateLabel: String,
    val percent: Int
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    attempts: AttemptRepository
) : ViewModel() {

    val history: StateFlow<List<HistoryEntry>> = attempts.observeHistory()
        .map { list -> list.map { it.toEntry() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private fun Attempt.toEntry(): HistoryEntry {
        val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        return HistoryEntry(
            id = id,
            title = company ?: section ?: "Practice",
            dateLabel = formatter.format(Date(startedAt)),
            percent = percent
        )
    }
}
