package com.enoch02.aptinaija.feature.wronganswers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WrongAnswersViewModel @Inject constructor(
    private val attempts: AttemptRepository
) : ViewModel() {

    val questions: StateFlow<List<Question>> = attempts.observeWrongQuestions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun resolve(questionId: String) {
        viewModelScope.launch { attempts.resolveWrong(questionId) }
    }
}
