package com.enoch02.aptinaija.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enoch02.aptinaija.data.local.entity.BookmarkEntity
import com.enoch02.aptinaija.data.local.entity.QuestionEntity
import com.enoch02.aptinaija.data.local.entity.WrongQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN bookmarks b ON b.questionId = q.id
        ORDER BY b.createdAt DESC
        """
    )
    fun observeAll(): Flow<List<QuestionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE questionId = :questionId)")
    fun observeIsBookmarked(questionId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE questionId = :questionId)")
    suspend fun isBookmarked(questionId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE questionId = :questionId")
    suspend fun delete(questionId: String)
}

@Dao
interface WrongQuestionDao {

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN wrong_questions w ON w.questionId = q.id
        WHERE w.resolvedAt IS NULL
        ORDER BY w.lastWrongAt DESC
        """
    )
    fun observeUnresolved(): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wrongQuestion: WrongQuestionEntity)

    @Query("UPDATE wrong_questions SET resolvedAt = :resolvedAt WHERE questionId = :questionId")
    suspend fun resolve(questionId: String, resolvedAt: Long)
}
