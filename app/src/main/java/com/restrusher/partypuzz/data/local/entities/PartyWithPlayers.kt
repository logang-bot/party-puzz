package com.restrusher.partypuzz.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PartyWithPlayers(
    @Embedded val party: PartyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PartyPlayerCrossRef::class,
            parentColumn = "partyId",
            entityColumn = "playerId"
        )
    )
    val players: List<PlayerEntity>,
    @Relation(parentColumn = "id", entityColumn = "partyId")
    val photos: List<PartyPhotoEntity>
)
