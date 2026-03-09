package com.restrusher.partypuzz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.restrusher.partypuzz.data.local.dao.PartyDao
import com.restrusher.partypuzz.data.local.dao.PlayerDao
import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [PlayerEntity::class, PartyEntity::class, PartyPlayerCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class PartyPuzzDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun partyDao(): PartyDao

    companion object {
        @Volatile
        private var Instance: PartyPuzzDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context): PartyPuzzDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PartyPuzzDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
