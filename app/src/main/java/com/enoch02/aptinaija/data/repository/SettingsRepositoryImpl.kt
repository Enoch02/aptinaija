package com.enoch02.aptinaija.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.AppThemeMode
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    override val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            AppSettings(
                themeMode = AppThemeMode.fromRaw(
                    prefs[Keys.THEME_MODE] ?: AppThemeMode.AUTO.name
                ),
                shuffleQuestions = prefs[Keys.SHUFFLE_QUESTIONS] ?: true,
                shuffleOptions = prefs[Keys.SHUFFLE_OPTIONS] ?: true,
                defaultQuestionCount = prefs[Keys.DEFAULT_QUESTION_COUNT] ?: 20,
                passMarkPercent = prefs[Keys.PASS_MARK_PERCENT] ?: 50
            )
        }

    override suspend fun setThemeMode(value: AppThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = value.name }
    }

    override suspend fun setShuffleQuestions(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHUFFLE_QUESTIONS] = value }
    }

    override suspend fun setShuffleOptions(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHUFFLE_OPTIONS] = value }
    }

    override suspend fun setDefaultQuestionCount(value: Int) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_QUESTION_COUNT] = value }
    }

    override suspend fun setPassMarkPercent(value: Int) {
        context.settingsDataStore.edit { it[Keys.PASS_MARK_PERCENT] = value }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SHUFFLE_QUESTIONS = booleanPreferencesKey("shuffle_questions")
        val SHUFFLE_OPTIONS = booleanPreferencesKey("shuffle_options")
        val DEFAULT_QUESTION_COUNT = intPreferencesKey("default_question_count")
        val PASS_MARK_PERCENT = intPreferencesKey("pass_mark_percent")
    }
}
