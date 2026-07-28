package com.restrusher.partypuzl.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restrusher.partypuzl.data.local.dao.PartyDao
import com.restrusher.partypuzl.data.local.dao.PartyPhotoDao
import com.restrusher.partypuzl.data.local.dao.PlayerDao
import com.restrusher.partypuzl.data.local.dao.QuestionDao
import com.restrusher.partypuzl.data.local.dao.QuestionPackDao
import com.restrusher.partypuzl.data.local.entities.PartyEntity
import com.restrusher.partypuzl.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzl.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import com.restrusher.partypuzl.data.local.entities.QuestionEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [
        PlayerEntity::class,
        PartyEntity::class,
        PartyPlayerCrossRef::class,
        PartyPhotoEntity::class,
        QuestionPackEntity::class,
        QuestionEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class PartyPuzlDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun partyDao(): PartyDao
    abstract fun partyPhotoDao(): PartyPhotoDao
    abstract fun questionPackDao(): QuestionPackDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var Instance: PartyPuzlDatabase? = null

        /**
         * Adds the `question_packs` table without touching anything else.
         *
         * The builder still falls back to a destructive migration for any path this doesn't
         * cover, so without this an update would drop every saved party and photo just to gain
         * a new table. The statement is copied verbatim from Room's exported schema for v8 —
         * it has to match exactly or Room throws on open.
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `question_packs` (" +
                            "`id` TEXT NOT NULL, " +
                            "`tier` TEXT NOT NULL, " +
                            "`category` TEXT NOT NULL, " +
                            "`isEnabled` INTEGER NOT NULL, " +
                            "`isUnlocked` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )
            }
        }

        /**
         * Adds the `questions` table. Rows are rebuilt from the catalog by `QuestionPackSeeder`
         * on next launch, so the migration only has to create the shape.
         */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `questions` (" +
                            "`id` TEXT NOT NULL, " +
                            "`packId` TEXT NOT NULL, " +
                            "`source` TEXT NOT NULL, " +
                            "`sourceIndex` INTEGER NOT NULL, " +
                            "`isEnabled` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`), " +
                            "FOREIGN KEY(`packId`) REFERENCES `question_packs`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_questions_packId` " +
                            "ON `questions` (`packId`)"
                )
            }
        }

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context): PartyPuzlDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PartyPuzlDatabase::class.java, "app_database")
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
