package com.example.ai

import com.example.combat.CombatArenaSnapshot
import com.example.combat.CombatInputState
import com.example.combat.FighterState
import com.example.model.AiDifficulty
import com.example.model.AttackType
import com.example.model.DummyBehavior
import com.example.model.GameMode
import kotlin.math.abs
import kotlin.random.Random

class AiController(
    private val random: Random = Random(1337)
) {
    private var decisionCooldownSec: Float = 0f
    private var cachedInput: CombatInputState = CombatInputState()

    fun reset() {
        decisionCooldownSec = 0f
        cachedInput = CombatInputState()
    }

    fun computeOpponentInput(
        snapshot: CombatArenaSnapshot,
        mode: GameMode,
        difficulty: AiDifficulty,
        dummyBehavior: DummyBehavior,
        dt: Float
    ): CombatInputState {
        val ai = snapshot.opponent
        val player = snapshot.player

        if (!ai.isAlive || snapshot.isRoundOver) {
            return CombatInputState()
        }

        // Practice Mode Training Dummy Logic
        if (mode == GameMode.PRACTICE && dummyBehavior != DummyBehavior.AI_SPARRING) {
            return when (dummyBehavior) {
                DummyBehavior.IDLE -> CombatInputState()
                DummyBehavior.BLOCK_ALL -> CombatInputState(blockHeld = true)
                DummyBehavior.DODGE_REACTION -> {
                    val playerAttacking = player.currentAttack != null
                    CombatInputState(
                        dodgePressed = playerAttacking && ai.canDodge && abs(player.x - ai.x) < 2.6f
                    )
                }
                DummyBehavior.COUNTER_ATTACK -> {
                    val justRecovered = ai.state == FighterState.IDLE &&
                        player.currentAttack != null &&
                        player.attackElapsedSec > player.currentAttack.startupSec + player.currentAttack.activeSec
                    CombatInputState(
                        lightPressed = justRecovered && abs(player.x - ai.x) <= ai.profile.lightReach
                    )
                }
                DummyBehavior.AI_SPARRING -> CombatInputState()
            }
        }

        decisionCooldownSec -= dt
        if (decisionCooldownSec > 0f) {
            // Clear one-shot button presses while holding movement/block state
            return cachedInput.copy(
                jumpPressed = false,
                dodgePressed = false,
                lightPressed = false,
                heavyPressed = false,
                specialPressed = false,
                ultimatePressed = false
            )
        }

        decisionCooldownSec = difficulty.reactionDelaySec * (0.75f + random.nextFloat() * 0.45f)

        val dx = player.x - ai.x
        val absDist = abs(dx)
        val dirToPlayer = if (dx >= 0f) 1f else -1f

        val playerIsAttacking = player.currentAttack != null &&
            player.attackElapsedSec <= (player.currentAttack.startupSec + player.currentAttack.activeSec)
        val playerInRecovery = player.currentAttack != null &&
            player.attackElapsedSec > (player.currentAttack.startupSec + player.currentAttack.activeSec)

        // 1. Defensive Reaction when player attacks in range
        if (playerIsAttacking && absDist < 2.6f) {
            val roll = random.nextFloat()
            if (roll < difficulty.defenseChance) {
                // Hard/Expert AI mixes between Dodge and Block
                if (ai.canDodge && (difficulty == AiDifficulty.HARD || difficulty == AiDifficulty.EXPERT) && random.nextFloat() < 0.48f) {
                    cachedInput = CombatInputState(
                        moveAxis = -dirToPlayer.toFloat(),
                        dodgePressed = true
                    )
                    return cachedInput
                }
                cachedInput = CombatInputState(blockHeld = true)
                return cachedInput
            }
        }

        // 2. Resource Awareness: Ultimate & Special Usage
        if (ai.canUseUltimate && absDist <= ai.profile.ultimateAbility.rangeMeters) {
            if (random.nextFloat() < difficulty.specialUsageWeight) {
                cachedInput = CombatInputState(ultimatePressed = true)
                return cachedInput
            }
        }

        if (ai.canUseSpecial && absDist <= ai.profile.specialAbility.rangeMeters) {
            if (random.nextFloat() < difficulty.specialUsageWeight * 0.85f) {
                cachedInput = CombatInputState(specialPressed = true)
                return cachedInput
            }
        }

        // 3. Combo Continuation & Whiff Punishment
        if (ai.comboCount > 0 && absDist <= ai.profile.heavyReach) {
            if (random.nextFloat() < difficulty.comboProbability) {
                val followWithHeavy = ai.comboChain.size >= 2 ||
                    (ai.comboChain.lastOrNull() == AttackType.LIGHT && random.nextBoolean())
                cachedInput = CombatInputState(
                    moveAxis = dirToPlayer * 0.4f,
                    lightPressed = !followWithHeavy,
                    heavyPressed = followWithHeavy
                )
                return cachedInput
            }
        }

        if (playerInRecovery && absDist <= ai.profile.heavyReach && difficulty != AiDifficulty.EASY) {
            cachedInput = CombatInputState(
                moveAxis = dirToPlayer * 0.5f,
                heavyPressed = true
            )
            return cachedInput
        }

        // 4. Neutral Spacing & Approach
        val optimalRange = ai.profile.lightReach * 0.92f
        if (absDist > optimalRange) {
            val jumpApproach = absDist > 2.8f && random.nextFloat() < (difficulty.aggressionWeight * 0.18f)
            cachedInput = CombatInputState(
                moveAxis = dirToPlayer.toFloat(),
                jumpPressed = jumpApproach
            )
            return cachedInput
        }

        // 5. Close-range Offense
        if (random.nextFloat() < difficulty.aggressionWeight) {
            val useHeavy = random.nextFloat() < 0.35f
            cachedInput = CombatInputState(
                moveAxis = dirToPlayer * 0.25f,
                lightPressed = !useHeavy,
                heavyPressed = useHeavy
            )
            return cachedInput
        }

        // Occasional micro-spacing backstep on Hard/Expert
        val microSpace = if (difficulty == AiDifficulty.EXPERT || difficulty == AiDifficulty.HARD) {
            -dirToPlayer * 0.5f
        } else 0f
        cachedInput = CombatInputState(moveAxis = microSpace)
        return cachedInput
    }
}
