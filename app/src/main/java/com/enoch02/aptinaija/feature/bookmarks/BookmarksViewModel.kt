package com.enoch02.aptinaija.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.AttemptMode
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
import com.enoch02.aptinaija.domain.usecase.BuildAttemptRequest
import com.enoch02.aptinaija.domain.usecase.BuildAttemptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookmarksUiState(
    val loading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val starting: Boolean = false,
    val attemptId: String? = null,
    val error: String? = null
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarks: BookmarkRepository,
    private val buildAttempt: BuildAttemptUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksUiState())
    val state: StateFlow<BookmarksUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            bookmarks.observeBookmarks().collect { questions ->
                _state.update { it.copy(loading = false, questions = questions) }
            }
        }
    }

    fun remove(questionId: String) {
        viewModelScope.launch { bookmarks.toggle(questionId) }
    }

    /** FR-10: a practice-style attempt over bookmarked questions. */
    fun startRetest() {
        val current = _state.value
        if (current.starting || current.questions.isEmpty()) return
        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                buildAttempt(
                    BuildAttemptRequest(
                        mode = AttemptMode.PRACTICE,
                        bookmarksOnly = true
                    )
                )
            }.onSuccess { attemptId ->
                _state.update { it.copy(starting = false, attemptId = attemptId) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        starting = false,
                        error = error.message ?: "Could not start the retest."
                    )
                }
            }
        }
    }

    fun consumeAttemptId() {
        _state.update { it.copy(attemptId = null) }
    }
}
