package com.restrusher.partypuzl.ui.views.game.gameScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.repositories.interfaces.PartyPhotoRepository
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

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val partyPhotoRepository: PartyPhotoRepository
) : ViewModel() {

    companion object {
        private const val STICKY_DARE_EXIT_DELAY_MS = 400L
    }

    private val currentPartyId: Int? = GamePlayersList.currentPartyId

    private val modeHandler: GameModeHandler = when (GameOptionsSource.currentGameModeNameRes) {
        R.string.bar_game_mode -> BarModeHandler()
        R.string.couples_game_mode -> CouplesModeHandler()
        R.string.party_puzz_game_mode -> PartyPuzlModeHandler()
        else -> NoOpModeHandler()
    }

    private val isPartyPuzlMode = GameOptionsSource.currentGameModeNameRes == R.string.party_puzz_game_mode

    private val _uiState = MutableStateFlow(
        GameScreenState(
            players = GamePlayersList.PlayersList.toList(),
            barMode = BarModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.bar_game_mode || isPartyPuzlMode),
            couplesMode = CouplesModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.couples_game_mode || isPartyPuzlMode)
        )
    )
    val uiState: StateFlow<GameScreenState> = _uiState.asStateFlow()

    private var dealJob: Job? = null
    private var outcomeJob: Job? = null
    private val stickyDareJobs = mutableMapOf<String, Job>()

    // Round-based player selection: each player plays once per round before any repeats
    private val roundQueue = mutableListOf<Player>()
    private var roundNumber = 0

    init {
        // No deal has been played yet, so the first hero card is drawn at random rather than
        // always opening on Truth or Dare.
        _uiState.update { it.copy(heroDealType = it.availableDealTypes.random()) }
        advanceToNextTurn()
    }

    private fun nextPlayerInRound(players: List<Player>): Player? {
        if (players.isEmpty()) return null
        if (roundQueue.isEmpty()) {
            roundQueue.addAll(players.shuffled())
            roundNumber++
        }
        return roundQueue.removeAt(0)
    }

    fun onDealChosen(dealType: GameDealType, truthOrDareChoice: TruthOrDareChoice? = null) {
        if (_uiState.value.dealPhase != GameDealPhase.DEAL_CHOICE) return
        startChallenge(dealType, truthOrDareChoice)
    }

    fun onSurpriseRequested() {
        val state = _uiState.value
        if (state.dealPhase != GameDealPhase.DEAL_CHOICE) return
        val target = state.availableDealTypes.random()
        _uiState.update {
            it.copy(dealPhase = GameDealPhase.SURPRISE_SHUFFLE, surpriseDealType = target)
        }
        dealJob?.cancel()
        dealJob = viewModelScope.launch {
            delay(SURPRISE_SHUFFLE_DURATION_MS)
            startChallenge(target, null)
        }
    }

    private fun startChallenge(dealType: GameDealType, truthOrDareChoice: TruthOrDareChoice?) {
        val state = _uiState.value
        val choice = if (dealType == GameDealType.TRUTH_OR_DARE) {
            truthOrDareChoice ?: TruthOrDareChoice.entries.random()
        } else {
            null
        }
        val content = buildChallengeContent(
            ChallengeRequest(
                dealType = dealType,
                truthOrDareChoice = choice,
                playerName = state.selectedPlayer?.nickName.orEmpty(),
                activeStickyDares = state.activeStickyDares
            )
        )
        _uiState.update {
            it.copy(
                dealPhase = GameDealPhase.CHALLENGE_SHOWN,
                dealType = dealType,
                heroDealType = dealType,
                surpriseDealType = null,
                truthOrDareChoice = choice,
                challengeText = content.challengeText,
                generalKnowledgeQuestion = content.gkQuestion,
                stickyDarePresentContinuous = content.presentContinuous,
                stickyDareDurationLabel = content.durationLabel,
                stickyDareDurationSeconds = content.durationSeconds,
                miniGame = content.miniGame,
                pendingCameraRequest = isCameraAvailable()
            )
        }
    }

    fun onGeneralKnowledgeAnswered(option: Char) {
        if (_uiState.value.selectedAnswerOption != null) return
        _uiState.update { it.copy(selectedAnswerOption = option) }
    }

    fun onMiniGameOpponentSelected(opponent: Player) {
        _uiState.update { it.copy(miniGameOpponent = opponent) }
    }

    fun onTruthOrDareSkipped() {
        if (_uiState.value.dealType != GameDealType.TRUTH_OR_DARE) return
        applyOutcome { modeHandler.applyPunishment(it, it.selectedPlayer) }
    }

    fun onStickyDareSkipped() {
        if (_uiState.value.dealType != GameDealType.STICKY_DARE) return
        applyOutcome { modeHandler.applyPunishment(it, it.selectedPlayer) }
    }

    fun onMiniGameDealFinished() {
        if (_uiState.value.miniGameResult == null) return
        applyOutcome { modeHandler.applyMiniGameResult(it) }
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
        outcomeJob?.cancel()
        val state = _uiState.value
        // A dare cancelled from the sticky-dares sheet punishes mid-turn — clear it and stay put,
        // otherwise the current player would silently lose their turn.
        if (state.dealPhase != GameDealPhase.CHALLENGE_SHOWN) {
            _uiState.update { modeHandler.clearEvent(it).copy(outcomeStage = null) }
            return
        }
        if (state.pendingCameraRequest) {
            _uiState.update {
                modeHandler.clearEvent(it).copy(
                    outcomeStage = null,
                    showCameraRequest = true,
                    pendingCameraRequest = false
                )
            }
            return
        }
        advanceToNextTurn()
    }

    fun onChallengeDismissed() {
        val state = _uiState.value
        if (!state.isChallengeDismissible) return

        when (state.dealType) {
            GameDealType.GENERAL_KNOWLEDGE -> dismissGeneralKnowledge(state)
            GameDealType.STICKY_DARE -> dismissStickyDare(state)
            else -> dismissTruthOrDare(state)
        }
    }

    private fun dismissGeneralKnowledge(state: GameScreenState) {
        val isCorrect = state.generalKnowledgeQuestion != null &&
                state.selectedAnswerOption == state.generalKnowledgeQuestion.correctOption
        applyOutcome {
            if (isCorrect) modeHandler.applyReward(it)
            else modeHandler.applyPunishment(it, it.selectedPlayer)
        }
    }

    private fun dismissStickyDare(state: GameScreenState) {
        val dare = ActiveStickyDare(
            id = UUID.randomUUID().toString(),
            playerName = state.selectedPlayer?.nickName.orEmpty(),
            presentContinuousText = state.stickyDarePresentContinuous.orEmpty(),
            durationLabel = state.stickyDareDurationLabel.orEmpty(),
            totalSeconds = state.stickyDareDurationSeconds ?: 60,
            remainingSeconds = state.stickyDareDurationSeconds ?: 60
        )
        _uiState.update { it.copy(activeStickyDares = it.activeStickyDares + dare) }
        if (state.pendingCameraRequest) {
            _uiState.update {
                it.copy(
                    showCameraRequest = true,
                    pendingCameraRequest = false,
                    stickyDarePresentContinuous = null,
                    stickyDareDurationLabel = null,
                    stickyDareDurationSeconds = null
                )
            }
        } else {
            advanceToNextTurn()
        }
        startStickyDareTimer(dare.id)
    }

    private fun dismissTruthOrDare(state: GameScreenState) {
        val isDare = state.dealType == GameDealType.TRUTH_OR_DARE &&
                state.truthOrDareChoice == TruthOrDareChoice.DARE
        if (isDare && state.pendingCameraRequest) {
            _uiState.update { it.copy(showCameraRequest = true, pendingCameraRequest = false) }
        } else {
            advanceToNextTurn()
        }
    }

    fun onCameraRequestDismissed() = advanceToNextTurn()

    fun onPhotoCaptured(uri: Uri) {
        val partyId = currentPartyId ?: run {
            advanceToNextTurn()
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { storePhoto(partyId, uri) }
            advanceToNextTurn()
        }
    }

    private suspend fun storePhoto(partyId: Int, uri: Uri) {
        val dir = File(context.filesDir, "party_photos/$partyId").also { it.mkdirs() }
        val dest = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        partyPhotoRepository.addPhoto(partyId, dest.absolutePath)
    }

    fun onMiniGameAborted() = advanceToNextTurn()

    fun onHotPotatoResultReceived(loserName: String) {
        _uiState.update { it.copy(miniGameResult = LoserMiniGameResult(loserName = loserName)) }
    }

    fun onSimonSaysResultReceived(loserName: String) {
        _uiState.update { it.copy(miniGameResult = LoserMiniGameResult(loserName = loserName)) }
    }

    fun onCircleMasterResultReceived(loserName: String) {
        _uiState.update { it.copy(miniGameResult = LoserMiniGameResult(loserName = loserName)) }
    }

    fun onMiniGameResultReceived(player1Score: Int, player2Score: Int) {
        val state = _uiState.value
        val result = ScoredMiniGameResult(
            player1Name = state.selectedPlayer?.nickName.orEmpty(),
            player1Score = player1Score,
            player2Name = state.miniGameOpponent?.nickName.orEmpty(),
            player2Score = player2Score,
            showScoreDetails = state.miniGame != MiniGame.TAP_WAR
        )
        _uiState.update { it.copy(miniGameResult = result) }
    }

    fun cancelStickyDare(dareId: String) {
        stickyDareJobs.remove(dareId)?.cancel()
        val dare = _uiState.value.activeStickyDares.find { it.id == dareId }
        viewModelScope.launch {
            completeAndRemoveStickyDare(dareId)
            val darePlayer = _uiState.value.players.find { it.nickName == dare?.playerName }
            _uiState.update { modeHandler.applyPunishment(it, darePlayer) }
            if (_uiState.value.hasActiveModeEvent) startOutcomeSpin()
        }
    }

    // Reward / punishment lands after a short slot-reel spin. When the mode produced no event
    // (Standard mode, or Party Puzl rolling its no-op handler) the turn just moves on.
    private fun applyOutcome(transform: (GameScreenState) -> GameScreenState) {
        _uiState.update(transform)
        if (_uiState.value.hasActiveModeEvent) startOutcomeSpin() else advanceToNextTurn()
    }

    private fun startOutcomeSpin() {
        _uiState.update { it.copy(outcomeStage = OutcomeStage.SPINNING) }
        outcomeJob?.cancel()
        outcomeJob = viewModelScope.launch {
            delay(OUTCOME_SPIN_DURATION_MS)
            _uiState.update { it.copy(outcomeStage = OutcomeStage.REVEALED) }
        }
    }

    // The round queue must advance exactly once per turn. MutableStateFlow.update is a
    // compare-and-set retry loop whose lambda can run more than once, so the queue is
    // stepped here, outside of it, and only the resulting values are folded into the state.
    private fun advanceToNextTurn() {
        dealJob?.cancel()
        outcomeJob?.cancel()
        val nextPlayer = nextPlayerInRound(_uiState.value.players)
        val round = roundNumber
        _uiState.update {
            clearedTurn(modeHandler.clearEvent(it)).copy(
                dealPhase = GameDealPhase.DEAL_CHOICE,
                selectedPlayer = nextPlayer,
                roundNumber = round
            )
        }
    }

    private fun clearedTurn(state: GameScreenState) = state.copy(
        dealType = null,
        surpriseDealType = null,
        challengeText = null,
        truthOrDareChoice = null,
        generalKnowledgeQuestion = null,
        selectedAnswerOption = null,
        stickyDarePresentContinuous = null,
        stickyDareDurationLabel = null,
        stickyDareDurationSeconds = null,
        miniGame = null,
        miniGameOpponent = null,
        miniGameResult = null,
        outcomeStage = null,
        pendingCameraRequest = false,
        showCameraRequest = false
    )

    private fun isCameraAvailable(): Boolean = currentPartyId != null &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private suspend fun completeAndRemoveStickyDare(dareId: String) {
        _uiState.update { state ->
            state.copy(activeStickyDares = state.activeStickyDares.map { dare ->
                if (dare.id == dareId) dare.copy(isCompleted = true) else dare
            })
        }
        delay(STICKY_DARE_EXIT_DELAY_MS)
        _uiState.update { state ->
            state.copy(activeStickyDares = state.activeStickyDares.filter { it.id != dareId })
        }
    }

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
                    completeAndRemoveStickyDare(dareId)
                    break
                }
            }
            stickyDareJobs.remove(dareId)
        }
    }

    private data class ChallengeRequest(
        val dealType: GameDealType,
        val truthOrDareChoice: TruthOrDareChoice?,
        val playerName: String,
        val activeStickyDares: List<ActiveStickyDare>
    )

    private data class ChallengeContent(
        val challengeText: String? = null,
        val gkQuestion: GeneralKnowledgeQuestion? = null,
        val presentContinuous: String? = null,
        val durationLabel: String? = null,
        val durationSeconds: Int? = null,
        val miniGame: MiniGame? = null
    )

    private fun buildChallengeContent(request: ChallengeRequest): ChallengeContent =
        when (request.dealType) {
            GameDealType.TRUTH_OR_DARE -> buildTruthOrDareContent(request.truthOrDareChoice)
            GameDealType.STICKY_DARE -> buildStickyDareContent(request)
            GameDealType.GENERAL_KNOWLEDGE -> ChallengeContent(gkQuestion = loadGkQuestions().randomOrNull())
            GameDealType.MINI_GAME -> ChallengeContent(
                miniGame = MiniGame.entries
                    .filter { _uiState.value.players.size >= it.minPlayers }
                    .randomOrNull()
            )
        }

    private fun buildTruthOrDareContent(choice: TruthOrDareChoice?): ChallengeContent {
        val texts = when (choice) {
            TruthOrDareChoice.TRUTH -> context.resources.getStringArray(R.array.truth_texts)
            else -> context.resources.getStringArray(R.array.dare_texts)
        }
        return ChallengeContent(challengeText = texts.random())
    }

    private fun buildStickyDareContent(request: ChallengeRequest): ChallengeContent {
        val dares = context.resources.getStringArray(R.array.sticky_dares)
        val presentContinuous = context.resources.getStringArray(R.array.sticky_dares_present_continuous)
        val durationLabels = context.resources.getStringArray(R.array.sticky_dares_duration_labels)
        val durationSeconds = context.resources.getIntArray(R.array.sticky_dares_duration_seconds)
        val activePcTexts = request.activeStickyDares
            .filter { it.playerName == request.playerName && !it.isCompleted }
            .map { it.presentContinuousText }
            .toSet()
        val eligibleIndices = dares.indices.filter { presentContinuous[it] !in activePcTexts }
        val index = (if (eligibleIndices.isNotEmpty()) eligibleIndices else dares.indices.toList()).random()
        return ChallengeContent(
            challengeText = dares[index],
            presentContinuous = presentContinuous[index],
            durationLabel = durationLabels[index],
            durationSeconds = durationSeconds[index]
        )
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
        outcomeJob?.cancel()
        stickyDareJobs.values.forEach { it.cancel() }
        super.onCleared()
    }
}
