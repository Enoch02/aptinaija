package com.enoch02.aptinaija.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enoch02.aptinaija.data.local.entity.AttemptEntity
import com.enoch02.aptinaija.data.local.entity.AttemptQuestionEntity
import kotlinx.coroutines.flow.Flow

data class AttemptItemRow(
    val position: Int,
    val optionOrderJson: String,
    val selectedOption: Int?,
    val isCorrect: Boolean?,
    val id: String,
    val company: String,
    val section: String,
    val difficulty: String,
    val stem: String,
    val optionsJson: String,
    val answerIndex: Int,
    val explanation: String,
    val passage: String?,
    val sourceUrl: String?,
    val tagsJson: String,
    val status: String
)

data class SectionScoreRow(
    val section: String,
    val total: Int,
    val correct: Long
)

data class SectionWeaknessRow(
    val section: String,
    val wrong: Long,
    val total: Int
)

@Dao
interface AttemptDao {

    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Insert
    suspend fun insertItems(items: List<AttemptQuestionEntity>)

    @Query("SELECT * FROM attempts WHERE id = :id")
    suspend fun attempt(id: String): AttemptEntity?

    @Query(
        """
        SELECT aq.position, aq.optionOrderJson, aq.selectedOption, aq.isCorrect, q.*
        FROM attempt_questions aq
        INNER JOIN questions q ON q.id = aq.questionId
        WHERE aq.attemptId = :attemptId
        ORDER BY aq.position
        """
    )
    suspend fun items(attemptId: String): List<AttemptItemRow>

    @Query("SELECT * FROM attempts WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun observeHistory(): Flow<List<AttemptEntity>>

    @Query(
        """
        SELECT * FROM attempts
        WHERE status = 'COMPLETED'
        ORDER BY completedAt DESC
        LIMIT 1
        """
    )
    fun observeLastCompleted(): Flow<AttemptEntity?>

    @Query(
        """
        SELECT section, COUNT(*) AS total, SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) AS correct
        FROM attempt_questions
        WHERE attemptId = :attemptId
        GROUP BY section
        ORDER BY MIN(position)
        """
    )
    suspend fun sectionScores(attemptId: String): List<SectionScoreRow>

    @Query(
        """
        SELECT aq.section,
               SUM(CASE WHEN aq.isCorrect = 0 THEN 1 ELSE 0 END) AS wrong,
               COUNT(*) AS total
        FROM attempt_questions aq
        INNER JOIN attempts a ON a.id = aq.attemptId
        WHERE a.status = 'COMPLETED'
        GROUP BY aq.section
        ORDER BY wrong DESC
        LIMIT :limit
        """
    )
    fun observeWeakSections(limit: Int): Flow<List<SectionWeaknessRow>>

    @Query(
        """
        UPDATE attempt_questions
        SET selectedOption = :selectedOption, isCorrect = :isCorrect, answeredAt = :answeredAt
        WHERE attemptId = :attemptId AND position = :position
        """
    )
    suspend fun recordAnswer(
        attemptId: String,
        position: Int,
        selectedOption: Int,
        isCorrect: Boolean,
        answeredAt: Long
    )

    @Query(
        "SELECT questionId FROM attempt_questions WHERE attemptId = :attemptId AND position = :position"
    )
    suspend fun questionIdAt(attemptId: String, position: Int): String?

    @Query(
        """
        UPDATE attempts
        SET status = 'COMPLETED', correctCount = :correctCount, completedAt = :completedAt
        WHERE id = :attemptId
        """
    )
    suspend fun complete(attemptId: String, correctCount: Int, completedAt: Long)
}
