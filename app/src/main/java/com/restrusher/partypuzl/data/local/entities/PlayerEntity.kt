package com.restrusher.partypuzl.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn

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
