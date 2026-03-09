package com.restrusher.partypuzz.data.local.entities

import androidx.room.Entity

@Entity(tableName = "party_player_cross_ref", primaryKeys = ["partyId", "playerId"])
data class PartyPlayerCrossRef(val partyId: Int, val playerId: Int)
