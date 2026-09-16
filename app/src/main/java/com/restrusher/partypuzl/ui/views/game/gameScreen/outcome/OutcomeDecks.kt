package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarEvent
import com.restrusher.partypuzl.ui.views.game.gameScreen.BarModeState
import com.restrusher.partypuzl.ui.views.game.gameScreen.CouplesEvent

/** Resolved for the roster, so the target-picker's candidates exclude the player taking the turn. */
internal fun barRewardDeck(players: List<Player>, currentPlayer: Player?): List<BarEvent> =
    barRewardFlavours.map { BarEvent.FlavourReward(it) } +
            BarModeState.giveDrinksPickTargetEvent(players, currentPlayer)

internal fun barPunishmentDeck(): List<BarEvent> =
    barPunishmentFlavours.map { BarEvent.FlavourPunishment(it) } + BarModeState.takeDrinksEvent()

internal fun couplesRewardDeck(): List<CouplesEvent> =
    couplesRewardFlavours.map { CouplesEvent.FlavourReward(it) } +
            listOf(CouplesEvent.GiveAKiss, CouplesEvent.ChooseKissers, CouplesEvent.ChooseLovers)

internal fun couplesPunishmentDeck(
    players: List<Player>,
    currentPlayer: Player?
): List<CouplesEvent> {
    val target = interestedTarget(players, currentPlayer)
    return couplesPunishmentFlavours.map { CouplesEvent.FlavourPunishment(it) } + listOf(
        CouplesEvent.MakeALoveDeclaration(targetPlayerName = target),
        CouplesEvent.ActOfLove(requesterPlayerName = target)
    )
}

/**
 * The deck the reel will show, guaranteed to contain [fired] exactly once.
 *
 * A mini-game rolls its own outcome, so the generic entry of the same kind is substituted rather
 * than kept alongside it — otherwise the reel would offer two different "Take N drinks!" lines.
 */
internal fun <T : Any> deckLandingOn(deck: List<T>, fired: T): List<T> {
    if (fired in deck) return deck
    val sameKind = deck.indexOfFirst { it::class == fired::class }
    if (sameKind < 0) return deck + fired
    return deck.toMutableList().also { it[sameKind] = fired }
}

private fun interestedTarget(players: List<Player>, currentPlayer: Player?): String {
    val others = players.filter { it.id != currentPlayer?.id }
    val matches = others.filter { currentPlayer == null || it matchesTasteOf currentPlayer }
    return matches.ifEmpty { others }.randomOrNull()?.nickName.orEmpty()
}

private infix fun Player.matchesTasteOf(chooser: Player): Boolean =
    gender == Gender.Unknown || when (chooser.interestedIn) {
        InterestedIn.Man -> gender == Gender.Male
        InterestedIn.Woman -> gender == Gender.Female
        InterestedIn.Both -> true
    }
