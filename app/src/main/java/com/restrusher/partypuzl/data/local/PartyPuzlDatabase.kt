package com.restrusher.partypuzl.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.restrusher.partypuzl.data.local.dao.PartyDao
import com.restrusher.partypuzl.data.local.dao.PartyPhotoDao
import com.restrusher.partypuzl.data.local.dao.PlayerDao
import com.restrusher.partypuzl.data.local.entities.PartyEntity
import com.restrusher.partypuzl.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzl.data.local.entities.PartyPlayerCrossRef
import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [PlayerEntity::class, PartyEntity::class, PartyPlayerCrossRef::class, PartyPhotoEntity::class],
    version = 7,
    exportSchema = false
)
abstract class PartyPuzlDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun partyDao(): PartyDao
    abstract fun partyPhotoDao(): PartyPhotoDao

    companion object {
        @Volatile
        private var Instance: PartyPuzlDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context): PartyPuzlDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PartyPuzlDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
