package com.enoch02.aptinaija.data.repository

import com.enoch02.aptinaija.data.local.dao.BookmarkDao
import com.enoch02.aptinaija.data.local.entity.BookmarkEntity
import com.enoch02.aptinaija.data.mapper.toDomain
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun observeBookmarks(): Flow<List<Question>> =
        bookmarkDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = bookmarkDao.observeCount()

    override fun observeIsBookmarked(questionId: String): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(questionId)

    override suspend fun toggle(questionId: String) {
        if (bookmarkDao.isBookmarked(questionId)) {
            bookmarkDao.delete(questionId)
        } else {
            bookmarkDao.insert(
                BookmarkEntity(questionId = questionId, createdAt = System.currentTimeMillis())
            )
        }
    }
}
