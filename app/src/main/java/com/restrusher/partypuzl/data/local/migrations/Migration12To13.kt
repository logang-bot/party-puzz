package com.restrusher.partypuzl.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restrusher.partypuzl.data.local.entities.GameMode

/**
 * Replaces `parties.lastGameModeNameRes` with `lastGameMode`.
 *
 * The old column held an `R.string` id, which goes stale as soon as a release adds or removes a
 * string, and a stale id crashed every screen that rendered the party. The new column holds a
 * [GameMode] name. Ids that still match this build's mode strings are carried over; anything else
 * becomes NULL, which reads as "no mode" instead of a crash.
 *
 * The table is recreated rather than altered because `DROP COLUMN` needs SQLite 3.35, which is
 * not there on this app's `minSdk`. Room runs migrations with foreign keys off, so dropping the
 * old table leaves `party_photos` and `party_player_cross_ref` untouched.
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createNewPartiesTable(db)
        copyPartiesWithModeKeys(db)
        db.execSQL("DROP TABLE `parties`")
        db.execSQL("ALTER TABLE `parties_new` RENAME TO `parties`")
    }
}

private fun createNewPartiesTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS `parties_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`dateCreation` INTEGER NOT NULL, " +
                "`lastUsedAt` INTEGER, " +
                "`lastGameMode` TEXT)"
    )
}

private fun copyPartiesWithModeKeys(db: SupportSQLiteDatabase) {
    db.execSQL(
        "INSERT INTO `parties_new` (`id`, `name`, `dateCreation`, `lastUsedAt`, `lastGameMode`) " +
                "SELECT `id`, `name`, `dateCreation`, `lastUsedAt`, ${modeKeyCase()} FROM `parties`"
    )
}

private fun modeKeyCase(): String =
    GameMode.entries.joinToString(
        separator = " ",
        prefix = "CASE `lastGameModeNameRes` ",
        postfix = " ELSE NULL END"
    ) { "WHEN ${it.nameRes} THEN '${it.name}'" }
