package com.restrusher.partypuzz.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.restrusher.partypuzz.data.models.Gender

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nickName: String,
    val gender: Gender
)
