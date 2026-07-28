package com.restrusher.partypuzl.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.restrusher.partypuzl.data.local.entities.QuestionEntity

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions")
    suspend fun getAll(): List<QuestionEntity>

    /** Questions belonging to packs the user has switched on, and not individually muted. */
    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN question_packs p ON p.id = q.packId
        WHERE p.isEnabled = 1 AND q.isEnabled = 1
        """
    )
    suspend fun getPlayableQuestions(): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE packId = :packId AND isEnabled = 1")
    suspend fun countForPack(packId: String): Int

    @Query("SELECT packId, COUNT(*) as count FROM questions WHERE isEnabled = 1 GROUP BY packId")
    suspend fun countsByPack(): List<PackQuestionCount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("UPDATE questions SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    /**
     * Rebuilds the whole mapping. Called when `QuestionCatalog.MAPPING_VERSION` changes, which
     * is how an edit to a source array repairs any index drift.
     */
    @Transaction
    suspend fun replaceAll(questions: List<QuestionEntity>) {
        deleteAll()
        insertAll(questions)
    }
}

data class PackQuestionCount(
    val packId: String,
    val count: Int
)
