package com.restrusher.partypuzz.data.models

data class Player (
    val id: Int,
    val nickName: String,
    val gender: Gender,
    val age: Int
)

enum class Gender {
    Male,
    Female
}