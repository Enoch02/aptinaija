package com.enoch02.aptinaija.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.enoch02.aptinaija.data.local.entity.PackMetadataEntity
import com.enoch02.aptinaija.data.local.entity.TemplateSectionEntity
import com.enoch02.aptinaija.data.local.entity.TestTemplateEntity
import kotlinx.coroutines.flow.Flow

data class TestTemplateWithSections(
    @Embedded val template: TestTemplateEntity,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val sections: List<TemplateSectionEntity>
)

@Dao
interface TestTemplateDao {

    @Transaction
    @Query("SELECT * FROM test_templates ORDER BY title")
    fun observeAll(): Flow<List<TestTemplateWithSections>>

    @Transaction
    @Query("SELECT * FROM test_templates WHERE id = :id")
    suspend fun byId(id: String): TestTemplateWithSections?

    @Query("SELECT COUNT(*) FROM test_templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TestTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<TemplateSectionEntity>)

    @Query("DELETE FROM test_templates")
    suspend fun clear()
}

@Dao
interface PackMetadataDao {

    @Query("SELECT * FROM pack_metadata WHERE id = 1")
    suspend fun get(): PackMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: PackMetadataEntity)
}
