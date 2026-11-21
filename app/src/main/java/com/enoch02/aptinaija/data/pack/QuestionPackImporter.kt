package com.enoch02.aptinaija.data.pack

import android.content.Context
import androidx.room.withTransaction
import com.enoch02.aptinaija.core.common.IoDispatcher
import com.enoch02.aptinaija.data.local.AptiNaijaDatabase
import com.enoch02.aptinaija.data.local.dao.PackMetadataDao
import com.enoch02.aptinaija.data.local.entity.PackMetadataEntity
import com.enoch02.aptinaija.data.local.entity.TemplateSectionEntity
import com.enoch02.aptinaija.data.local.entity.TestTemplateEntity
import com.enoch02.aptinaija.data.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Loads the versioned JSON packs bundled in assets into Room. Imports are idempotent;
 * a new schema version or a different question count triggers a full replace.
 */
@Singleton
class QuestionPackImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AptiNaijaDatabase,
    private val metadataDao: PackMetadataDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val mutex = Mutex()

    @Volatile
    private var importedThisProcess = false

    suspend fun importIfNeeded() {
        if (importedThisProcess) return
        mutex.withLock {
            if (importedThisProcess) return
            val questionPack = readPack(QUESTION_PACK_ASSET, QuestionPackDto.serializer())
            val templatePack = readPack(TEMPLATE_PACK_ASSET, TemplatesPackDto.serializer())
            require(questionPack.schemaVersion == 2 && templatePack.schemaVersion == 2) {
                "Question and template packs must use schema_version 2"
            }
            val existing = metadataDao.get()

            val upToDate = existing != null &&
                existing.schemaVersion == questionPack.schemaVersion &&
                existing.questionCount == questionPack.questions.size &&
                existing.templateCount == templatePack.templates.size
            if (upToDate) {
                importedThisProcess = true
                return
            }

            importLocked(questionPack, templatePack)
            importedThisProcess = true
        }
    }

    private suspend fun <T> readPack(
        asset: String,
        serializer: kotlinx.serialization.KSerializer<T>
    ): T = withContext(ioDispatcher) {
        val raw = context.assets.open(asset).bufferedReader().use { it.readText() }
        AppJson.decodeFromString(serializer, raw)
    }

    private suspend fun importLocked(
        questionPack: QuestionPackDto,
        templatePack: TemplatesPackDto
    ) {
        val questions = questionPack.questions
            .filter { it.status != "rejected" }
            .filter { it.options.size >= 2 && it.answer in it.options.indices }
            .map { it.toEntity() }

        val defaults = templatePack.defaults
        val templates = templatePack.templates.map { (id, dto) ->
            TestTemplateEntity(
                id = id,
                companyId = dto.companyId,
                title = dto.title,
                company = dto.company,
                provider = dto.provider,
                sourceUrl = dto.source,
                shuffle = defaults.shuffle,
                feedbackDuring = defaults.feedbackDuring,
                autoSubmit = defaults.autoSubmit,
                pause = defaults.pause
            )
        }
        val sections = templatePack.templates.flatMap { (id, dto) ->
            dto.sections.mapIndexed { index, section ->
                TemplateSectionEntity(
                    templateId = id,
                    position = index,
                    section = section.section,
                    questionCount = section.count,
                    minutes = section.minutes
                )
            }
        }

        database.withTransaction {
            database.questionDao().clear()
            database.testTemplateDao().clear()
            database.questionDao().insertAll(questions)
            database.testTemplateDao().insertTemplates(templates)
            database.testTemplateDao().insertSections(sections)
            database.packMetadataDao().upsert(
                PackMetadataEntity(
                    schemaVersion = questionPack.schemaVersion,
                    questionCount = questions.size,
                    templateCount = templates.size,
                    importedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private companion object {
        const val QUESTION_PACK_ASSET = "question_pack.json"
        const val TEMPLATE_PACK_ASSET = "test_templates.json"
    }
}
