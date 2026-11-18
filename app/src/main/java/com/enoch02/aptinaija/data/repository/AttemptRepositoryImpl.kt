package com.enoch02.aptinaija.data.repository

import androidx.room.withTransaction
import com.enoch02.aptinaija.data.local.AptiNaijaDatabase
import com.enoch02.aptinaija.data.local.dao.AttemptDao
import com.enoch02.aptinaija.data.local.dao.WrongQuestionDao
import com.enoch02.aptinaija.data.local.entity.AttemptEntity
import com.enoch02.aptinaija.data.local.entity.AttemptQuestionEntity
import com.enoch02.aptinaija.data.local.entity.WrongQuestionEntity
import com.enoch02.aptinaija.data.mapper.encodeStringList
import com.enoch02.aptinaija.data.mapper.toDomain
import com.enoch02.aptinaija.domain.model.Attempt
import com.enoch02.aptinaija.domain.model.AttemptItem
import com.enoch02.aptinaija.domain.model.AttemptSession
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.model.SectionScore
import com.enoch02.aptinaija.domain.model.SectionWeakness
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AttemptRepositoryImpl @Inject constructor(
    private val database: AptiNaijaDatabase,
    private val attemptDao: AttemptDao,
    private val wrongQuestionDao: WrongQuestionDao
) : AttemptRepository {

    override suspend fun create(attempt: Attempt, items: List<AttemptItem>): String =
        database.withTransaction {
            attemptDao.insertAttempt(
                AttemptEntity(
                    id = attempt.id,
                    templateId = attempt.templateId,
                    mode = attempt.mode.name,
                    company = attempt.company,
                    section = attempt.section,
                    startedAt = attempt.startedAt,
                    completedAt = attempt.completedAt,
                    deadlineAt = attempt.deadlineAt,
                    totalQuestions = attempt.totalQuestions,
                    correctCount = attempt.correctCount,
                    status = attempt.status.name
                )
            )
            attemptDao.insertItems(
                items.map { item ->
                    AttemptQuestionEntity(
                        attemptId = attempt.id,
                        position = item.position,
                        questionId = item.question.id,
                        section = item.question.section,
                        optionOrderJson = encodeStringList(item.optionOrder.map(Int::toString)),
                        selectedOption = item.selectedOption,
                        isCorrect = item.isCorrect,
                        answeredAt = null
                    )
                }
            )
            attempt.id
        }

    override suspend fun get(id: String): AttemptSession? {
        val attempt = attemptDao.attempt(id) ?: return null
        return AttemptSession(
            attempt = attempt.toDomain(),
            items = attemptDao.items(id).map { it.toDomain() }
        )
    }

    override suspend fun recordAnswer(
        attemptId: String,
        position: Int,
        selectedOption: Int,
        isCorrect: Boolean
    ) {
        val now = System.currentTimeMillis()
        attemptDao.recordAnswer(attemptId, position, selectedOption, isCorrect, now)
        val questionId = attemptDao.questionIdAt(attemptId, position) ?: return
        if (isCorrect) {
            wrongQuestionDao.resolve(questionId, now)
        } else {
            wrongQuestionDao.upsert(WrongQuestionEntity(questionId = questionId, lastWrongAt = now))
        }
    }

    override suspend fun complete(attemptId: String, correctCount: Int) {
        attemptDao.complete(attemptId, correctCount, System.currentTimeMillis())
    }

    override fun observeHistory(): Flow<List<Attempt>> =
        attemptDao.observeHistory().map { rows -> rows.map { it.toDomain() } }

    override fun observeLastCompleted(): Flow<Attempt?> =
        attemptDao.observeLastCompleted().map { it?.toDomain() }

    override fun observeWeakSections(limit: Int): Flow<List<SectionWeakness>> =
        attemptDao.observeWeakSections(limit).map { rows ->
            rows.map {
                SectionWeakness(section = it.section, wrong = it.wrong.toInt(), total = it.total)
            }
        }

    override suspend fun sectionScores(attemptId: String): List<SectionScore> =
        attemptDao.sectionScores(attemptId).map {
            SectionScore(section = it.section, total = it.total, correct = it.correct.toInt())
        }

    override fun observeWrongQuestions(): Flow<List<Question>> =
        wrongQuestionDao.observeUnresolved().map { rows -> rows.map { it.toDomain() } }

    override suspend fun resolveWrong(questionId: String) {
        wrongQuestionDao.resolve(questionId, System.currentTimeMillis())
    }
}
