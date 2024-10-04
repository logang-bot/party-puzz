package com.restrusher.partypuzz.data.models

data class Player (
    val id: Int,
    val nickName: String,
    val gender: Gender
) {
    companion object {
        fun getEmptyPlayer(): Player {
            return Player(0, "", Gender.Male)
        }
    }
}

enum class Gender {
    Male,
    Female,
    Unknown,
}