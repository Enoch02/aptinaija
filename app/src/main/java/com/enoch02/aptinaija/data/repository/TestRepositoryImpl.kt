package com.enoch02.aptinaija.data.repository

import com.enoch02.aptinaija.data.local.dao.TestTemplateDao
import com.enoch02.aptinaija.data.mapper.toDomain
import com.enoch02.aptinaija.domain.model.TestTemplate
import com.enoch02.aptinaija.domain.repository.TestRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TestRepositoryImpl @Inject constructor(
    private val templateDao: TestTemplateDao
) : TestRepository {

    override fun observeTemplates(): Flow<List<TestTemplate>> =
        templateDao.observeAll().map { rows ->
            rows.map { it.template.toDomain(it.sections) }
        }

    override suspend fun template(id: String): TestTemplate? =
        templateDao.byId(id)?.let { it.template.toDomain(it.sections) }
}
