package com.restrusher.partypuzl.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.restrusher.partypuzl.data.local.entities.CustomPackEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel
import kotlinx.coroutines.flow.Flow

/**
 * A custom pack as every screen needs it: its authored metadata, both of its flags, and the entry
 * counts.
 *
 * The two flags answer different questions and are never interchangeable — [isAvailable] is
 * "offer this pack on the setup screen", set in the manager; [isEnabled] is "play it this
 * session", set on the setup screen itself.
 *
 * The per-type counts are here rather than derived later because the manager screen needs them to
 * warn about a pack that holds only truths or only dares — a Truth-or-Dare deck needs both halves
 * (see `EnabledPackContent.availableCategories`).
 */
data class CustomPackSummary(
    val packId: String,
    val name: String,
    val description: String,
    val category: PackCategory,
    val spice: SpiceLevel,
    val createdAt: Long,
    val isAvailable: Boolean,
    val isEnabled: Boolean,
    val entryCount: Int,
    val truthCount: Int,
    val dareCount: Int
)

private const val SUMMARY_QUERY = """
    SELECT
        c.packId AS packId,
        c.name AS name,
        c.description AS description,
        c.category AS category,
        c.spice AS spice,
        c.createdAt AS createdAt,
        c.isAvailable AS isAvailable,
        p.isEnabled AS isEnabled,
        (SELECT COUNT(*) FROM custom_entries e
            WHERE e.packId = c.packId AND e.isEnabled = 1) AS entryCount,
        (SELECT COUNT(*) FROM custom_entries e
            WHERE e.packId = c.packId AND e.isEnabled = 1 AND e.type = 'TRUTH') AS truthCount,
        (SELECT COUNT(*) FROM custom_entries e
            WHERE e.packId = c.packId AND e.isEnabled = 1 AND e.type = 'DARE') AS dareCount
    FROM custom_packs c
    INNER JOIN question_packs p ON p.id = c.packId
"""

@Dao
interface CustomPackDao {

    /** Newest first — a pack the user just wrote should be at the top of the manager. */
    @Query("$SUMMARY_QUERY ORDER BY c.createdAt DESC")
    fun getSummaries(): Flow<List<CustomPackSummary>>

    @Query("$SUMMARY_QUERY WHERE c.packId = :packId")
    fun getSummary(packId: String): Flow<CustomPackSummary?>

    @Query("$SUMMARY_QUERY WHERE c.packId = :packId")
    suspend fun getSummaryOnce(packId: String): CustomPackSummary?

    @Upsert
    suspend fun upsert(pack: CustomPackEntity)

    /**
     * Creates both halves of a pack at once. The `question_packs` row has to exist first or the
     * foreign key rejects the `custom_packs` row, so the two writes cannot be split.
     */
    @Transaction
    suspend fun insertPack(packRow: QuestionPackEntity, customRow: CustomPackEntity) {
        insertPackRow(packRow)
        upsert(customRow)
    }

    @Upsert
    suspend fun insertPackRow(packRow: QuestionPackEntity)

    /** Deleting the pack row cascades to `custom_packs`, `custom_entries` and `questions`. */
    @Query("DELETE FROM question_packs WHERE id = :packId")
    suspend fun deletePackRow(packId: String)

    @Query("UPDATE question_packs SET isEnabled = :enabled WHERE id = :packId")
    suspend fun setEnabled(packId: String, enabled: Boolean)

    @Query("UPDATE custom_packs SET isAvailable = :available WHERE packId = :packId")
    suspend fun setAvailable(packId: String, available: Boolean)

    /**
     * The manager's switch. Withdrawing a pack also drops it from the current session, since a
     * pack that is not offered must not still be dealing; putting it back switches it on, which is
     * how a freshly written pack arrives too.
     *
     * Both writes together, so the two flags can never be observed disagreeing.
     */
    @Transaction
    suspend fun setAvailability(packId: String, available: Boolean) {
        setAvailable(packId, available)
        setEnabled(packId, available)
    }
}
