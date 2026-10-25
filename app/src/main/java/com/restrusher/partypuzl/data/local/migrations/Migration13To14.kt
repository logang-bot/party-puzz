package com.restrusher.partypuzl.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restrusher.partypuzl.data.models.PackCategory

/**
 * Renames the stored `GENERAL_KNOWLEDGE` category to [PackCategory.QUIZ].
 *
 * `question_packs.category` holds the enum name, and Room throws on a name the enum no longer has,
 * so every pack row has to be rewritten before the table is first read.
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "UPDATE `question_packs` SET `category` = '${PackCategory.QUIZ.name}' " +
                    "WHERE `category` = 'GENERAL_KNOWLEDGE'"
        )
    }
}
