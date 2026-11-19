package com.enoch02.aptinaija.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.enoch02.aptinaija.domain.model.AttemptSession
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
import com.enoch02.aptinaija.navigation.ReviewRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReviewItemUi(
    val questionId: String,
    val position: Int,
    val section: String,
    val passage: String?,
    val stem: String,
    val options: List<String>,
    val selectedDisplayIndex: Int?,
    val correctDisplayIndex: Int,
    val isCorrect: Boolean?,
    val explanation: String,
    val bookmarked: Boolean
)

enum class ReviewError { MISSING_ATTEMPT }

data class ReviewUiState(
    val loading: Boolean = true,
    val error: ReviewError? = null,
    val wrongOnly: Boolean = false,
    val items: List<ReviewItemUi> = emptyList()
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attempts: AttemptRepository,
    private val bookmarks: BookmarkRepository
) : ViewModel() {

    private val attemptId: String = savedStateHandle.toRoute<ReviewRoute>().attemptId
    private val session = MutableStateFlow<AttemptSession?>(null)
    private val loaded = MutableStateFlow(false)
    private val wrongOnly = MutableStateFlow(false)

    private val bookmarkedIds = bookmarks.observeBookmarks()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val state: StateFlow<ReviewUiState> = combine(
        session,
        loaded,
        wrongOnly,
        bookmarkedIds
    ) { current, isLoaded, onlyWrong, marked ->
        val items = current?.items.orEmpty()
            .filter { !onlyWrong || it.isCorrect == false }
            .map { item ->
                val order = item.optionOrder.ifEmpty { item.question.options.indices.toList() }
                ReviewItemUi(
                    questionId = item.question.id,
                    position = item.position,
                    section = item.question.section,
                    passage = item.question.passage,
                    stem = item.question.stem,
                    options = order.mapNotNull { item.question.options.getOrNull(it) },
                    selectedDisplayIndex = item.selectedOption?.let { selected ->
                        order.indexOf(selected).takeIf { index -> index >= 0 }
                    },
                    correctDisplayIndex = order.indexOf(item.question.answerIndex),
                    isCorrect = item.isCorrect,
                    explanation = item.question.explanation,
                    bookmarked = item.question.id in marked
                )
            }
        ReviewUiState(
            loading = !isLoaded,
            error = if (isLoaded && current == null) ReviewError.MISSING_ATTEMPT else null,
            wrongOnly = onlyWrong,
            items = items
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewUiState()
    )

    init {
        viewModelScope.launch {
            session.value = attempts.get(attemptId)
            loaded.value = true
        }
    }

    fun setWrongOnly(value: Boolean) {
        wrongOnly.value = value
    }

    fun toggleBookmark(questionId: String) {
        viewModelScope.launch { bookmarks.toggle(questionId) }
    }
}
