package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiController
import com.example.combat.CombatArenaSnapshot
import com.example.combat.CombatEngine
import com.example.combat.CombatInputState
import com.example.managers.ProceduralAudioEngine
import com.example.managers.ProgressionCalculator
import com.example.managers.SaveManager
import com.example.managers.TournamentBracketState
import com.example.managers.TournamentManager
import com.example.model.AiDifficulty
import com.example.model.ArenaProfile
import com.example.model.DummyBehavior
import com.example.model.FighterProfile
import com.example.model.GameCatalog
import com.example.model.GameMode
import com.example.model.GraphicsPreset
import com.example.model.PlayerSaveData
import com.example.model.PlayerSettings
import com.example.model.PracticeStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    MAIN_MENU,
    SELECTION,
    TOURNAMENT_BRACKET,
    COMBAT_MATCH,
    ROSTER_UNLOCKS,
    PROFILE_STATS,
    SETTINGS,
    QA_DIAGNOSTICS
}

data class ArcadeLadderStage(
    val stageNumber: Int, // 1..5
    val opponentId: String,
    val arenaId: String,
    val difficulty: AiDifficulty,
    val stageTitle: String
)

data class DoomArcadeUiState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val saveData: PlayerSaveData = PlayerSaveData(),
    val selectedMode: GameMode = GameMode.ARCADE,
    val selectedPlayerFighterId: String = "kairo",
    val selectedOpponentFighterId: String = "vex",
    val selectedArenaId: String = "neon_district",
    val selectedDifficulty: AiDifficulty = AiDifficulty.NORMAL,
    val selectedFightStyle: com.example.model.FightStyle = com.example.model.FightStyle.RUSH,
    val bestOfThreeSeries: Boolean = true,
    val arcadeStageIndex: Int = 0, // 0..4 for 5-stage ladder
    val arcadeLadder: List<ArcadeLadderStage> = emptyList(),
    val tournamentBracket: TournamentBracketState = TournamentBracketState(),
    val combatSnapshot: CombatArenaSnapshot = CombatEngine().createInitialSnapshot(
        GameCatalog.fighterById("kairo"),
        GameCatalog.fighterById("vex")
    ),
    val isMatchPaused: Boolean = false,
    val practiceDummyBehavior: DummyBehavior = DummyBehavior.IDLE,
    val practiceInfiniteHealth: Boolean = true,
    val practiceInfiniteEnergy: Boolean = true,
    val practiceStats: PracticeStats = PracticeStats(),
    val measuredFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val statusBannerMessage: String? = null
) {
    val selectedPlayerProfile: FighterProfile
        get() = GameCatalog.fighterById(selectedPlayerFighterId)

    val selectedOpponentProfile: FighterProfile
        get() = GameCatalog.fighterById(selectedOpponentFighterId)

    val selectedArenaProfile: ArenaProfile
        get() = GameCatalog.arenaById(selectedArenaId)
}

class DoomArcadeViewModel(application: Application) : AndroidViewModel(application) {

    private val saveManager = SaveManager(application.applicationContext)
    private val combatEngine = CombatEngine()
    private val aiController = AiController()
    private val audioEngine = ProceduralAudioEngine()

    private val _uiState = MutableStateFlow(DoomArcadeUiState())
    val uiState: StateFlow<DoomArcadeUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var pendingPlayerInput = CombatInputState()

    init {
        val loadedSave = saveManager.loadSaveData()
        _uiState.update {
            it.copy(
                saveData = loadedSave,
                selectedPlayerFighterId = loadedSave.lastSelectedFighterId,
                selectedArenaId = loadedSave.lastSelectedArenaId,
                bestOfThreeSeries = loadedSave.settings.bestOfThreeVersus,
                arcadeLadder = generateArcadeLadder(loadedSave.lastSelectedFighterId, loadedSave.unlockedArenaIds)
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        val s = _uiState.value.saveData.settings
        audioEngine.playUiClick(s.masterVolume, s.sfxVolume)
        if (screen != AppScreen.COMBAT_MATCH) {
            stopCombatLoop()
        }
        _uiState.update { it.copy(currentScreen = screen, isMatchPaused = false) }
    }

    fun selectGameMode(mode: GameMode) {
        val s = _uiState.value.saveData.settings
        audioEngine.playUiClick(s.masterVolume, s.sfxVolume)
        _uiState.update { state ->
            val ladder = if (mode == GameMode.ARCADE) {
                generateArcadeLadder(state.selectedPlayerFighterId, state.saveData.unlockedArenaIds)
            } else state.arcadeLadder
            state.copy(
                selectedMode = mode,
                arcadeStageIndex = 0,
                arcadeLadder = ladder,
                currentScreen = AppScreen.SELECTION
            )
        }
    }

    fun selectPlayerFighter(fighterId: String) {
        val state = _uiState.value
        if (fighterId !in state.saveData.unlockedFighterIds) return
        audioEngine.playUiClick(state.saveData.settings.masterVolume, state.saveData.settings.sfxVolume)

        val updatedSave = state.saveData.copy(lastSelectedFighterId = fighterId)
        saveManager.saveData(updatedSave)
        _uiState.update {
            it.copy(
                selectedPlayerFighterId = fighterId,
                saveData = updatedSave,
                arcadeLadder = generateArcadeLadder(fighterId, updatedSave.unlockedArenaIds)
            )
        }
    }

    fun selectOpponentFighter(fighterId: String) {
        val s = _uiState.value.saveData.settings
        audioEngine.playUiClick(s.masterVolume, s.sfxVolume)
        _uiState.update { it.copy(selectedOpponentFighterId = fighterId) }
    }

    fun selectArena(arenaId: String) {
        val state = _uiState.value
        if (arenaId !in state.saveData.unlockedArenaIds) return
        audioEngine.playUiClick(state.saveData.settings.masterVolume, state.saveData.settings.sfxVolume)

        val updatedSave = state.saveData.copy(lastSelectedArenaId = arenaId)
        saveManager.saveData(updatedSave)
        _uiState.update {
            it.copy(
                selectedArenaId = arenaId,
                saveData = updatedSave
            )
        }
    }

    fun selectDifficulty(difficulty: AiDifficulty) {
        val s = _uiState.value.saveData.settings
        audioEngine.playUiClick(s.masterVolume, s.sfxVolume)
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun toggleBestOfThree(enabled: Boolean) {
        val state = _uiState.value
        val updatedSettings = state.saveData.settings.copy(bestOfThreeVersus = enabled)
        val updatedSave = state.saveData.copy(settings = updatedSettings)
        saveManager.saveData(updatedSave)
        _uiState.update {
            it.copy(
                bestOfThreeSeries = enabled,
                saveData = updatedSave
            )
        }
    }

    fun launchSelectedMode() {
        val state = _uiState.value
        audioEngine.playUiClick(state.saveData.settings.masterVolume, state.saveData.settings.sfxVolume)

        when (state.selectedMode) {
            GameMode.ARCADE -> {
                val ladder = generateArcadeLadder(state.selectedPlayerFighterId, state.saveData.unlockedArenaIds)
                val firstStage = ladder.first()
                _uiState.update {
                    it.copy(
                        arcadeLadder = ladder,
                        arcadeStageIndex = 0,
                        selectedOpponentFighterId = firstStage.opponentId,
                        selectedArenaId = firstStage.arenaId,
                        selectedDifficulty = firstStage.difficulty
                    )
                }
                startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
            }
            GameMode.TOURNAMENT -> {
                val bracket = TournamentManager.createBracket(
                    playerFighterId = state.selectedPlayerFighterId,
                    arenaId = state.selectedArenaId,
                    difficulty = state.selectedDifficulty
                )
                _uiState.update {
                    it.copy(
                        tournamentBracket = bracket,
                        currentScreen = AppScreen.TOURNAMENT_BRACKET
                    )
                }
            }
            GameMode.PRACTICE -> {
                _uiState.update {
                    it.copy(practiceStats = PracticeStats())
                }
                startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
            }
            GameMode.VERSUS -> {
                startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
            }
        }
    }

    fun startCurrentTournamentMatch() {
        val state = _uiState.value
        val bracket = state.tournamentBracket
        val activeMatch = if (bracket.currentRoundIndex == 0) {
            bracket.semifinals.firstOrNull { it.involvesPlayer }
        } else {
            bracket.grandFinal
        } ?: return

        _uiState.update {
            it.copy(
                selectedPlayerFighterId = activeMatch.fighterAId,
                selectedOpponentFighterId = activeMatch.fighterBId,
                selectedArenaId = bracket.arenaId,
                selectedDifficulty = bracket.difficulty
            )
        }
        startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
    }

    fun startMatch(
        roundNumber: Int = 1,
        playerRoundsWon: Int = 0,
        opponentRoundsWon: Int = 0
    ) {
        val state = _uiState.value
        val playerProfile = GameCatalog.fighterById(state.selectedPlayerFighterId)
        val opponentProfile = GameCatalog.fighterById(state.selectedOpponentFighterId)

        aiController.reset()
        pendingPlayerInput = CombatInputState()

        val initialSnapshot = combatEngine.createInitialSnapshot(
            playerProfile = playerProfile,
            opponentProfile = opponentProfile,
            roundNumber = roundNumber,
            playerRoundsWon = playerRoundsWon,
            opponentRoundsWon = opponentRoundsWon,
            playerStyle = state.selectedFightStyle
        )

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.COMBAT_MATCH,
                combatSnapshot = initialSnapshot,
                isMatchPaused = false
            )
        }

        startCombatLoop()
    }

    fun selectFightStyle(style: com.example.model.FightStyle) {
        val s = _uiState.value.saveData.settings
        audioEngine.playUiClick(s.masterVolume, s.sfxVolume)
        _uiState.update { state ->
            state.copy(
                selectedFightStyle = style,
                combatSnapshot = state.combatSnapshot.copy(
                    player = state.combatSnapshot.player.copy(fightStyle = style)
                )
            )
        }
    }

    fun launchQuickFight() {
        val state = _uiState.value
        audioEngine.playUiClick(state.saveData.settings.masterVolume, state.saveData.settings.sfxVolume)
        val opponents = GameCatalog.fighters.map { it.id }.filter { it != state.selectedPlayerFighterId }
        val oppId = opponents.firstOrNull() ?: "vex"
        _uiState.update {
            it.copy(
                selectedMode = GameMode.VERSUS,
                selectedOpponentFighterId = oppId
            )
        }
        startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
    }

    fun triggerEnergySurge() {
        pendingPlayerInput = pendingPlayerInput.copy(energySurgePressed = true)
    }

    private fun startCombatLoop() {
        stopCombatLoop()
        gameLoopJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            var lastTimeNs = System.nanoTime()
            while (isActive) {
                val targetFps = _uiState.value.saveData.settings.fpsCap.coerceIn(30, 120)
                val targetFrameMs = (1000L / targetFps).coerceAtLeast(8L)
                delay(targetFrameMs)

                val nowNs = System.nanoTime()
                val dtSec = ((nowNs - lastTimeNs) / 1_000_000_000f).coerceIn(0.004f, 0.05f)
                lastTimeNs = nowNs

                val currentState = _uiState.value
                if (currentState.currentScreen != AppScreen.COMBAT_MATCH || currentState.isMatchPaused) {
                    continue
                }

                stepCombatFrame(dtSec)
            }
        }
    }

    private fun stopCombatLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    fun stepCombatFrame(dtSec: Float) {
        val state = _uiState.value
        val snap = state.combatSnapshot
        if (snap.isRoundOver) return

        val aiInput = aiController.computeOpponentInput(
            snapshot = snap,
            mode = state.selectedMode,
            difficulty = state.selectedDifficulty,
            dummyBehavior = state.practiceDummyBehavior,
            dt = dtSec
        )

        val pInput = pendingPlayerInput
        // Clear one-shot triggers after consuming them
        pendingPlayerInput = pendingPlayerInput.copy(
            jumpPressed = false,
            dodgePressed = false,
            lightPressed = false,
            heavyPressed = false,
            specialPressed = false,
            ultimatePressed = false,
            energySurgePressed = false
        )

        val isPractice = state.selectedMode == GameMode.PRACTICE
        val (nextSnap, audioEvents) = combatEngine.step(
            snapshot = snap,
            playerInput = pInput,
            opponentInput = aiInput,
            rawDeltaSec = dtSec,
            infiniteHealthPractice = isPractice && state.practiceInfiniteHealth,
            infiniteEnergyPractice = isPractice && state.practiceInfiniteEnergy,
            disableRoundTimer = isPractice
        )

        val settings = state.saveData.settings
        audioEvents.forEach { event ->
            audioEngine.playCombatEvent(event, settings.masterVolume, settings.sfxVolume)
        }

        val updatedPracticeStats = if (isPractice) {
            PracticeStats(
                attempts = nextSnap.playerAttempts,
                hitsLanded = nextSnap.playerHitsLanded,
                totalDamageDealt = nextSnap.playerTotalDamageDealt,
                lastHitDamage = nextSnap.playerLastHitDamage,
                currentCombo = nextSnap.player.comboCount,
                maxCombo = nextSnap.playerMaxCombo,
                specialUses = nextSnap.playerSpecialsUsed,
                ultimateUses = nextSnap.playerUltimatesUsed
            )
        } else state.practiceStats

        val instantFps = (1f / dtSec).toInt().coerceIn(15, 120)
        val finalSnapshot = if (nextSnap.isRoundOver && !snap.isRoundOver) {
            resolveRoundEnd(state, nextSnap)
        } else {
            nextSnap
        }

        _uiState.update {
            it.copy(
                combatSnapshot = finalSnapshot,
                practiceStats = updatedPracticeStats,
                measuredFps = (it.measuredFps * 4 + instantFps) / 5,
                frameTimeMs = dtSec * 1000f
            )
        }
    }

    private fun resolveRoundEnd(
        state: DoomArcadeUiState,
        roundEndSnap: CombatArenaSnapshot
    ): CombatArenaSnapshot {
        val playerWonRound = roundEndSnap.winnerIsPlayer == true
        val nextP1Wins = roundEndSnap.playerRoundsWon + (if (playerWonRound) 1 else 0)
        val nextP2Wins = roundEndSnap.opponentRoundsWon + (if (!playerWonRound) 1 else 0)

        val roundsNeededToWin = when (state.selectedMode) {
            GameMode.VERSUS -> if (state.bestOfThreeSeries) 2 else 1
            GameMode.ARCADE, GameMode.TOURNAMENT -> 2 // Best of 3 rounds per stage
            GameMode.PRACTICE -> 1
        }

        val matchFinished = nextP1Wins >= roundsNeededToWin || nextP2Wins >= roundsNeededToWin
        if (matchFinished && state.selectedMode != GameMode.PRACTICE) {
            val playerWonMatch = nextP1Wins > nextP2Wins
            val isFinalArcadeStage = state.selectedMode == GameMode.ARCADE &&
                state.arcadeStageIndex >= state.arcadeLadder.size - 1

            var updatedBracket = state.tournamentBracket
            var wonEntireTournament = false
            if (state.selectedMode == GameMode.TOURNAMENT) {
                updatedBracket = TournamentManager.recordPlayerMatchOutcome(state.tournamentBracket, playerWonMatch)
                wonEntireTournament = updatedBracket.championFighterId == state.selectedPlayerFighterId &&
                    updatedBracket.currentRoundIndex == 2
            }

            val updatedSave = ProgressionCalculator.applyMatchResult(
                currentSave = state.saveData,
                playerWon = playerWonMatch,
                completedArcadeLadder = isFinalArcadeStage && playerWonMatch,
                completedTournamentVictory = wonEntireTournament,
                matchMaxCombo = roundEndSnap.playerMaxCombo
            )
            saveManager.saveData(updatedSave)

            _uiState.update {
                it.copy(
                    saveData = updatedSave,
                    tournamentBracket = updatedBracket
                )
            }
        }

        return roundEndSnap.copy(
            playerRoundsWon = nextP1Wins,
            opponentRoundsWon = nextP2Wins,
            isMatchOver = matchFinished
        )
    }

    fun proceedToNextRoundOrAction() {
        val state = _uiState.value
        val snap = state.combatSnapshot

        if (!snap.isMatchOver) {
            // Next round in current Best-of-Three series
            startMatch(
                roundNumber = snap.roundNumber + 1,
                playerRoundsWon = snap.playerRoundsWon,
                opponentRoundsWon = snap.opponentRoundsWon
            )
            return
        }

        val playerWonMatch = snap.playerRoundsWon > snap.opponentRoundsWon
        when (state.selectedMode) {
            GameMode.ARCADE -> {
                if (playerWonMatch && state.arcadeStageIndex < state.arcadeLadder.size - 1) {
                    val nextIndex = state.arcadeStageIndex + 1
                    val nextStage = state.arcadeLadder[nextIndex]
                    _uiState.update {
                        it.copy(
                            arcadeStageIndex = nextIndex,
                            selectedOpponentFighterId = nextStage.opponentId,
                            selectedArenaId = nextStage.arenaId,
                            selectedDifficulty = nextStage.difficulty
                        )
                    }
                    startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
                } else {
                    navigateTo(AppScreen.MAIN_MENU)
                }
            }
            GameMode.TOURNAMENT -> {
                navigateTo(AppScreen.TOURNAMENT_BRACKET)
            }
            GameMode.VERSUS, GameMode.PRACTICE -> {
                // Rematch
                startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
            }
        }
    }

    fun rematchImmediately() {
        startMatch(roundNumber = 1, playerRoundsWon = 0, opponentRoundsWon = 0)
    }

    fun setMoveAxis(axis: Float) {
        pendingPlayerInput = pendingPlayerInput.copy(moveAxis = axis.coerceIn(-1f, 1f))
    }

    fun setBlockHeld(held: Boolean) {
        pendingPlayerInput = pendingPlayerInput.copy(blockHeld = held)
    }

    fun triggerJump() {
        pendingPlayerInput = pendingPlayerInput.copy(jumpPressed = true)
    }

    fun triggerDodge() {
        pendingPlayerInput = pendingPlayerInput.copy(dodgePressed = true)
    }

    fun triggerLightAttack() {
        pendingPlayerInput = pendingPlayerInput.copy(lightPressed = true)
    }

    fun triggerHeavyAttack() {
        pendingPlayerInput = pendingPlayerInput.copy(heavyPressed = true)
    }

    fun triggerSpecialAttack() {
        pendingPlayerInput = pendingPlayerInput.copy(specialPressed = true)
    }

    fun triggerUltimateAttack() {
        pendingPlayerInput = pendingPlayerInput.copy(ultimatePressed = true)
    }

    fun togglePauseMatch() {
        _uiState.update { it.copy(isMatchPaused = !it.isMatchPaused) }
    }

    fun setPracticeDummyBehavior(behavior: DummyBehavior) {
        _uiState.update { it.copy(practiceDummyBehavior = behavior) }
    }

    fun togglePracticeInfiniteHealth(enabled: Boolean) {
        _uiState.update { it.copy(practiceInfiniteHealth = enabled) }
    }

    fun togglePracticeInfiniteEnergy(enabled: Boolean) {
        _uiState.update { it.copy(practiceInfiniteEnergy = enabled) }
    }

    fun resetPracticeSession() {
        val state = _uiState.value
        val freshSnap = combatEngine.createInitialSnapshot(
            playerProfile = state.selectedPlayerProfile,
            opponentProfile = state.selectedOpponentProfile
        )
        _uiState.update {
            it.copy(
                combatSnapshot = freshSnap,
                practiceStats = PracticeStats()
            )
        }
    }

    fun updateSettings(transform: (PlayerSettings) -> PlayerSettings) {
        val state = _uiState.value
        val newSettings = transform(state.saveData.settings)
        val updatedSave = state.saveData.copy(settings = newSettings)
        saveManager.saveData(updatedSave)
        _uiState.update {
            it.copy(
                saveData = updatedSave,
                bestOfThreeSeries = newSettings.bestOfThreeVersus
            )
        }
    }

    fun simulateArcadeVictoryUnlockTest() {
        val state = _uiState.value
        var updated = state.saveData
        repeat(3) {
            updated = ProgressionCalculator.applyMatchResult(
                currentSave = updated,
                playerWon = true,
                completedArcadeLadder = true,
                matchMaxCombo = 6
            )
        }
        saveManager.saveData(updated)
        _uiState.update {
            it.copy(
                saveData = updated,
                statusBannerMessage = "Verified 3 Arcade Wins: Sky Temple & Level ${updated.level} unlocked!"
            )
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    private fun generateArcadeLadder(
        playerFighterId: String,
        unlockedArenas: Set<String>
    ): List<ArcadeLadderStage> {
        val opponents = GameCatalog.fighters.map { it.id }.filter { it != playerFighterId }
        val arenas = GameCatalog.arenas.map { it.id }.filter { it in unlockedArenas }.ifEmpty {
            listOf("neon_district", "ember_temple", "frost_forge")
        }
        val difficulties = listOf(
            AiDifficulty.EASY,
            AiDifficulty.NORMAL,
            AiDifficulty.NORMAL,
            AiDifficulty.HARD,
            AiDifficulty.EXPERT
        )
        val titles = listOf(
            "STAGE 1 // NEON INITIATION",
            "STAGE 2 // RIVAL INTERCEPT",
            "STAGE 3 // ELEMENTAL CRUCIBLE",
            "STAGE 4 // SEMI-FINAL GAUNTLET",
            "STAGE 5 // GRAND CHAMPIONSHIP"
        )
        return (0 until 5).map { idx ->
            ArcadeLadderStage(
                stageNumber = idx + 1,
                opponentId = opponents[idx % opponents.size],
                arenaId = arenas[idx % arenas.size],
                difficulty = difficulties[idx],
                stageTitle = titles[idx]
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCombatLoop()
    }
}
