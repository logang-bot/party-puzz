package com.restrusher.partypuzl.screenshots

import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.data.local.entities.GameMode
import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import com.restrusher.partypuzl.data.models.CustomEntryDraft
import com.restrusher.partypuzl.data.models.CustomPackDraft
import com.restrusher.partypuzl.data.models.PackTopic
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PlayerRepository
import javax.inject.Inject

data class SeededDemo(val partyId: Int, val packId: String, val players: List<Player>)

class ScreenshotSeeder @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val partyRepository: PartyRepository,
    private val customPackRepository: CustomPackRepository,
    private val preferences: UserPreferencesRepository
) {
    suspend fun seed(content: DemoContent): SeededDemo {
        preferences.setPartyPhotosEnabled(false)
        val partyId = partyRepository.createParty(content.partyName).toInt()
        val players = content.players.map { createPartyMember(it, partyId) }
        partyRepository.updateLastUsed(partyId, GameMode.STANDARD)
        val packId = createPack(content.pack)
        startSession(partyId, players)
        return SeededDemo(partyId, packId, players)
    }

    private suspend fun createPartyMember(demo: DemoPlayer, partyId: Int): Player {
        val entity = PlayerEntity(
            nickName = demo.nickName,
            gender = demo.gender,
            interestedIn = demo.interestedIn,
            avatarName = demo.avatarName
        )
        val id = playerRepository.createPlayer(entity).toInt()
        partyRepository.linkPlayerToParty(partyId, id)
        return Player(id, demo.nickName, demo.gender, demo.interestedIn, avatarName = demo.avatarName)
    }

    private suspend fun createPack(pack: DemoPack): String {
        val draft = CustomPackDraft(
            name = pack.name,
            description = pack.description,
            topic = PackTopic.TRAVEL,
            spice = SpiceLevel.MILD
        )
        val packId = customPackRepository.savePack(draft)
        pack.entries.forEach { customPackRepository.saveEntry(it.toDraft(packId)) }
        return packId
    }

    private fun DemoEntry.toDraft(packId: String) = CustomEntryDraft(
        packId = packId,
        type = type,
        text = text,
        optionA = optionA,
        optionB = optionB,
        correctOption = optionA?.let { 'A' }
    )

    private fun startSession(partyId: Int, players: List<Player>) {
        GamePlayersList.resetPlayersList()
        players.forEach(GamePlayersList::addPlayer)
        GamePlayersList.currentPartyId = partyId
        GameOptionsSource.currentGameModeNameRes = R.string.standard_game_mode
    }
}
