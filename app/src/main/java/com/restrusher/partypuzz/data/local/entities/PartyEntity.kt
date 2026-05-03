package com.restrusher.partypuzz.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dateCreation: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val lastGameModeNameRes: Int? = null
)
