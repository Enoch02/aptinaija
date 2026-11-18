package com.enoch02.aptinaija.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [Index("company"), Index("section"), Index("difficulty")]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "test_templates")
data class TestTemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val provider: String,
    val sourceUrl: String?,
    val shuffle: Boolean,
    val feedbackDuring: Boolean,
    val autoSubmit: Boolean,
    val pause: Boolean
)

@Entity(
    tableName = "template_sections",
    primaryKeys = ["templateId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = TestTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class TemplateSectionEntity(
    val templateId: String,
    val position: Int,
    val section: String,
    val questionCount: Int,
    val minutes: Int
)

@Entity(
    tableName = "attempts",
    indices = [Index("status"), Index("startedAt")]
)
data class AttemptEntity(
    @PrimaryKey val id: String,
    val templateId: String?,
    val mode: String,
    val company: String?,
    val section: String?,
    val startedAt: Long,
    val completedAt: Long?,
    val deadlineAt: Long?,
    val totalQuestions: Int,
    val correctCount: Int,
    val status: String
)

@Entity(
    tableName = "attempt_questions",
    primaryKeys = ["attemptId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = AttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attemptId"), Index("questionId")]
)
data class AttemptQuestionEntity(
    val attemptId: String,
    val position: Int,
    val questionId: String,
    val section: String,
    val optionOrderJson: String,
    val selectedOption: Int?,
    val isCorrect: Boolean?,
    val answeredAt: Long?
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val questionId: String,
    val createdAt: Long
)

@Entity(tableName = "wrong_questions")
data class WrongQuestionEntity(
    @PrimaryKey val questionId: String,
    val lastWrongAt: Long,
    val resolvedAt: Long? = null
)

@Entity(tableName = "pack_metadata")
data class PackMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val schemaVersion: Int,
    val questionCount: Int,
    val templateCount: Int,
    val importedAt: Long
)
