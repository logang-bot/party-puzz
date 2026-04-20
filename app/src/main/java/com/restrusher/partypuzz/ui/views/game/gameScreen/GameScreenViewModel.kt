package com.restrusher.partypuzz.ui.views.game.gameScreen

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.data.repositories.interfaces.PartyPhotoRepository
import com.restrusher.partypuzz.ui.common.ads.InterstitialAdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val partyPhotoRepository: PartyPhotoRepository,
    private val interstitialAdManager: InterstitialAdManager
) : ViewModel() {

    companion object {
        private const val ANIMATION_DURATION_MS = 5000L
        private const val NAME_CYCLE_INTERVAL_MS = 300L
        private const val REVEAL_DURATION_MS = 1000L
        private const val STICKY_DARE_EXIT_DELAY_MS = 400L
        private const val CAMERA_TRIGGER_PROBABILITY = 0.33
    }

    private val currentPartyId: Int? = GamePlayersList.currentPartyId

    private val modeHandler: GameModeHandler = when (GameOptionsSource.currentGameModeNameRes) {
        R.string.bar_game_mode -> BarModeHandler()
        R.string.couples_game_mode -> CouplesModeHandler()
        R.string.party_puzz_game_mode -> PartyPuzzModeHandler()
        else -> NoOpModeHandler()
    }

    private val isPartyPuzzMode = GameOptionsSource.currentGameModeNameRes == R.string.party_puzz_game_mode

    private val _uiState = MutableStateFlow(
        GameScreenState(
            players = GamePlayersList.PlayersList.toList(),
            barMode = BarModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.bar_game_mode || isPartyPuzzMode),
            couplesMode = CouplesModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.couples_game_mode || isPartyPuzzMode)
        )
    )
    val uiState: StateFlow<GameScreenState> = _uiState.asStateFlow()

    private var dealJob: Job? = null
    private val stickyDareJobs = mutableMapOf<String, Job>()

    fun showInterstitial(activity: Activity, onDone: () -> Unit) {
        interstitialAdManager.showAd(activity, onDone)
    }

    fun onGameDealTapped() {
        val state = _uiState.value
        if (state.dealPhase != GameDealPhase.IDLE || state.players.isEmpty()) return

        val selectedPlayer = state.players.random()
        val availableDealTypes = GameDealType.entries.filter { type ->
            val playerCountOk = if (type == GameDealType.MINI_GAME) state.players.size >= 2 else true
            playerCountOk && isDealTypeEnabled(type)
        }
        val dealType = availableDealTypes.random()

        dealJob?.cancel()
        dealJob = viewModelScope.launch {
            // Phase 1: Cycle through player names for 5 seconds
            _uiState.update { it.copy(dealPhase = GameDealPhase.ANIMATING) }
            val players = _uiState.value.players
            val iterations = (ANIMATION_DURATION_MS / NAME_CYCLE_INTERVAL_MS).toInt()
            val namePool = players.map { it.nickName }.toMutableList()
            repeat(iterations) { i ->
                if (i % players.size == 0) namePool.shuffle()
                _uiState.update { it.copy(animatingName = namePool[i % players.size]) }
                delay(NAME_CYCLE_INTERVAL_MS)
            }

            // Phase 2: Reveal selected player's name
            _uiState.update {
                it.copy(
                    dealPhase = GameDealPhase.PLAYER_NAME_REVEAL,
                    selectedPlayer = selectedPlayer,
                    animatingName = ""
                )
            }
            delay(REVEAL_DURATION_MS)

            // Phase 3: Reveal selected player's photo
            _uiState.update { it.copy(dealPhase = GameDealPhase.PLAYER_PHOTO_REVEAL) }
            delay(REVEAL_DURATION_MS)

            // Phase 4: Show challenge — load content based on deal type
            val (challengeText, gkQuestion, pcText, durationLabel, durationSeconds, miniGame) =
                buildChallengeContent(dealType)

            _uiState.update {
                it.copy(
                    dealPhase = GameDealPhase.CHALLENGE_SHOWN,
                    dealType = dealType,
                    challengeText = challengeText,
                    generalKnowledgeQuestion = gkQuestion,
                    stickyDarePresentContinuous = pcText,
                    stickyDareDurationLabel = durationLabel,
                    stickyDareDurationSeconds = durationSeconds,
                    miniGame = miniGame,
                    pendingCameraRequest = currentPartyId != null &&
                            Random.nextDouble() < CAMERA_TRIGGER_PROBABILITY
                )
            }
        }
    }

    fun onTruthOrDareChosen(choice: TruthOrDareChoice) {
        if (_uiState.value.truthOrDareChoice != null) return
        val texts = when (choice) {
            TruthOrDareChoice.TRUTH -> context.resources.getStringArray(R.array.truth_texts)
            TruthOrDareChoice.DARE -> context.resources.getStringArray(R.array.dare_texts)
        }
        _uiState.update { it.copy(truthOrDareChoice = choice, challengeText = texts.random()) }
    }

    fun onGeneralKnowledgeAnswered(option: Char) {
        if (_uiState.value.selectedAnswerOption != null) return
        _uiState.update { it.copy(selectedAnswerOption = option) }
    }

    fun onMiniGameOpponentSelected(opponent: Player) {
        _uiState.update { it.copy(miniGameOpponent = opponent) }
    }

    fun onTruthOrDareSkipped() {
        if (_uiState.value.truthOrDareChoice == null) return
        // TODO: Wire up RewardedAdManager here — show a rewarded ad before applying the punishment.
        //       If the user watches the ad to completion (onRewarded fires), grant the skip for free.
        //       If the ad is dismissed early or unavailable, apply the punishment as usual.
        _uiState.update { modeHandler.applyPunishment(it, it.selectedPlayer) }
    }

    fun onStickyDareSkipped() {
        if (_uiState.value.dealType != GameDealType.STICKY_DARE) return
        // TODO: Wire up RewardedAdManager here — same pattern as onTruthOrDareSkipped above.
        _uiState.update { modeHandler.applyPunishment(it, it.selectedPlayer) }
    }

    fun onMiniGameDealFinished() {
        if (_uiState.value.miniGameResult == null) return
        _uiState.update { modeHandler.applyMiniGameResult(it) }
        if (!_uiState.value.hasActiveModeEvent) resetDeal()
    }

    fun onGiveDrinksTargetSelected(targetName: String) {
        val currentEvent = _uiState.value.barMode.activeEvent as? BarEvent.GiveDrinksPickTarget ?: return
        _uiState.update {
            it.copy(
                barMode = it.barMode.copy(
                    activeEvent = BarEvent.GiveDrinks(
                        amount = currentEvent.amount,
                        targetPlayerName = targetName
                    )
                )
            )
        }
    }

    fun onModeEventDismissed() {
        dealJob?.cancel()
        if (_uiState.value.pendingCameraRequest) {
            _uiState.update {
                modeHandler.clearEvent(it).copy(showCameraRequest = true, pendingCameraRequest = false)
            }
        } else {
            _uiState.update { resetDealState(modeHandler.clearEvent(it)) }
        }
    }

    fun onChallengeDismissed() {
        val state = _uiState.value
        if (!state.isChallengeDismissible) return

        if (state.dealType == GameDealType.GENERAL_KNOWLEDGE) {
            val isCorrect = state.generalKnowledgeQuestion != null &&
                    state.selectedAnswerOption == state.generalKnowledgeQuestion.correctOption
            _uiState.update {
                if (isCorrect) modeHandler.applyReward(it)
                else modeHandler.applyPunishment(it, it.selectedPlayer)
            }
            return
        }

        if (state.dealType == GameDealType.STICKY_DARE) {
            val dare = ActiveStickyDare(
                id = UUID.randomUUID().toString(),
                playerName = state.selectedPlayer?.nickName.orEmpty(),
                presentContinuousText = state.stickyDarePresentContinuous.orEmpty(),
                durationLabel = state.stickyDareDurationLabel.orEmpty(),
                totalSeconds = state.stickyDareDurationSeconds ?: 60,
                remainingSeconds = state.stickyDareDurationSeconds ?: 60
            )
            _uiState.update {
                it.copy(
                    activeStickyDares = it.activeStickyDares + dare,
                    dealPhase = GameDealPhase.IDLE,
                    selectedPlayer = null,
                    animatingName = "",
                    dealType = null,
                    challengeText = null,
                    stickyDarePresentContinuous = null,
                    stickyDareDurationLabel = null,
                    stickyDareDurationSeconds = null
                )
            }
            startStickyDareTimer(dare.id)
        } else {
            val isDare = state.dealType == GameDealType.TRUTH_OR_DARE &&
                    state.truthOrDareChoice == TruthOrDareChoice.DARE
            if (isDare && state.pendingCameraRequest) {
                _uiState.update { it.copy(showCameraRequest = true, pendingCameraRequest = false) }
            } else {
                resetDeal()
            }
        }
    }

    fun onCameraRequestDismissed() {
        dealJob?.cancel()
        _uiState.update { resetDealState(it) }
    }

    fun onPhotoCaptured(uri: Uri) {
        val partyId = currentPartyId ?: run {
            dealJob?.cancel()
            _uiState.update { resetDealState(it) }
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dir = File(context.filesDir, "party_photos/$partyId").also { it.mkdirs() }
                val dest = File(dir, "photo_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                partyPhotoRepository.addPhoto(partyId, dest.absolutePath)
            }
            dealJob?.cancel()
            _uiState.update { resetDealState(it) }
        }
    }

    fun onMiniGameAborted() = resetDeal()

    fun onMiniGameResultReceived(player1Score: Int, player2Score: Int) {
        val state = _uiState.value
        val result = MiniGameResult(
            player1Name = state.selectedPlayer?.nickName.orEmpty(),
            player1Score = player1Score,
            player2Name = state.miniGameOpponent?.nickName.orEmpty(),
            player2Score = player2Score
        )
        _uiState.update { it.copy(miniGameResult = result) }
    }

    fun cancelStickyDare(dareId: String) {
        stickyDareJobs[dareId]?.cancel()
        stickyDareJobs.remove(dareId)
        val dare = _uiState.value.activeStickyDares.find { it.id == dareId }
        viewModelScope.launch {
            _uiState.update { s ->
                s.copy(activeStickyDares = s.activeStickyDares.map { d ->
                    if (d.id == dareId) d.copy(isCompleted = true) else d
                })
            }
            delay(STICKY_DARE_EXIT_DELAY_MS)
            _uiState.update { s ->
                s.copy(activeStickyDares = s.activeStickyDares.filter { it.id != dareId })
            }
            val darePlayer = _uiState.value.players.find { it.nickName == dare?.playerName }
            _uiState.update { modeHandler.applyPunishment(it, darePlayer) }
        }
    }

    private fun resetDeal() {
        dealJob?.cancel()
        _uiState.update { resetDealState(it) }
    }

    private fun resetDealState(state: GameScreenState) = state.copy(
        dealPhase = GameDealPhase.IDLE,
        selectedPlayer = null,
        animatingName = "",
        dealType = null,
        challengeText = null,
        truthOrDareChoice = null,
        generalKnowledgeQuestion = null,
        selectedAnswerOption = null,
        miniGame = null,
        miniGameOpponent = null,
        miniGameResult = null,
        pendingCameraRequest = false,
        showCameraRequest = false
    )

    private fun startStickyDareTimer(dareId: String) {
        stickyDareJobs[dareId] = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { state ->
                    state.copy(
                        activeStickyDares = state.activeStickyDares.map { dare ->
                            if (dare.id == dareId)
                                dare.copy(remainingSeconds = (dare.remainingSeconds - 1).coerceAtLeast(0))
                            else dare
                        }
                    )
                }
                val remaining = _uiState.value.activeStickyDares
                    .find { it.id == dareId }?.remainingSeconds ?: break
                if (remaining <= 0) {
                    _uiState.update { state ->
                        state.copy(
                            activeStickyDares = state.activeStickyDares.map { dare ->
                                if (dare.id == dareId) dare.copy(isCompleted = true) else dare
                            }
                        )
                    }
                    delay(STICKY_DARE_EXIT_DELAY_MS)
                    _uiState.update { state ->
                        state.copy(activeStickyDares = state.activeStickyDares.filter { it.id != dareId })
                    }
                    break
                }
            }
            stickyDareJobs.remove(dareId)
        }
    }

    private fun isDealTypeEnabled(dealType: GameDealType): Boolean {
        val labelRes = when (dealType) {
            GameDealType.TRUTH_OR_DARE -> R.string.truth_or_dare
            GameDealType.GENERAL_KNOWLEDGE -> R.string.general_knowledge_title
            GameDealType.STICKY_DARE -> R.string.sticky_dares
            GameDealType.MINI_GAME -> R.string.mini_games
        }
        return GameOptionsSource.options.find { it.labelRes == labelRes }?.enabled ?: true
    }

    // Returns (challengeText, gkQuestion, presentContinuous, durationLabel, durationSeconds, miniGame)
    private data class ChallengeContent(
        val challengeText: String?,
        val gkQuestion: GeneralKnowledgeQuestion?,
        val presentContinuous: String?,
        val durationLabel: String?,
        val durationSeconds: Int?,
        val miniGame: MiniGame?
    )

    private fun buildChallengeContent(dealType: GameDealType): ChallengeContent {
        return when (dealType) {
            GameDealType.TRUTH_OR_DARE -> ChallengeContent(null, null, null, null, null, null)
            GameDealType.STICKY_DARE -> {
                val dares = context.resources.getStringArray(R.array.sticky_dares)
                val presentContinuous = context.resources.getStringArray(R.array.sticky_dares_present_continuous)
                val durationLabels = context.resources.getStringArray(R.array.sticky_dares_duration_labels)
                val durationSeconds = context.resources.getIntArray(R.array.sticky_dares_duration_seconds)
                val index = dares.indices.random()
                ChallengeContent(
                    challengeText = dares[index],
                    gkQuestion = null,
                    presentContinuous = presentContinuous[index],
                    durationLabel = durationLabels[index],
                    durationSeconds = durationSeconds[index],
                    miniGame = null
                )
            }
            GameDealType.GENERAL_KNOWLEDGE -> ChallengeContent(
                challengeText = null,
                gkQuestion = loadGkQuestions().randomOrNull(),
                presentContinuous = null,
                durationLabel = null,
                durationSeconds = null,
                miniGame = null
            )
            GameDealType.MINI_GAME -> {
                val eligibleGames = MiniGame.entries.filter {
                    _uiState.value.players.size >= it.minPlayers
                }
                ChallengeContent(
                    challengeText = null,
                    gkQuestion = null,
                    presentContinuous = null,
                    durationLabel = null,
                    durationSeconds = null,
                    miniGame = eligibleGames.randomOrNull()
                )
            }
        }
    }

    private fun loadGkQuestions(): List<GeneralKnowledgeQuestion> {
        val questions = context.resources.getStringArray(R.array.gk_questions)
        val optionsA = context.resources.getStringArray(R.array.gk_options_a)
        val optionsB = context.resources.getStringArray(R.array.gk_options_b)
        val correctOptions = context.resources.getStringArray(R.array.gk_correct_options)
        return questions.indices.map { i ->
            GeneralKnowledgeQuestion(
                question = questions[i],
                optionA = optionsA[i],
                optionB = optionsB[i],
                correctOption = correctOptions[i].first()
            )
        }
    }

    override fun onCleared() {
        dealJob?.cancel()
        stickyDareJobs.values.forEach { it.cancel() }
        super.onCleared()
    }
}
