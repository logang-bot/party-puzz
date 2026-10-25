package com.restrusher.partypuzl.testutil

import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player

object TestPlayers {
    val alice = Player(1, "Alice", Gender.Female, InterestedIn.Man)
    val bruno = Player(2, "Bruno", Gender.Male, InterestedIn.Woman)
    val cleo = Player(3, "Cleo", Gender.Female, InterestedIn.Both)
    val unknown = Player(4, "Sam", Gender.Unknown, InterestedIn.Both)

    val roster = listOf(alice, bruno, cleo)
}
