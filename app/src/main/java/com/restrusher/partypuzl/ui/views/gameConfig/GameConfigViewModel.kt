package com.restrusher.partypuzl.ui.views.gameConfig

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.billing.BillingManager
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.appData.appDataSource.SessionUnlocksSource
import com.restrusher.partypuzl.data.local.appData.appModels.QuestionPackDefinition
import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PartyRepository
import com.restrusher.partypuzl.data.repositories.interfaces.PlayerRepository
import com.restrusher.partypuzl.data.packs.QuestionPackSeeder
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionRepository
import com.restrusher.partypuzl.navigation.GameConfigScreen
import com.restrusher.partypuzl.ui.views.game.gameScreen.MiniGame
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
    private val questionPackSeeder: QuestionPackSeeder,
    private val billingManager: BillingManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<GameConfigScreen>()

    /**
     * Prompt count per pack, read once after seeding. These only change when the catalog's
     * mapping version does, so there is nothing to observe.
     */
    private var promptCounts: Map<String, Int> = emptyMap()

    private val _uiState = MutableStateFlow(GameConfigState())
    val uiState: StateFlow<GameConfigState> = _uiState.asStateFlow()

    init {
        GamePlayersList.resetPlayersList()
        loadParty()
        observePacks()
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
     * the purchase flag in here too means buying "remove ads" lights up every premium pack
     * without the screen having to know why.
     */
    private fun observePacks() {
        viewModelScope.launch {
            questionPackSeeder.seedIfNeeded()
            promptCounts = questionRepository.countsByPack()
            combine(
                questionPackRepository.getPacks(),
                SessionUnlocksSource.unlockedPackIds,
                userPreferencesRepository.isAdFree
            ) { packs, sessionUnlocks, isAdFree ->
                // Purchased but the rows don't say so yet — mirror it once so the unlock holds
                // independently of the flag. Guarded on "still locked" so writing the rows (which
                // re-emits this flow) doesn't schedule another write.
                val needsPurchaseSync =
                    isAdFree && packs.any { it.tier == PackTier.PREMIUM && !it.isUnlocked }
                packs.toUiModels(sessionUnlocks, isAdFree) to needsPurchaseSync
            }.collect { (models, needsPurchaseSync) ->
                if (needsPurchaseSync) persistPurchaseUnlock()
                _uiState.update { state ->
                    state.copy(
                        officialPacks = models.filter { it.tier == PackTier.OFFICIAL },
                        premiumPacks = models.filter { it.tier == PackTier.PREMIUM }
                    )
                }
            }
        }
    }

    private fun List<QuestionPackEntity>.toUiModels(
        sessionUnlocks: Set<String>,
        isAdFree: Boolean
    ): List<PackUiModel> {
        val byId = associateBy { it.id }
        // Catalog order is the display order. A DB row with no catalog entry is a pack that was
        // removed in a later release, so it is simply not shown.
        return QuestionPackCatalog.all.mapNotNull { definition ->
            val entity = byId[definition.id] ?: return@mapNotNull null
            val sessionUnlocked = definition.id in sessionUnlocks
            val unlocked = definition.tier != PackTier.PREMIUM ||
                    entity.isUnlocked || isAdFree || sessionUnlocked
            PackUiModel(
                id = definition.id,
                tier = definition.tier,
                nameRes = definition.nameRes,
                iconRes = definition.iconRes,
                accent = definition.accent,
                promptCount = definition.promptCount(),
                // A locked pack can never read as enabled, whatever the stored flag says.
                isEnabled = entity.isEnabled && unlocked,
                isUnlocked = unlocked,
                isSessionUnlocked = sessionUnlocked && !entity.isUnlocked && !isAdFree
            )
        }
    }

    /**
     * Mini-games carry no question rows — they are code, not prompts — so that pack reports how
     * many mini-games ship instead of a misleading zero.
     */
    private fun QuestionPackDefinition.promptCount(): Int =
        if (category == PackCategory.MINI_GAME) MiniGame.entries.size
        else promptCounts[id] ?: 0

    fun onTogglePack(packId: String) {
        val pack = (uiState.value.officialPacks + uiState.value.premiumPacks)
            .firstOrNull { it.id == packId } ?: return
        if (!pack.isUnlocked) return
        viewModelScope.launch { questionPackRepository.setEnabled(packId, !pack.isEnabled) }
    }

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

    /**
     * Purchase path. `BillingManager` flips `isAdFree` once Play acknowledges the purchase, and
     * [observePacks] already treats that flag as "everything unlocked", so there is nothing to
     * do on success here — the rows light up on their own. `unlockAllPremium` persists it on the
     * pack rows as well so the unlock survives even if `isAdFree` is ever reset.
     */
    fun onPurchaseRequested(activity: Activity) {
        if (!billingManager.launchPurchaseFlow(activity)) {
            _uiState.update {
                it.copy(unlockTarget = null, messageRes = R.string.unlock_purchase_unavailable)
            }
            return
        }
        _uiState.update { it.copy(unlockTarget = null) }
    }

    private fun persistPurchaseUnlock() {
        viewModelScope.launch { questionPackRepository.unlockAllPremium() }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(messageRes = null) }
    }

    fun onStartGame(onReady: () -> Unit) {
        viewModelScope.launch {
            val partyId = args.partyId ?: GamePlayersList.currentPartyId
            if (partyId != null) partyRepository.updateLastUsed(partyId, args.gameModeName)
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
