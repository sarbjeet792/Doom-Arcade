package com.example.combat

import androidx.compose.ui.graphics.Color
import com.example.model.AttackType
import com.example.model.FightStyle
import com.example.model.FighterProfile
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class FighterState {
    IDLE,
    MOVING,
    JUMPING,
    ATTACKING,
    BLOCKING,
    DODGING,
    TAKING_DAMAGE,
    SPECIAL_ATTACK,
    ULTIMATE_ATTACK,
    STUNNED,
    DEFEATED,
    VICTORY
}

data class CombatInputState(
    val moveAxis: Float = 0f, // -1f left, +1f right
    val jumpPressed: Boolean = false,
    val blockHeld: Boolean = false,
    val dodgePressed: Boolean = false,
    val lightPressed: Boolean = false,
    val heavyPressed: Boolean = false,
    val specialPressed: Boolean = false,
    val ultimatePressed: Boolean = false,
    val energySurgePressed: Boolean = false
)

data class FighterRuntimeState(
    val profile: FighterProfile,
    val isPlayerOne: Boolean,
    val fightStyle: FightStyle = FightStyle.RUSH,
    val x: Float,
    val y: Float = 0f,
    val zOffset: Float = 0f, // 2.5D sidestep depth during dodge
    val velocityX: Float = 0f,
    val velocityY: Float = 0f,
    val facingRight: Boolean,
    val health: Int = profile.maxHealth,
    val energy: Int = CombatConstants.INITIAL_ROUND_ENERGY,
    val state: FighterState = FighterState.IDLE,
    val stateTimerSec: Float = 0f,
    val currentAttack: AttackType? = null,
    val attackElapsedSec: Float = 0f,
    val attackInstanceId: Long = 0L,
    val lastRegisteredAttackId: Long = -1L,
    val bufferedAttack: AttackType? = null,
    val dodgeInvulnerableTimerSec: Float = 0f,
    val dodgeCooldownTimerSec: Float = 0f,
    val energySurgeCooldownSec: Float = 0f,
    val stunTimerSec: Float = 0f,
    val comboCount: Int = 0,
    val comboChain: List<AttackType> = emptyList(),
    val timeSinceLastLandedHitSec: Float = 99f,
    val lastComboLabel: String = "",
    val squashStretchY: Float = 1f,
    val hitLeanAngleDeg: Float = 0f,
    val flashAlpha: Float = 0f,
    val auraIntensity: Float = 0.25f
) {
    val isAlive: Boolean get() = health > 0 && state != FighterState.DEFEATED
    val isDodgeInvulnerable: Boolean get() = dodgeInvulnerableTimerSec > 0f
    val isBlockingActive: Boolean get() = state == FighterState.BLOCKING
    val canUseSpecial: Boolean get() = isAlive && energy >= CombatConstants.SPECIAL_ENERGY_COST
    val canUseUltimate: Boolean get() = isAlive && energy >= CombatConstants.ULTIMATE_ENERGY_COST
    val canDodge: Boolean get() = isAlive && dodgeCooldownTimerSec <= 0f && state != FighterState.DODGING
    val canSurgeEnergy: Boolean get() = isAlive && energySurgeCooldownSec <= 0f && energy < CombatConstants.MAX_ENERGY

    val attackProgress: Float
        get() {
            val atk = currentAttack ?: return 0f
            val total = (atk.startupSec + atk.activeSec + atk.recoverySec).coerceAtLeast(0.05f)
            return (attackElapsedSec / total).coerceIn(0f, 1f)
        }
}

data class FloatingDamageText(
    val id: Long,
    val text: String,
    val subText: String = "",
    val worldX: Float,
    val worldY: Float,
    val color: Color,
    val isCrit: Boolean = false,
    val ageSec: Float = 0f,
    val lifetimeSec: Float = 0.95f
)

data class CombatParticle(
    val worldX: Float,
    val worldY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float,
    val ageSec: Float = 0f,
    val lifetimeSec: Float = 0.45f
)

data class CombatShockwave(
    val worldX: Float,
    val worldY: Float,
    val color: Color,
    val maxRadius: Float,
    val ageSec: Float = 0f,
    val lifetimeSec: Float = 0.42f
)

sealed class CombatAudioEvent {
    data object LightSwing : CombatAudioEvent()
    data object HeavySwing : CombatAudioEvent()
    data class HitImpact(val isHeavy: Boolean, val isCrit: Boolean) : CombatAudioEvent()
    data object GuardImpact : CombatAudioEvent()
    data object DodgeWhoosh : CombatAudioEvent()
    data class SpecialCast(val fighterId: String) : CombatAudioEvent()
    data class UltimateCast(val fighterId: String) : CombatAudioEvent()
    data object Knockout : CombatAudioEvent()
}

data class CombatArenaSnapshot(
    val player: FighterRuntimeState,
    val opponent: FighterRuntimeState,
    val roundNumber: Int = 1,
    val playerRoundsWon: Int = 0,
    val opponentRoundsWon: Int = 0,
    val roundTimerSec: Float = 99f,
    val isRoundOver: Boolean = false,
    val isMatchOver: Boolean = false,
    val winnerIsPlayer: Boolean? = null,
    val announcementBanner: String = "ROUND 1 • FIGHT!",
    val announcementTimerSec: Float = 1.6f,
    val specialCutInBanner: String = "",
    val specialCutInColor: Color = Color(0xFF00E5FF),
    val specialCutInTimerSec: Float = 0f,
    val screenShakeMagnitude: Float = 0f,
    val floatingTexts: List<FloatingDamageText> = emptyList(),
    val particles: List<CombatParticle> = emptyList(),
    val shockwaves: List<CombatShockwave> = emptyList(),
    val playerAttempts: Int = 0,
    val playerHitsLanded: Int = 0,
    val playerTotalDamageDealt: Int = 0,
    val playerLastHitDamage: Int = 0,
    val playerMaxCombo: Int = 0,
    val playerSpecialsUsed: Int = 0,
    val playerUltimatesUsed: Int = 0
)

class CombatEngine(
    private val random: Random = Random(42)
) {
    private var nextAttackId = 1L
    private var nextFloatingTextId = 1L

    fun createInitialSnapshot(
        playerProfile: FighterProfile,
        opponentProfile: FighterProfile,
        roundNumber: Int = 1,
        playerRoundsWon: Int = 0,
        opponentRoundsWon: Int = 0,
        playerStyle: FightStyle = FightStyle.RUSH,
        opponentStyle: FightStyle = FightStyle.POWER,
        initialEnergy: Int = CombatConstants.INITIAL_ROUND_ENERGY
    ): CombatArenaSnapshot {
        return CombatArenaSnapshot(
            player = FighterRuntimeState(
                profile = playerProfile,
                isPlayerOne = true,
                fightStyle = playerStyle,
                x = -1.9f,
                facingRight = true,
                energy = initialEnergy
            ),
            opponent = FighterRuntimeState(
                profile = opponentProfile,
                isPlayerOne = false,
                fightStyle = opponentStyle,
                x = 1.9f,
                facingRight = false,
                energy = initialEnergy
            ),
            roundNumber = roundNumber,
            playerRoundsWon = playerRoundsWon,
            opponentRoundsWon = opponentRoundsWon,
            roundTimerSec = 99f,
            announcementBanner = "ROUND $roundNumber • FIGHT!",
            announcementTimerSec = 1.5f
        )
    }

    /**
     * Steps the entire combat simulation forward by [rawDeltaSec].
     * Returns the updated snapshot plus any audio events triggered this frame.
     */
    fun step(
        snapshot: CombatArenaSnapshot,
        playerInput: CombatInputState,
        opponentInput: CombatInputState,
        rawDeltaSec: Float,
        infiniteHealthPractice: Boolean = false,
        infiniteEnergyPractice: Boolean = false,
        disableRoundTimer: Boolean = false
    ): Pair<CombatArenaSnapshot, List<CombatAudioEvent>> {
        val dt = rawDeltaSec.coerceIn(0.001f, 0.05f)
        val audioEvents = mutableListOf<CombatAudioEvent>()

        var p1 = snapshot.player
        var p2 = snapshot.opponent

        if (infiniteEnergyPractice) {
            p1 = p1.copy(energy = CombatConstants.MAX_ENERGY)
        }
        if (infiniteHealthPractice) {
            p1 = p1.copy(health = p1.profile.maxHealth)
            p2 = p2.copy(health = p2.profile.maxHealth)
        }

        val nextFloating = snapshot.floatingTexts.mapNotNull {
            val aged = it.copy(ageSec = it.ageSec + dt, worldY = it.worldY + dt * 0.85f)
            if (aged.ageSec < aged.lifetimeSec) aged else null
        }.toMutableList()

        val nextParticles = snapshot.particles.mapNotNull {
            val aged = it.copy(
                ageSec = it.ageSec + dt,
                worldX = it.worldX + it.vx * dt,
                worldY = max(0f, it.worldY + it.vy * dt),
                vy = it.vy - 9.8f * dt
            )
            if (aged.ageSec < aged.lifetimeSec) aged else null
        }.toMutableList()

        val nextShockwaves = snapshot.shockwaves.mapNotNull {
            val aged = it.copy(ageSec = it.ageSec + dt)
            if (aged.ageSec < aged.lifetimeSec) aged else null
        }.toMutableList()

        var shake = max(0f, snapshot.screenShakeMagnitude - dt * 2.8f)
        val bannerTimer = max(0f, snapshot.announcementTimerSec - dt)
        val bannerText = if (bannerTimer <= 0f && !snapshot.isRoundOver) "" else snapshot.announcementBanner
        var cutInTimer = max(0f, snapshot.specialCutInTimerSec - dt)
        var cutInBanner = if (cutInTimer <= 0f) "" else snapshot.specialCutInBanner
        var cutInColor = snapshot.specialCutInColor

        if (snapshot.isRoundOver) {
            return snapshot.copy(
                announcementTimerSec = bannerTimer,
                specialCutInTimerSec = cutInTimer,
                specialCutInBanner = cutInBanner,
                screenShakeMagnitude = shake,
                floatingTexts = nextFloating,
                particles = nextParticles,
                shockwaves = nextShockwaves
            ) to audioEvents
        }

        var attempts = snapshot.playerAttempts
        var hitsLanded = snapshot.playerHitsLanded
        var totalDamage = snapshot.playerTotalDamageDealt
        var lastDamage = snapshot.playerLastHitDamage
        var maxCombo = snapshot.playerMaxCombo
        var specialsUsed = snapshot.playerSpecialsUsed
        var ultimatesUsed = snapshot.playerUltimatesUsed

        // Update facing directions when neutral
        val p1ShouldFaceRight = p1.x <= p2.x
        val p2ShouldFaceRight = p2.x < p1.x

        val p1Stepped = stepFighter(
            fighter = p1,
            opponentX = p2.x,
            shouldFaceRight = p1ShouldFaceRight,
            input = playerInput,
            dt = dt,
            audioEvents = audioEvents,
            floatingTexts = nextFloating,
            particles = nextParticles,
            shockwaves = nextShockwaves,
            onAttackStarted = { atk ->
                attempts++
                if (atk == AttackType.SPECIAL) {
                    specialsUsed++
                    cutInBanner = "${p1.profile.displayName.uppercase()} • ${p1.profile.specialAbility.name.uppercase()}!"
                    cutInColor = p1.profile.primaryColor
                    cutInTimer = 1.15f
                    shake = max(shake, 0.35f)
                }
                if (atk == AttackType.ULTIMATE) {
                    ultimatesUsed++
                    cutInBanner = "ULTIMATE OVERDRIVE • ${p1.profile.ultimateAbility.name.uppercase()}!!"
                    cutInColor = Color(0xFFFFD600)
                    cutInTimer = 1.55f
                    shake = max(shake, 0.72f)
                }
            }
        )
        p1 = p1Stepped

        val p2Stepped = stepFighter(
            fighter = p2,
            opponentX = p1.x,
            shouldFaceRight = p2ShouldFaceRight,
            input = opponentInput,
            dt = dt,
            audioEvents = audioEvents,
            floatingTexts = nextFloating,
            particles = nextParticles,
            shockwaves = nextShockwaves,
            onAttackStarted = { atk ->
                if (atk == AttackType.SPECIAL) {
                    cutInBanner = "${p2.profile.displayName.uppercase()} • ${p2.profile.specialAbility.name.uppercase()}!"
                    cutInColor = p2.profile.primaryColor
                    cutInTimer = 1.10f
                }
                if (atk == AttackType.ULTIMATE) {
                    cutInBanner = "ENEMY ULTIMATE • ${p2.profile.ultimateAbility.name.uppercase()}!!"
                    cutInColor = Color(0xFFFF1744)
                    cutInTimer = 1.45f
                }
            }
        )
        p2 = p2Stepped

        // Prevent character body overlap unless dodging in 2.5D Z-axis
        val minSeparation = 0.68f
        val dx = p2.x - p1.x
        if (abs(dx) < minSeparation && p1.y < 0.6f && p2.y < 0.6f &&
            p1.state != FighterState.DODGING && p2.state != FighterState.DODGING
        ) {
            val push = (minSeparation - abs(dx)) * 0.5f
            val dir = if (dx >= 0f) 1f else -1f
            p1 = p1.copy(x = (p1.x - dir * push).coerceIn(-CombatConstants.STAGE_HALF_WIDTH, CombatConstants.STAGE_HALF_WIDTH))
            p2 = p2.copy(x = (p2.x + dir * push).coerceIn(-CombatConstants.STAGE_HALF_WIDTH, CombatConstants.STAGE_HALF_WIDTH))
        }

        // Resolve active attack hits for P1 -> P2
        val p1HitResult = tryResolveAttackHit(
            attacker = p1,
            defender = p2,
            infiniteHealthDefender = infiniteHealthPractice,
            infiniteEnergyAttacker = infiniteEnergyPractice,
            audioEvents = audioEvents,
            floatingTexts = nextFloating,
            particles = nextParticles,
            shockwaves = nextShockwaves
        )
        p1 = p1HitResult.attacker
        p2 = p1HitResult.defender
        shake = max(shake, p1HitResult.addedShake)
        if (p1HitResult.landedDamage > 0) {
            hitsLanded++
            totalDamage += p1HitResult.landedDamage
            lastDamage = p1HitResult.landedDamage
            maxCombo = max(maxCombo, p1.comboCount)
        }

        // Resolve active attack hits for P2 -> P1
        val p2HitResult = tryResolveAttackHit(
            attacker = p2,
            defender = p1,
            infiniteHealthDefender = infiniteHealthPractice,
            infiniteEnergyAttacker = false,
            audioEvents = audioEvents,
            floatingTexts = nextFloating,
            particles = nextParticles,
            shockwaves = nextShockwaves
        )
        p2 = p2HitResult.attacker
        p1 = p2HitResult.defender
        shake = max(shake, p2HitResult.addedShake)

        // Round timer & Victory detection
        val nextRoundTimer = if (disableRoundTimer) 99f else max(0f, snapshot.roundTimerSec - dt)
        var roundOver = false
        var winnerIsPlayer: Boolean? = null
        var nextBanner = bannerText
        var nextBannerTimer = bannerTimer

        if (p1.health <= 0 || p2.health <= 0 || (!disableRoundTimer && nextRoundTimer <= 0f)) {
            roundOver = true
            winnerIsPlayer = when {
                p2.health <= 0 && p1.health > 0 -> true
                p1.health <= 0 && p2.health > 0 -> false
                else -> p1.health >= p2.health
            }
            if (winnerIsPlayer) {
                p1 = p1.copy(state = FighterState.VICTORY, currentAttack = null)
                if (p2.health <= 0) p2 = p2.copy(state = FighterState.DEFEATED, currentAttack = null)
                nextBanner = if (p2.health <= 0) "K.O.! ${p1.profile.displayName.uppercase()} WINS!" else "TIME OVER! ${p1.profile.displayName.uppercase()} WINS!"
            } else {
                p2 = p2.copy(state = FighterState.VICTORY, currentAttack = null)
                if (p1.health <= 0) p1 = p1.copy(state = FighterState.DEFEATED, currentAttack = null)
                nextBanner = if (p1.health <= 0) "DEFEAT! ${p2.profile.displayName.uppercase()} WINS!" else "TIME OVER! ${p2.profile.displayName.uppercase()} WINS!"
            }
            nextBannerTimer = 4.0f
            audioEvents.add(CombatAudioEvent.Knockout)
            shake = max(shake, 0.75f)
        }

        return snapshot.copy(
            player = p1,
            opponent = p2,
            roundTimerSec = nextRoundTimer,
            isRoundOver = roundOver,
            winnerIsPlayer = winnerIsPlayer,
            announcementBanner = nextBanner,
            announcementTimerSec = nextBannerTimer,
            specialCutInBanner = cutInBanner,
            specialCutInColor = cutInColor,
            specialCutInTimerSec = cutInTimer,
            screenShakeMagnitude = shake,
            floatingTexts = nextFloating,
            particles = nextParticles,
            shockwaves = nextShockwaves,
            playerAttempts = attempts,
            playerHitsLanded = hitsLanded,
            playerTotalDamageDealt = totalDamage,
            playerLastHitDamage = lastDamage,
            playerMaxCombo = maxCombo,
            playerSpecialsUsed = specialsUsed,
            playerUltimatesUsed = ultimatesUsed
        ) to audioEvents
    }

    private fun stepFighter(
        fighter: FighterRuntimeState,
        opponentX: Float,
        shouldFaceRight: Boolean,
        input: CombatInputState,
        dt: Float,
        audioEvents: MutableList<CombatAudioEvent>,
        floatingTexts: MutableList<FloatingDamageText>,
        particles: MutableList<CombatParticle>,
        shockwaves: MutableList<CombatShockwave>,
        onAttackStarted: (AttackType) -> Unit
    ): FighterRuntimeState {
        if (fighter.state == FighterState.DEFEATED || fighter.state == FighterState.VICTORY) {
            return fighter
        }

        // Continuous passive cyber-reactor energy charge so Special & Ultimate moves fill rapidly
        val passiveGain = (CombatConstants.PASSIVE_ENERGY_REGEN_PER_SEC * dt * fighter.fightStyle.energyGainMultiplier).toInt()
        val passiveEnergy = CombatCalculator.clampEnergy(fighter.energy + passiveGain)
        val targetAura = when {
            passiveEnergy >= CombatConstants.ULTIMATE_ENERGY_COST -> 1.0f
            passiveEnergy >= CombatConstants.SPECIAL_ENERGY_COST -> 0.65f
            else -> 0.25f
        }

        var f = fighter.copy(
            energy = passiveEnergy,
            stateTimerSec = fighter.stateTimerSec + dt,
            dodgeInvulnerableTimerSec = CombatCalculator.stepCooldown(fighter.dodgeInvulnerableTimerSec, dt),
            dodgeCooldownTimerSec = CombatCalculator.stepCooldown(fighter.dodgeCooldownTimerSec, dt),
            energySurgeCooldownSec = CombatCalculator.stepCooldown(fighter.energySurgeCooldownSec, dt),
            stunTimerSec = CombatCalculator.stepCooldown(fighter.stunTimerSec, dt),
            timeSinceLastLandedHitSec = fighter.timeSinceLastLandedHitSec + dt,
            squashStretchY = fighter.squashStretchY + (1f - fighter.squashStretchY) * min(1f, dt * 12f),
            hitLeanAngleDeg = fighter.hitLeanAngleDeg * max(0f, 1f - dt * 10f),
            flashAlpha = max(0f, fighter.flashAlpha - dt * 4.5f),
            auraIntensity = fighter.auraIntensity + (targetAura - fighter.auraIntensity) * min(1f, dt * 6f)
        )

        // Active Overdrive Energy Surge button (+260K Energy immediately!)
        if (input.energySurgePressed && f.canSurgeEnergy) {
            val surgedEnergy = CombatCalculator.addHitEnergy(
                f.energy,
                CombatConstants.ENERGY_SURGE_BURST,
                f.fightStyle.energyGainMultiplier
            )
            f = f.copy(
                energy = surgedEnergy,
                energySurgeCooldownSec = 1.6f,
                squashStretchY = 1.16f,
                auraIntensity = 1.0f
            )
            shockwaves.add(
                CombatShockwave(
                    worldX = f.x,
                    worldY = f.y + 0.9f,
                    color = Color(0xFFFFD600),
                    maxRadius = 1.55f
                )
            )
            floatingTexts.add(
                FloatingDamageText(
                    id = nextFloatingTextId++,
                    text = "+OVERDRIVE SURGE!",
                    subText = "${surgedEnergy / 1000}K / 1000K EN",
                    worldX = f.x,
                    worldY = f.y + 1.55f,
                    color = Color(0xFFFFD600)
                )
            )
            audioEvents.add(CombatAudioEvent.SpecialCast(f.profile.id))
        }

        // Combo timeout reset
        if (f.comboCount > 0 && CombatCalculator.isComboTimedOut(f.timeSinceLastLandedHitSec)) {
            f = f.copy(comboCount = 0, comboChain = emptyList(), lastComboLabel = "")
        }

        // Apply vertical gravity
        if (f.y > 0f || f.velocityY != 0f) {
            val nextVy = f.velocityY - CombatConstants.GRAVITY * dt
            val nextY = max(0f, f.y + f.velocityY * dt)
            val landed = f.y > 0f && nextY == 0f
            f = f.copy(
                y = nextY,
                velocityY = if (nextY == 0f) 0f else nextVy,
                squashStretchY = if (landed) 0.84f else f.squashStretchY
            )
        }

        val requestedAttack = when {
            input.ultimatePressed && f.canUseUltimate -> AttackType.ULTIMATE
            input.specialPressed && f.canUseSpecial -> AttackType.SPECIAL
            input.heavyPressed -> AttackType.HEAVY
            input.lightPressed -> AttackType.LIGHT
            else -> null
        }

        // Handle Stun / TakingDamage states
        if (f.state == FighterState.STUNNED || f.state == FighterState.TAKING_DAMAGE) {
            val nextX = (f.x + f.velocityX * dt).coerceIn(
                -CombatConstants.STAGE_HALF_WIDTH,
                CombatConstants.STAGE_HALF_WIDTH
            )
            val dampedVx = f.velocityX * max(0f, 1f - dt * 8f)
            if (f.stunTimerSec <= 0f) {
                f = f.copy(
                    x = nextX,
                    velocityX = 0f,
                    state = if (f.y > 0f) FighterState.JUMPING else FighterState.IDLE,
                    stateTimerSec = 0f
                )
            } else {
                return f.copy(x = nextX, velocityX = dampedVx)
            }
        }

        // Handle active Dodge state
        if (f.state == FighterState.DODGING) {
            val nextX = (f.x + f.velocityX * dt).coerceIn(
                -CombatConstants.STAGE_HALF_WIDTH,
                CombatConstants.STAGE_HALF_WIDTH
            )
            if (f.stateTimerSec >= CombatConstants.DODGE_INVULNERABILITY_SEC) {
                f = f.copy(
                    x = nextX,
                    zOffset = 0f,
                    velocityX = 0f,
                    state = FighterState.IDLE,
                    stateTimerSec = 0f
                )
            } else {
                return f.copy(x = nextX)
            }
        }

        // Handle active Attack / Special / Ultimate states
        val activeAttack = f.currentAttack
        if (activeAttack != null && (
                f.state == FighterState.ATTACKING ||
                    f.state == FighterState.SPECIAL_ATTACK ||
                    f.state == FighterState.ULTIMATE_ATTACK
                )
        ) {
            val elapsed = f.attackElapsedSec + dt
            val totalDuration = activeAttack.startupSec + activeAttack.activeSec + activeAttack.recoverySec

            // Buffer next attack during active/recovery frames
            val nextBuffer = if (requestedAttack != null && elapsed >= activeAttack.startupSec) {
                requestedAttack
            } else {
                f.bufferedAttack
            }

            // Apply forward momentum during startup/active frames of specials/ultimates
            val dashSpeed = when (activeAttack) {
                AttackType.SPECIAL -> f.profile.specialAbility.dashVelocity
                AttackType.ULTIMATE -> f.profile.ultimateAbility.dashVelocity
                AttackType.HEAVY -> 1.8f
                AttackType.LIGHT -> 0.9f
            } * f.fightStyle.speedMultiplier
            val dir = if (f.facingRight) 1f else -1f
            val moveDelta = if (elapsed <= activeAttack.startupSec + activeAttack.activeSec) {
                dir * dashSpeed * dt
            } else 0f

            val nextX = (f.x + moveDelta).coerceIn(
                -CombatConstants.STAGE_HALF_WIDTH,
                CombatConstants.STAGE_HALF_WIDTH
            )

            // Spawn continuous trail particles during Special & Ultimate animations
            if (activeAttack == AttackType.SPECIAL || activeAttack == AttackType.ULTIMATE) {
                particles.add(
                    CombatParticle(
                        worldX = nextX + dir * (0.4f + elapsed * 3.2f),
                        worldY = f.y + 0.85f + (random.nextFloat() - 0.5f) * 0.6f,
                        vx = dir * (2.5f + random.nextFloat() * 3f),
                        vy = (random.nextFloat() - 0.3f) * 2.5f,
                        color = if (activeAttack == AttackType.ULTIMATE) f.profile.accentColor else f.profile.primaryColor,
                        radius = if (activeAttack == AttackType.ULTIMATE) 6.5f else 4.5f,
                        lifetimeSec = 0.32f
                    )
                )
            }

            if (elapsed >= totalDuration) {
                val buffered = nextBuffer
                f = f.copy(
                    x = nextX,
                    state = FighterState.IDLE,
                    stateTimerSec = 0f,
                    currentAttack = null,
                    attackElapsedSec = 0f,
                    bufferedAttack = null
                )
                if (buffered != null) {
                    return startAttack(f, buffered, opponentX, audioEvents, shockwaves, onAttackStarted)
                }
            } else {
                return f.copy(
                    x = nextX,
                    attackElapsedSec = elapsed,
                    bufferedAttack = nextBuffer
                )
            }
        }

        // Neutral state transitions: Face opponent
        f = f.copy(facingRight = shouldFaceRight)

        // Dodge trigger
        if (input.dodgePressed && f.canDodge) {
            val dodgeDir = when {
                abs(input.moveAxis) > 0.2f -> if (input.moveAxis > 0f) 1f else -1f
                else -> if (f.facingRight) -1f else 1f // Default backstep evade
            }
            audioEvents.add(CombatAudioEvent.DodgeWhoosh)
            return f.copy(
                state = FighterState.DODGING,
                stateTimerSec = 0f,
                velocityX = dodgeDir * (f.profile.moveSpeed * f.fightStyle.speedMultiplier * 1.85f),
                zOffset = if (f.isPlayerOne) -0.45f else 0.45f,
                dodgeInvulnerableTimerSec = CombatConstants.DODGE_INVULNERABILITY_SEC,
                dodgeCooldownTimerSec = CombatConstants.DODGE_COOLDOWN_SEC,
                squashStretchY = 0.86f
            )
        }

        // Attack trigger
        if (requestedAttack != null) {
            return startAttack(f, requestedAttack, opponentX, audioEvents, shockwaves, onAttackStarted)
        }

        // Block trigger (grounded)
        if (input.blockHeld && f.y <= 0.05f) {
            return f.copy(
                state = FighterState.BLOCKING,
                velocityX = 0f
            )
        }

        // Jump trigger
        if (input.jumpPressed && f.y <= 0.01f) {
            f = f.copy(
                state = FighterState.JUMPING,
                velocityY = f.profile.jumpVelocity,
                squashStretchY = 1.18f
            )
        }

        // Horizontal movement
        val moveAxis = input.moveAxis.coerceIn(-1f, 1f)
        val vx = moveAxis * f.profile.moveSpeed * f.fightStyle.speedMultiplier
        val nextX = (f.x + vx * dt).coerceIn(
            -CombatConstants.STAGE_HALF_WIDTH,
            CombatConstants.STAGE_HALF_WIDTH
        )
        val nextState = when {
            f.y > 0.01f -> FighterState.JUMPING
            abs(moveAxis) > 0.08f -> FighterState.MOVING
            else -> FighterState.IDLE
        }

        return f.copy(
            x = nextX,
            velocityX = vx,
            state = nextState
        )
    }

    private fun startAttack(
        fighter: FighterRuntimeState,
        attackType: AttackType,
        opponentX: Float,
        audioEvents: MutableList<CombatAudioEvent>,
        shockwaves: MutableList<CombatShockwave>,
        onAttackStarted: (AttackType) -> Unit
    ): FighterRuntimeState {
        if (!fighter.isAlive) return fighter

        // Verify energy requirement for Special / Ultimate
        val cost = when (attackType) {
            AttackType.SPECIAL -> CombatConstants.SPECIAL_ENERGY_COST
            AttackType.ULTIMATE -> CombatConstants.ULTIMATE_ENERGY_COST
            else -> 0
        }
        if (!CombatCalculator.canPayEnergy(fighter.energy, cost)) {
            return fighter
        }

        var updatedEnergy = CombatCalculator.spendEnergy(fighter.energy, cost)
        // Normal light/heavy attacks build momentum energy even on swing!
        if (attackType == AttackType.LIGHT || attackType == AttackType.HEAVY) {
            updatedEnergy = CombatCalculator.addHitEnergy(
                updatedEnergy,
                CombatConstants.SWING_ENERGY_GAIN,
                fighter.fightStyle.energyGainMultiplier
            )
        }

        val newAttackId = nextAttackId++

        // Check teleport ability (e.g., Vex Shadow Step / Void Breaker)
        var targetX = fighter.x
        var facingRight = fighter.facingRight
        var invulnSec = fighter.dodgeInvulnerableTimerSec

        val ability = when (attackType) {
            AttackType.SPECIAL -> fighter.profile.specialAbility
            AttackType.ULTIMATE -> fighter.profile.ultimateAbility
            else -> null
        }

        if (ability != null) {
            invulnSec = max(invulnSec, ability.grantsInvulnerabilitySec)
            shockwaves.add(
                CombatShockwave(
                    worldX = fighter.x,
                    worldY = fighter.y + 0.9f,
                    color = if (attackType == AttackType.ULTIMATE) fighter.profile.accentColor else fighter.profile.primaryColor,
                    maxRadius = if (attackType == AttackType.ULTIMATE) 2.2f else 1.4f
                )
            )
            if (ability.teleportsBehind) {
                val behindOffset = if (opponentX >= fighter.x) 0.92f else -0.92f
                targetX = (opponentX + behindOffset).coerceIn(
                    -CombatConstants.STAGE_HALF_WIDTH + 0.3f,
                    CombatConstants.STAGE_HALF_WIDTH - 0.3f
                )
                facingRight = targetX <= opponentX
            }
        }

        when (attackType) {
            AttackType.LIGHT -> audioEvents.add(CombatAudioEvent.LightSwing)
            AttackType.HEAVY -> audioEvents.add(CombatAudioEvent.HeavySwing)
            AttackType.SPECIAL -> audioEvents.add(CombatAudioEvent.SpecialCast(fighter.profile.id))
            AttackType.ULTIMATE -> audioEvents.add(CombatAudioEvent.UltimateCast(fighter.profile.id))
        }

        onAttackStarted(attackType)

        val targetState = when (attackType) {
            AttackType.SPECIAL -> FighterState.SPECIAL_ATTACK
            AttackType.ULTIMATE -> FighterState.ULTIMATE_ATTACK
            else -> FighterState.ATTACKING
        }

        return fighter.copy(
            x = targetX,
            facingRight = facingRight,
            energy = updatedEnergy,
            state = targetState,
            stateTimerSec = 0f,
            currentAttack = attackType,
            attackElapsedSec = 0f,
            attackInstanceId = newAttackId,
            bufferedAttack = null,
            dodgeInvulnerableTimerSec = invulnSec,
            squashStretchY = if (attackType == AttackType.HEAVY || attackType == AttackType.ULTIMATE) 1.18f else 1.08f
        )
    }

    private data class HitStepResult(
        val attacker: FighterRuntimeState,
        val defender: FighterRuntimeState,
        val landedDamage: Int,
        val addedShake: Float
    )

    private fun tryResolveAttackHit(
        attacker: FighterRuntimeState,
        defender: FighterRuntimeState,
        infiniteHealthDefender: Boolean,
        infiniteEnergyAttacker: Boolean,
        audioEvents: MutableList<CombatAudioEvent>,
        floatingTexts: MutableList<FloatingDamageText>,
        particles: MutableList<CombatParticle>,
        shockwaves: MutableList<CombatShockwave>
    ): HitStepResult {
        val atk = attacker.currentAttack ?: return HitStepResult(attacker, defender, 0, 0f)
        if (!attacker.isAlive || !defender.isAlive) {
            return HitStepResult(attacker, defender, 0, 0f)
        }

        // Prevent duplicate hit registration from the same attack swing
        if (attacker.lastRegisteredAttackId == attacker.attackInstanceId) {
            return HitStepResult(attacker, defender, 0, 0f)
        }

        // Check active frame window
        val inActiveWindow = attacker.attackElapsedSec >= atk.startupSec &&
            attacker.attackElapsedSec <= (atk.startupSec + atk.activeSec)
        if (!inActiveWindow) {
            return HitStepResult(attacker, defender, 0, 0f)
        }

        // Check spatial range & direction
        val reach = when (atk) {
            AttackType.LIGHT -> attacker.profile.lightReach
            AttackType.HEAVY -> attacker.profile.heavyReach
            AttackType.SPECIAL -> attacker.profile.specialAbility.rangeMeters
            AttackType.ULTIMATE -> attacker.profile.ultimateAbility.rangeMeters
        }
        val dx = defender.x - attacker.x
        val facingCorrectly = (attacker.facingRight && dx >= -0.35f) || (!attacker.facingRight && dx <= 0.35f)
        val verticalOverlap = abs(attacker.y - defender.y) <= 1.55f
        if (!facingCorrectly || abs(dx) > reach || !verticalOverlap) {
            return HitStepResult(attacker, defender, 0, 0f)
        }

        // Mark this attack instance as registered so it can never hit twice
        var nextAttacker = attacker.copy(lastRegisteredAttackId = attacker.attackInstanceId)

        // Extend or reset combo chain within 0.95s window, capped at MAX_COMBO_HITS
        val withinChainWindow = CombatCalculator.isWithinComboChainWindow(attacker.timeSinceLastLandedHitSec)
        val updatedChain = if (withinChainWindow && attacker.comboCount < CombatConstants.MAX_COMBO_HITS) {
            attacker.comboChain + atk
        } else {
            listOf(atk)
        }

        val baseDmg = when (atk) {
            AttackType.LIGHT -> attacker.profile.lightDamage
            AttackType.HEAVY -> attacker.profile.heavyDamage
            AttackType.SPECIAL -> attacker.profile.specialAbility.baseDamage
            AttackType.ULTIMATE -> attacker.profile.ultimateAbility.baseDamage
        }

        val critRoll = random.nextInt(CombatConstants.BASIS_POINTS_DIVISOR)
        val totalDefenseBp = defender.profile.defenseBp + defender.fightStyle.defenseBonusBp
        val resolution = CombatCalculator.calculateDamage(
            baseDamage = baseDmg,
            chainHistory = updatedChain,
            attackerCritChanceBp = attacker.profile.critChanceBp,
            attackerCritMultiplierBp = attacker.profile.critMultiplierBp,
            defenderDefenseBp = totalDefenseBp,
            isBlocking = defender.isBlockingActive,
            isDodgeInvulnerable = defender.isDodgeInvulnerable,
            criticalRollBp = critRoll,
            styleDamageMultiplierBp = attacker.fightStyle.damageMultiplierBp
        )

        if (resolution.wasDodged) {
            floatingTexts.add(
                FloatingDamageText(
                    id = nextFloatingTextId++,
                    text = "PHANTOM EVADE!",
                    subText = "0.28s INVULNERABLE",
                    worldX = defender.x,
                    worldY = defender.y + 1.35f,
                    color = Color(0xFF00E5FF)
                )
            )
            return HitStepResult(nextAttacker, defender, 0, 0.08f)
        }

        val newComboCount = if (resolution.wasBlocked) {
            attacker.comboCount
        } else {
            updatedChain.size
        }

        // Accelerated Energy gain on hit (180,000 base * style multiplier for normal attacks)
        val gainedEnergy = if (atk == AttackType.LIGHT || atk == AttackType.HEAVY) {
            CombatConstants.HIT_ENERGY_GAIN
        } else 60_000
        val nextAttackerEnergy = if (infiniteEnergyAttacker) {
            CombatConstants.MAX_ENERGY
        } else {
            CombatCalculator.addHitEnergy(
                nextAttacker.energy,
                gainedEnergy,
                attacker.fightStyle.energyGainMultiplier
            )
        }

        nextAttacker = nextAttacker.copy(
            energy = nextAttackerEnergy,
            comboCount = newComboCount,
            comboChain = if (resolution.wasBlocked) attacker.comboChain else updatedChain,
            timeSinceLastLandedHitSec = if (resolution.wasBlocked) attacker.timeSinceLastLandedHitSec else 0f,
            lastComboLabel = resolution.comboChainLabel
        )

        val nextDefenderHealth = if (infiniteHealthDefender) {
            defender.profile.maxHealth
        } else {
            CombatCalculator.clampHealth(defender.health - resolution.finalDamage, defender.profile.maxHealth)
        }

        val nextDefenderEnergy = CombatCalculator.addHitEnergy(
            defender.energy,
            CombatConstants.DEFENDER_HIT_ENERGY_GAIN,
            defender.fightStyle.energyGainMultiplier
        )

        val pushDir = if (attacker.facingRight) 1f else -1f
        val baseKnockback = when (atk) {
            AttackType.LIGHT -> 0.9f
            AttackType.HEAVY -> 1.8f
            AttackType.SPECIAL -> attacker.profile.specialAbility.knockbackForce
            AttackType.ULTIMATE -> attacker.profile.ultimateAbility.knockbackForce
        }
        val antiInfiniteMultiplier = if (newComboCount >= CombatConstants.MAX_COMBO_HITS) 2.2f else 1.0f
        val knockbackVx = pushDir * baseKnockback * antiInfiniteMultiplier * (if (resolution.wasBlocked) 0.45f else 1.0f)

        val stunDuration = when {
            resolution.wasBlocked -> 0.12f
            atk == AttackType.ULTIMATE -> attacker.profile.ultimateAbility.stunDurationSec
            atk == AttackType.SPECIAL -> attacker.profile.specialAbility.stunDurationSec
            atk == AttackType.HEAVY -> 0.30f
            else -> 0.20f
        }

        val nextDefenderState = when {
            nextDefenderHealth <= 0 -> FighterState.DEFEATED
            resolution.wasBlocked -> FighterState.BLOCKING
            atk == AttackType.SPECIAL || atk == AttackType.ULTIMATE -> FighterState.STUNNED
            else -> FighterState.TAKING_DAMAGE
        }

        val nextDefender = defender.copy(
            health = nextDefenderHealth,
            energy = nextDefenderEnergy,
            state = nextDefenderState,
            stateTimerSec = 0f,
            currentAttack = if (resolution.wasBlocked) defender.currentAttack else null,
            velocityX = knockbackVx,
            stunTimerSec = max(defender.stunTimerSec, stunDuration),
            hitLeanAngleDeg = pushDir * (if (resolution.wasBlocked) 8f else 24f),
            squashStretchY = if (resolution.wasBlocked) 0.94f else 0.80f,
            flashAlpha = 1f
        )

        // Emit combat audio & VFX
        if (resolution.wasBlocked) {
            audioEvents.add(CombatAudioEvent.GuardImpact)
        } else {
            audioEvents.add(
                CombatAudioEvent.HitImpact(
                    isHeavy = atk != AttackType.LIGHT,
                    isCrit = resolution.wasCritical
                )
            )
        }

        val impactX = (attacker.x + defender.x) * 0.5f
        val impactY = defender.y + 0.95f
        val textColor = when {
            resolution.wasBlocked -> Color(0xFF90CAF9)
            resolution.wasCritical -> Color(0xFFFFD600)
            atk == AttackType.ULTIMATE -> Color(0xFFFF1744)
            atk == AttackType.SPECIAL -> attacker.profile.primaryColor
            else -> Color.White
        }

        val labelPrefix = when {
            resolution.wasBlocked -> "GUARD -65% • "
            resolution.wasCritical -> "CRIT! "
            else -> ""
        }
        floatingTexts.add(
            FloatingDamageText(
                id = nextFloatingTextId++,
                text = "$labelPrefix${resolution.finalDamage}",
                subText = resolution.comboChainLabel,
                worldX = defender.x,
                worldY = defender.y + 1.45f,
                color = textColor,
                isCrit = resolution.wasCritical
            )
        )

        val sparkCount = when (atk) {
            AttackType.ULTIMATE -> 28
            AttackType.SPECIAL -> 18
            AttackType.HEAVY -> 13
            AttackType.LIGHT -> 8
        }
        for (i in 0 until sparkCount) {
            val angle = (i.toFloat() / sparkCount.toFloat()) * (Math.PI.toFloat() * 2f)
            val speed = 1.6f + random.nextFloat() * 4.5f
            particles.add(
                CombatParticle(
                    worldX = impactX,
                    worldY = impactY,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed + 1.9f,
                    color = if (i % 2 == 0) attacker.profile.primaryColor else textColor,
                    radius = if (atk == AttackType.ULTIMATE) 6.5f else 4.5f
                )
            )
        }

        shockwaves.add(
            CombatShockwave(
                worldX = impactX,
                worldY = impactY,
                color = if (resolution.wasBlocked) Color(0xFF40C4FF) else attacker.profile.primaryColor,
                maxRadius = when (atk) {
                    AttackType.ULTIMATE -> 2.7f
                    AttackType.SPECIAL -> 1.8f
                    AttackType.HEAVY -> 1.2f
                    AttackType.LIGHT -> 0.75f
                }
            )
        )

        val shakeAmount = when {
            resolution.wasBlocked -> 0.14f
            atk == AttackType.ULTIMATE -> 0.90f
            atk == AttackType.SPECIAL -> 0.56f
            resolution.wasCritical -> 0.44f
            atk == AttackType.HEAVY -> 0.32f
            else -> 0.16f
        }

        return HitStepResult(nextAttacker, nextDefender, resolution.finalDamage, shakeAmount)
    }
}
