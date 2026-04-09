package com.restrusher.partypuzz.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nickName: String,
    val gender: Gender,
    val interestedIn: InterestedIn,
    val photoPath: String? = null,
    val avatarName: String? = null
)
