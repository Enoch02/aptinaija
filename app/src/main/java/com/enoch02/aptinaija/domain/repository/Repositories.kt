package com.enoch02.aptinaija.domain.repository

import com.enoch02.aptinaija.domain.model.AppSettings
import com.enoch02.aptinaija.domain.model.AppThemeMode
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptSession
import com.enoch02.aptinaija.domain.model.CompanySummary
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.model.SectionScore
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.model.TestTemplate
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    suspend fun ensurePackImported()
    fun observeCompanies(): Flow<List<CompanySummary>>
    fun observeSections(company: String): Flow<List<SectionSummary>>
    fun observeQuestions(company: String, section: String?): Flow<List<Question>>
    suspend fun questionById(id: String): Question?
    suspend fun randomQuestions(company: String?, section: String?, limit: Int): List<Question>
}

interface TestRepository {
    fun observeTemplates(): Flow<List<TestTemplate>>
    suspend fun template(id: String): TestTemplate?
}

interface AttemptRepository {
    suspend fun create(attempt: Attempt, items: List<AttemptItem>): String
    suspend fun get(id: String): AttemptSession?
    suspend fun recordAnswer(attemptId: String, position: Int, selectedOption: Int, isCorrect: Boolean)
    suspend fun complete(attemptId: String, correctCount: Int)
    fun observeHistory(): Flow<List<Attempt>>
    fun observeLastCompleted(): Flow<Attempt?>
    fun observeWeakSections(limit: Int): Flow<List<SectionWeakness>>
    suspend fun sectionScores(attemptId: String): List<SectionScore>
    fun observeWrongQuestions(): Flow<List<Question>>
    suspend fun resolveWrong(questionId: String)
}

interface BookmarkRepository {
    fun observeBookmarks(): Flow<List<Question>>
    fun observeCount(): Flow<Int>
    fun observeIsBookmarked(questionId: String): Flow<Boolean>
    suspend fun toggle(questionId: String)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(value: AppThemeMode)
    suspend fun setShuffleQuestions(value: Boolean)
    suspend fun setShuffleOptions(value: Boolean)
    suspend fun setDefaultQuestionCount(value: Int)
    suspend fun setPassMarkPercent(value: Int)
}
