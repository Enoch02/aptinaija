package com.enoch02.aptinaija.data.repository

import com.enoch02.aptinaija.data.local.dao.QuestionDao
import com.enoch02.aptinaija.data.mapper.toDomain
import com.enoch02.aptinaija.data.pack.QuestionPackImporter
import com.enoch02.aptinaija.domain.model.CompanySummary
import com.enoch02.aptinaija.domain.model.Question
import com.enoch02.aptinaija.domain.model.SectionSummary
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val importer: QuestionPackImporter
) : QuestionRepository {

    override suspend fun ensurePackImported() = importer.importIfNeeded()

    override fun observeCompanies(): Flow<List<CompanySummary>> =
        questionDao.observeCompanyCounts().map { rows ->
            rows.map { CompanySummary(company = it.company, questionCount = it.questionCount) }
        }

    override fun observeSections(company: String): Flow<List<SectionSummary>> =
        questionDao.observeSectionCounts(company).map { rows ->
            rows.map { SectionSummary(section = it.section, questionCount = it.questionCount) }
        }

    override fun observeQuestions(company: String, section: String?): Flow<List<Question>> =
        questionDao.observeFiltered(company, section).map { rows -> rows.map { it.toDomain() } }

    override suspend fun questionById(id: String): Question? = questionDao.byId(id)?.toDomain()

    override suspend fun randomQuestions(
        company: String?,
        section: String?,
        limit: Int
    ): List<Question> = questionDao.random(company, section, limit).map { it.toDomain() }
}
