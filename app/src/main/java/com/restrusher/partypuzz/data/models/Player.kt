package com.restrusher.partypuzz.data.models

data class Player (
    val id: Int,
    val nickName: String,
    val gender: Gender,
    val age: Int
) {
    companion object {
        fun getEmptyPlayer(): Player {
            return Player(0, "", Gender.Male, 0)
        }
    }
}

enum class Gender {
    Male,
    Female,
    Unknown,
}