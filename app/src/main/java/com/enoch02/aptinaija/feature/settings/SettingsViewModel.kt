package com.enoch02.aptinaija.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.AppThemeMode
import com.enoch02.aptinaija.domain.model.QuestionCount
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setThemeMode(value: AppThemeMode) {
        viewModelScope.launch { repository.setThemeMode(value) }
    }

    fun setShuffleQuestions(value: Boolean) {
        viewModelScope.launch { repository.setShuffleQuestions(value) }
    }

    fun setShuffleOptions(value: Boolean) {
        viewModelScope.launch { repository.setShuffleOptions(value) }
    }

    fun setDefaultQuestionCount(value: Int) {
        viewModelScope.launch { repository.setDefaultQuestionCount(QuestionCount.normalize(value)) }
    }

    fun setPassMarkPercent(value: Int) {
        viewModelScope.launch { repository.setPassMarkPercent(value.coerceIn(10, 90)) }
    }
}
