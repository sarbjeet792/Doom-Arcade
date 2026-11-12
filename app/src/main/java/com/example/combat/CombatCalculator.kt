package com.example.combat

import com.example.model.AttackType
import kotlin.math.max

object CombatConstants {
    const val BASIS_POINTS_DIVISOR = 10_000
    const val BLOCK_DAMAGE_REDUCTION_BP = 6_500 // 65% damage reduction when blocking
    const val DODGE_INVULNERABILITY_SEC = 0.28f
    const val DODGE_COOLDOWN_SEC = 1.10f

    // Accelerated Energy Economy for rapid Special (300k) and Ultimate (1M) execution
    const val MAX_ENERGY = 1_000_000
    const val INITIAL_ROUND_ENERGY = 150_000
    const val HIT_ENERGY_GAIN = 180_000
    const val SWING_ENERGY_GAIN = 35_000
    const val DEFENDER_HIT_ENERGY_GAIN = 95_000
    const val PASSIVE_ENERGY_REGEN_PER_SEC = 48_000
    const val ENERGY_SURGE_BURST = 260_000
    const val SPECIAL_ENERGY_COST = 300_000
    const val ULTIMATE_ENERGY_COST = 1_000_000

    const val COMBO_CHAIN_WINDOW_SEC = 0.95f
    const val COMBO_TIMEOUT_SEC = 1.15f
    const val MAX_COMBO_HITS = 8 // Hard cap to prevent infinite combo chains

    // Exact P2 Combo Multipliers in Basis Points (10,000 = 1.00x)
    const val MULT_BASE_BP = 10_000
    const val MULT_LIGHT_LIGHT_BP = 11_000        // 1.10x
    const val MULT_HEAVY_LIGHT_BP = 11_500        // 1.15x
    const val MULT_LIGHT_HEAVY_BP = 12_500        // 1.25x
    const val MULT_LIGHT_LIGHT_HEAVY_BP = 14_000  // 1.40x

    const val STAGE_HALF_WIDTH = 4.2f
    const val GRAVITY = 22.0f
}

data class DamageResolution(
    val finalDamage: Int,
    val wasBlocked: Boolean,
    val wasDodged: Boolean,
    val wasCritical: Boolean,
    val comboMultiplierBp: Int,
    val comboChainLabel: String
)

object CombatCalculator {

    /**
     * Resolves the exact P2 combo multiplier in basis points (10,000 = 1.00x)
     * based on the sequence of recent attacks within the combo chain window.
     */
    fun resolveComboMultiplierBp(chainHistory: List<AttackType>): Pair<Int, String> {
        if (chainHistory.size < 2) {
            return CombatConstants.MULT_BASE_BP to ""
        }
        val lastThree = chainHistory.takeLast(3)
        if (lastThree.size == 3 &&
            lastThree[0] == AttackType.LIGHT &&
            lastThree[1] == AttackType.LIGHT &&
            lastThree[2] == AttackType.HEAVY
        ) {
            return CombatConstants.MULT_LIGHT_LIGHT_HEAVY_BP to "L → L → H (1.40x)"
        }

        val prev = chainHistory[chainHistory.size - 2]
        val curr = chainHistory.last()
        return when {
            prev == AttackType.LIGHT && curr == AttackType.HEAVY ->
                CombatConstants.MULT_LIGHT_HEAVY_BP to "L → H (1.25x)"
            prev == AttackType.HEAVY && curr == AttackType.LIGHT ->
                CombatConstants.MULT_HEAVY_LIGHT_BP to "H → L (1.15x)"
            prev == AttackType.LIGHT && curr == AttackType.LIGHT ->
                CombatConstants.MULT_LIGHT_LIGHT_BP to "L → L (1.10x)"
            curr == AttackType.SPECIAL ->
                13_000 to "CANCEL → SPECIAL (1.30x)"
            curr == AttackType.ULTIMATE ->
                15_000 to "CANCEL → ULTIMATE (1.50x)"
            else -> CombatConstants.MULT_BASE_BP to "${prev.code} → ${curr.code}"
        }
    }

    /**
     * Deterministic basis-point damage calculation.
     */
    fun calculateDamage(
        baseDamage: Int,
        chainHistory: List<AttackType>,
        attackerCritChanceBp: Int,
        attackerCritMultiplierBp: Int,
        defenderDefenseBp: Int,
        isBlocking: Boolean,
        isDodgeInvulnerable: Boolean,
        criticalRollBp: Int,
        styleDamageMultiplierBp: Int = CombatConstants.BASIS_POINTS_DIVISOR
    ): DamageResolution {
        if (isDodgeInvulnerable || baseDamage <= 0) {
            return DamageResolution(
                finalDamage = 0,
                wasBlocked = false,
                wasDodged = isDodgeInvulnerable,
                wasCritical = false,
                comboMultiplierBp = CombatConstants.MULT_BASE_BP,
                comboChainLabel = if (isDodgeInvulnerable) "EVADED" else ""
            )
        }

        val styledBase = (baseDamage.toLong() * styleDamageMultiplierBp) / CombatConstants.BASIS_POINTS_DIVISOR
        val (comboBp, comboLabel) = resolveComboMultiplierBp(chainHistory)
        val afterCombo = (styledBase * comboBp) / CombatConstants.BASIS_POINTS_DIVISOR

        // Critical hits cannot occur through a successful guard
        val isCrit = !isBlocking && (criticalRollBp in 0 until attackerCritChanceBp)
        val afterCrit = if (isCrit) {
            (afterCombo * attackerCritMultiplierBp) / CombatConstants.BASIS_POINTS_DIVISOR
        } else {
            afterCombo
        }

        val clampedDefenseBp = defenderDefenseBp.coerceIn(0, 8_000)
        val afterDefense = (afterCrit * (CombatConstants.BASIS_POINTS_DIVISOR - clampedDefenseBp)) /
            CombatConstants.BASIS_POINTS_DIVISOR

        val afterBlock = if (isBlocking) {
            val remainingRatioBp = CombatConstants.BASIS_POINTS_DIVISOR - CombatConstants.BLOCK_DAMAGE_REDUCTION_BP
            (afterDefense * remainingRatioBp) / CombatConstants.BASIS_POINTS_DIVISOR
        } else {
            afterDefense
        }

        val finalDmg = max(1L, afterBlock).toInt()
        return DamageResolution(
            finalDamage = finalDmg,
            wasBlocked = isBlocking,
            wasDodged = false,
            wasCritical = isCrit,
            comboMultiplierBp = comboBp,
            comboChainLabel = comboLabel
        )
    }

    fun clampHealth(currentHealth: Int, maxHealth: Int): Int =
        currentHealth.coerceIn(0, max(1, maxHealth))

    fun clampEnergy(currentEnergy: Int): Int =
        currentEnergy.coerceIn(0, CombatConstants.MAX_ENERGY)

    fun addHitEnergy(
        currentEnergy: Int,
        gain: Int = CombatConstants.HIT_ENERGY_GAIN,
        multiplier: Float = 1.0f
    ): Int {
        val scaledGain = (max(0, gain) * multiplier.coerceAtLeast(0.1f)).toInt()
        return clampEnergy(currentEnergy + scaledGain)
    }

    fun canPayEnergy(currentEnergy: Int, cost: Int): Boolean =
        cost >= 0 && currentEnergy >= cost

    fun spendEnergy(currentEnergy: Int, cost: Int): Int {
        if (!canPayEnergy(currentEnergy, cost)) return clampEnergy(currentEnergy)
        return clampEnergy(currentEnergy - cost)
    }

    fun stepCooldown(currentSec: Float, deltaSec: Float): Float =
        max(0f, currentSec - max(0f, deltaSec))

    fun isWithinComboChainWindow(elapsedSinceHitSec: Float): Boolean =
        elapsedSinceHitSec >= 0f && elapsedSinceHitSec <= CombatConstants.COMBO_CHAIN_WINDOW_SEC

    fun isComboTimedOut(elapsedSinceHitSec: Float): Boolean =
        elapsedSinceHitSec > CombatConstants.COMBO_TIMEOUT_SEC
}
