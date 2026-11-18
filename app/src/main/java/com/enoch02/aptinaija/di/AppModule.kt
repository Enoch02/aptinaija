package com.enoch02.aptinaija.di

import android.content.Context
import androidx.room.Room
import com.enoch02.aptinaija.core.common.IoDispatcher
import com.enoch02.aptinaija.data.local.AptiNaijaDatabase
import com.enoch02.aptinaija.data.local.dao.AttemptDao
import com.enoch02.aptinaija.data.local.dao.BookmarkDao
import com.enoch02.aptinaija.data.local.dao.PackMetadataDao
import com.enoch02.aptinaija.data.local.dao.QuestionDao
import com.enoch02.aptinaija.data.local.dao.TestTemplateDao
import com.enoch02.aptinaija.data.local.dao.WrongQuestionDao
import com.enoch02.aptinaija.data.repository.AttemptRepositoryImpl
import com.enoch02.aptinaija.data.repository.BookmarkRepositoryImpl
import com.enoch02.aptinaija.data.repository.QuestionRepositoryImpl
import com.enoch02.aptinaija.data.repository.SettingsRepositoryImpl
import com.enoch02.aptinaija.data.repository.TestRepositoryImpl
import com.enoch02.aptinaija.domain.repository.AttemptRepository
import com.enoch02.aptinaija.domain.repository.BookmarkRepository
import com.enoch02.aptinaija.domain.repository.QuestionRepository
import com.enoch02.aptinaija.domain.repository.SettingsRepository
import com.enoch02.aptinaija.domain.repository.TestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AptiNaijaDatabase =
        Room.databaseBuilder(context, AptiNaijaDatabase::class.java, "aptinaija.db").build()

    @Provides
    fun questionDao(database: AptiNaijaDatabase): QuestionDao = database.questionDao()

    @Provides
    fun testTemplateDao(database: AptiNaijaDatabase): TestTemplateDao = database.testTemplateDao()

    @Provides
    fun attemptDao(database: AptiNaijaDatabase): AttemptDao = database.attemptDao()

    @Provides
    fun bookmarkDao(database: AptiNaijaDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun wrongQuestionDao(database: AptiNaijaDatabase): WrongQuestionDao = database.wrongQuestionDao()

    @Provides
    fun packMetadataDao(database: AptiNaijaDatabase): PackMetadataDao = database.packMetadataDao()

    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun questionRepository(impl: QuestionRepositoryImpl): QuestionRepository

    @Binds
    @Singleton
    abstract fun testRepository(impl: TestRepositoryImpl): TestRepository

    @Binds
    @Singleton
    abstract fun attemptRepository(impl: AttemptRepositoryImpl): AttemptRepository

    @Binds
    @Singleton
    abstract fun bookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun settingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
