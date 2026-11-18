package com.enoch02.aptinaija.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarks: BookmarkRepository
) : ViewModel() {

    val bookmarksState: StateFlow<List<Question>> = bookmarks.observeBookmarks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun remove(questionId: String) {
        viewModelScope.launch { bookmarks.toggle(questionId) }
    }
}
