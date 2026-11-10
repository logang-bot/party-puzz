package com.restrusher.partypuzl.ui.views.gameConfig

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.data.local.appData.appDataSource.SessionUnlocksSource
import com.restrusher.partypuzl.data.local.entities.GameMode
import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.packBank.PackBankRepository
import com.restrusher.partypuzl.data.packBank.currentPackLanguage
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzl.data.packs.QuestionPackSeeder
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import com.restrusher.partypuzl.navigation.GameConfigScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GameConfigViewModel @Inject constructor(
    private val partyRepository: PartyRepository,
    private val playerRepository: PlayerRepository,
    private val questionPackRepository: QuestionPackRepository,
    private val questionRepository: QuestionRepository,
    private val customPackRepository: CustomPackRepository,
    private val questionPackSeeder: QuestionPackSeeder,
    private val packBankRepository: PackBankRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<GameConfigScreen>()

    /**
     * Prompt count per built-in free pack, read once after seeding. These only change when the
     * catalog's mapping version does, so there is nothing to observe. Premium counts come from the
     * pack bank instead, which can change while the screen is open.
     */
    private var promptCounts: Map<String, Int> = emptyMap()

    private val _uiState = MutableStateFlow(GameConfigState())
    val uiState: StateFlow<GameConfigState> = _uiState.asStateFlow()

    init {
        GamePlayersList.resetPlayersList()
        loadParty()
        observePacks()
        observePackBankRefresh()
    }

    /** Tapping a premium pack that never downloaded retries the download instead of toggling it. */
    fun onTogglePack(packId: String) {
        val state = uiState.value
        val pack = (state.officialPacks + state.premiumPacks + state.customPacks)
            .firstOrNull { it.id == packId } ?: return
        if (!pack.isUnlocked) return
        if (pack.needsDownload) return retryPackDownload()
        viewModelScope.launch { questionPackRepository.setEnabled(packId, !pack.isEnabled) }
    }

    private fun loadParty() {
        val partyId = args.partyId ?: return
        GamePlayersList.currentPartyId = partyId
        viewModelScope.launch {
            partyRepository.getPartyById(partyId)?.players?.forEach { entity ->
                GamePlayersList.addPlayer(
                    Player(
                        id = entity.id,
                        nickName = entity.nickName,
                        gender = entity.gender,
                        interestedIn = entity.interestedIn,
                        photoPath = entity.photoPath,
                        avatarName = entity.avatarName
                    )
                )
            }
        }
    }

    /**
     * Packs come from three places at once: the catalog (names, icons, prompt counts), Room
     * (enabled + permanently unlocked) and [SessionUnlocksSource] (rewarded-ad unlocks). Folding
     * the premium flag in here too means buying premium lights up every premium pack without the
     * screen having to know why.
     */
    private fun observePacks() {
        viewModelScope.launch {
            questionPackSeeder.seedIfNeeded()
            promptCounts = questionRepository.countsByPack()
            packBankRepository.loadCache()
            combine(
                questionPackRepository.getPacks(),
                SessionUnlocksSource.unlockedPackIds,
                userPreferencesRepository.isAdFree,
                customPackRepository.getSummaries(),
                packBankRepository.content
            ) { packs, sessionUnlocks, isAdFree, customSummaries, packBank ->
                val counts = promptCounts + packBank.countsFor(currentPackLanguage())
                PackModels(
                    catalog = packs.toCatalogUiModels(PremiumAccess(sessionUnlocks, isAdFree), counts),
                    // A pack the user has withdrawn in the manager is not offered here at all.
                    // The catalog plays that role for built-in packs; authored packs have no
                    // catalog, so `isAvailable` is what decides they exist for this screen.
                    custom = customSummaries.filter { it.isAvailable }.map { it.toUiModel() },
                    premiumRowsToSync = isAdFree.takeIf { packs.premiumRowsDisagreeWith(it) }
                )
            }.collect { models ->
                models.premiumRowsToSync?.let(::mirrorPremiumOntoRows)
                _uiState.update { state ->
                    state.copy(
                        arePacksLoaded = true,
                        officialPacks = models.catalog.filter { it.tier == PackTier.OFFICIAL },
                        premiumPacks = models.catalog.filter { it.tier == PackTier.PREMIUM },
                        customPacks = models.custom
                    )
                }
            }
        }
    }

    private fun observePackBankRefresh() {
        viewModelScope.launch {
            packBankRepository.isRefreshing.collect { refreshing ->
                _uiState.update { it.copy(isPackBankRefreshing = refreshing) }
            }
        }
    }

    private fun retryPackDownload() {
        viewModelScope.launch { packBankRepository.refresh(force = true) }
    }

    /**
     * Bundles one emission of the five-flow combine, which has outgrown a Triple.
     * [premiumRowsToSync] is the ownership the premium rows must be rewritten to, or null when they
     * already agree — guarding on that keeps the write (which re-emits) from looping.
     */
    private data class PackModels(
        val catalog: List<PackUiModel>,
        val custom: List<PackUiModel>,
        val premiumRowsToSync: Boolean?
    )

    private fun List<QuestionPackEntity>.premiumRowsDisagreeWith(isAdFree: Boolean): Boolean =
        any { it.tier == PackTier.PREMIUM && it.isUnlocked != isAdFree }

    fun onUnlockRequested(pack: PackUiModel) {
        _uiState.update { it.copy(unlockTarget = pack) }
    }

    fun onUnlockDismissed() {
        _uiState.update { it.copy(unlockTarget = null) }
    }

    /**
     * Rewarded-ad path. The unlock lasts for this process only, so it goes to
     * [SessionUnlocksSource] rather than Room. The pack switches on immediately — the user
     * watched an ad for it; making them tap again would be rude.
     */
    fun onRewardEarned(packId: String) {
        SessionUnlocksSource.unlock(packId)
        viewModelScope.launch {
            questionPackRepository.setEnabled(packId, true)
            _uiState.update { it.copy(unlockTarget = null) }
        }
    }

    /** Relocking after a refund also switches the packs off, so they stop being dealt. */
    private fun mirrorPremiumOntoRows(isAdFree: Boolean) {
        viewModelScope.launch { questionPackRepository.setPremiumUnlocked(isAdFree) }
    }

    fun onStartGame(onReady: () -> Unit) {
        viewModelScope.launch {
            val partyId = args.partyId ?: GamePlayersList.currentPartyId
            val gameMode = GameMode.fromNameRes(args.gameModeName)
            if (partyId != null && gameMode != null) partyRepository.updateLastUsed(partyId, gameMode)
            onReady()
        }
    }

    fun deletePlayer(player: Player) {
        GamePlayersList.removePlayer(player.id)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) {
                playerRepository.deletePlayer(
                    PlayerEntity(
                        id = player.id,
                        nickName = player.nickName,
                        gender = player.gender,
                        interestedIn = player.interestedIn,
                        photoPath = player.photoPath,
                        avatarName = player.avatarName
                    )
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
