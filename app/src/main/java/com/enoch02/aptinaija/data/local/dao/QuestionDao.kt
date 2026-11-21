package com.enoch02.aptinaija.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enoch02.aptinaija.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

data class CompanyCountRow(
    val company: String,
    val questionCount: Int
)

data class SectionCountRow(
    val section: String,
    val questionCount: Int
)

@Dao
interface QuestionDao {

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun byId(id: String): QuestionEntity?

    @Query(
        """
        SELECT * FROM questions
        WHERE (:company IS NULL OR company = :company)
          AND (:section IS NULL OR section = :section)
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun random(company: String?, section: String?, limit: Int): List<QuestionEntity>

    @Query(
        """
        SELECT * FROM questions
        WHERE company = :company AND (:section IS NULL OR section = :section)
        ORDER BY id
        """
    )
    fun observeFiltered(company: String, section: String?): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT company, COUNT(*) AS questionCount
        FROM questions
        GROUP BY company
        ORDER BY company
        """
    )
    fun observeCompanyCounts(): Flow<List<CompanyCountRow>>

    @Query(
        """
        SELECT section, COUNT(*) AS questionCount
        FROM questions
        WHERE company = :company
        GROUP BY section
        ORDER BY section
        """
    )
    fun observeSectionCounts(company: String): Flow<List<SectionCountRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun clear()
}
