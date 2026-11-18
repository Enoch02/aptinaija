package com.enoch02.aptinaija.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enoch02.aptinaija.data.local.dao.AttemptDao
import com.enoch02.aptinaija.data.local.dao.BookmarkDao
import com.enoch02.aptinaija.data.local.dao.PackMetadataDao
import com.enoch02.aptinaija.data.local.dao.QuestionDao
import com.enoch02.aptinaija.data.local.dao.TestTemplateDao
import com.enoch02.aptinaija.data.local.dao.WrongQuestionDao
import com.enoch02.aptinaija.data.local.entity.AttemptEntity
import com.enoch02.aptinaija.data.local.entity.AttemptQuestionEntity
import com.enoch02.aptinaija.data.local.entity.BookmarkEntity
import com.enoch02.aptinaija.data.local.entity.PackMetadataEntity
import com.enoch02.aptinaija.data.local.entity.QuestionEntity
import com.enoch02.aptinaija.data.local.entity.TemplateSectionEntity
import com.enoch02.aptinaija.data.local.entity.TestTemplateEntity
import com.enoch02.aptinaija.data.local.entity.WrongQuestionEntity

@Database(
    entities = [
        QuestionEntity::class,
        TestTemplateEntity::class,
        TemplateSectionEntity::class,
        AttemptEntity::class,
        AttemptQuestionEntity::class,
        BookmarkEntity::class,
        WrongQuestionEntity::class,
        PackMetadataEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AptiNaijaDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun testTemplateDao(): TestTemplateDao
    abstract fun attemptDao(): AttemptDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun wrongQuestionDao(): WrongQuestionDao
    abstract fun packMetadataDao(): PackMetadataDao
}
