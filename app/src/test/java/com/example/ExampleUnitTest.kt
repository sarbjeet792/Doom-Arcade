package com.example

import com.example.ai.AiController
import com.example.combat.CombatCalculator
import com.example.combat.CombatConstants
import com.example.combat.CombatEngine
import com.example.combat.CombatInputState
import com.example.combat.FighterState
import com.example.managers.ProgressionCalculator
import com.example.managers.SaveSerializer
import com.example.managers.TournamentManager
import com.example.model.AiDifficulty
import com.example.model.AttackType
import com.example.model.DummyBehavior
import com.example.model.FightStyle
import com.example.model.GameCatalog
import com.example.model.GameMode
import com.example.model.PlayerSaveData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify P2 combo multipliers L-H 1_25x, H-L 1_15x, L-L 1_10x, L-L-H 1_40x`() {
        assertEquals(
            12_500,
            CombatCalculator.resolveComboMultiplierBp(listOf(AttackType.LIGHT, AttackType.HEAVY)).first
        )
        assertEquals(
            11_500,
            CombatCalculator.resolveComboMultiplierBp(listOf(AttackType.HEAVY, AttackType.LIGHT)).first
        )
        assertEquals(
            11_000,
            CombatCalculator.resolveComboMultiplierBp(listOf(AttackType.LIGHT, AttackType.LIGHT)).first
        )
        assertEquals(
            14_000,
            CombatCalculator.resolveComboMultiplierBp(
                listOf(AttackType.LIGHT, AttackType.LIGHT, AttackType.HEAVY)
            ).first
        )
    }

    @Test
    fun `verify P2 blocking reduces damage by 65 percent and dodge invulnerability negates damage`() {
        val unblocked = CombatCalculator.calculateDamage(
            baseDamage = 1000,
            chainHistory = listOf(AttackType.LIGHT),
            attackerCritChanceBp = 0,
            attackerCritMultiplierBp = 15_000,
            defenderDefenseBp = 0,
            isBlocking = false,
            isDodgeInvulnerable = false,
            criticalRollBp = 9999
        )
        assertEquals(1000, unblocked.finalDamage)

        val blocked = CombatCalculator.calculateDamage(
            baseDamage = 1000,
            chainHistory = listOf(AttackType.LIGHT),
            attackerCritChanceBp = 0,
            attackerCritMultiplierBp = 15_000,
            defenderDefenseBp = 0,
            isBlocking = true,
            isDodgeInvulnerable = false,
            criticalRollBp = 9999
        )
        assertEquals(350, blocked.finalDamage)
        assertTrue(blocked.wasBlocked)

        val dodged = CombatCalculator.calculateDamage(
            baseDamage = 1000,
            chainHistory = listOf(AttackType.HEAVY),
            attackerCritChanceBp = 10_000,
            attackerCritMultiplierBp = 20_000,
            defenderDefenseBp = 0,
            isBlocking = false,
            isDodgeInvulnerable = true,
            criticalRollBp = 0
        )
        assertEquals(0, dodged.finalDamage)
        assertTrue(dodged.wasDodged)
    }

    @Test
    fun `verify accelerated energy fill economy and FightStyle multipliers`() {
        var energy = CombatConstants.INITIAL_ROUND_ENERGY
        assertEquals(150_000, energy)

        // Single hit with RUSH style (1.35x of 180k = +243k -> 393k >= 300k Special ready!)
        energy = CombatCalculator.addHitEnergy(
            energy,
            CombatConstants.HIT_ENERGY_GAIN,
            FightStyle.RUSH.energyGainMultiplier
        )
        assertTrue(energy >= CombatConstants.SPECIAL_ENERGY_COST)
        assertTrue(CombatCalculator.canPayEnergy(energy, CombatConstants.SPECIAL_ENERGY_COST))

        // Spend Special (300k)
        energy = CombatCalculator.spendEnergy(energy, CombatConstants.SPECIAL_ENERGY_COST)
        assertTrue(energy < CombatConstants.SPECIAL_ENERGY_COST)

        // Fill rapidly to 1M Ultimate cap
        repeat(4) {
            energy = CombatCalculator.addHitEnergy(
                energy,
                CombatConstants.ENERGY_SURGE_BURST,
                FightStyle.RUSH.energyGainMultiplier
            )
        }
        assertEquals(CombatConstants.MAX_ENERGY, energy)
        assertTrue(CombatCalculator.canPayEnergy(energy, CombatConstants.ULTIMATE_ENERGY_COST))
        energy = CombatCalculator.spendEnergy(energy, CombatConstants.ULTIMATE_ENERGY_COST)
        assertEquals(0, energy)
    }

    @Test
    fun `verify combat engine prevents double hit registration, surges energy, and animates special cut-ins`() {
        val engine = CombatEngine()
        val kairo = GameCatalog.fighterById("kairo")
        val vex = GameCatalog.fighterById("vex")

        var snap = engine.createInitialSnapshot(
            playerProfile = kairo,
            opponentProfile = vex,
            initialEnergy = 0
        )
        snap = snap.copy(
            player = snap.player.copy(x = -0.5f),
            opponent = snap.opponent.copy(x = 0.5f)
        )

        // Trigger light attack
        snap = engine.step(snap, CombatInputState(lightPressed = true), CombatInputState(), 0.02f).first
        assertEquals(FighterState.ATTACKING, snap.player.state)

        // Step through startup + active + recovery frames and ensure opponent is only hit ONCE
        var hitFrames = 0
        val initialOppHp = snap.opponent.health
        repeat(20) {
            val prevHp = snap.opponent.health
            snap = engine.step(snap, CombatInputState(), CombatInputState(), 0.02f).first
            if (snap.opponent.health < prevHp) {
                hitFrames++
            }
        }
        assertEquals(1, hitFrames)
        assertTrue(snap.opponent.health < initialOppHp)
        // Energy should exceed 300k after swing + hit + passive regen with RUSH style!
        assertTrue(snap.player.energy >= CombatConstants.SPECIAL_ENERGY_COST)

        // Trigger Special Attack and verify cut-in banner & SPECIAL_ATTACK state
        snap = engine.step(snap, CombatInputState(specialPressed = true), CombatInputState(), 0.02f).first
        assertEquals(FighterState.SPECIAL_ATTACK, snap.player.state)
        assertTrue(snap.specialCutInBanner.contains("AZURE BURST"))
        assertTrue(snap.specialCutInTimerSec > 0f)
    }

    @Test
    fun `verify P5 XP progression and P6 3-arcade-win Sky Temple unlock`() {
        var save = PlayerSaveData()
        assertFalse("sky_temple" in save.unlockedArenaIds)

        save = ProgressionCalculator.applyMatchResult(save, playerWon = true, completedArcadeLadder = true)
        assertEquals(1, save.level)
        assertEquals(220, save.xp)
        assertEquals(1, save.arcadeWins)

        save = ProgressionCalculator.applyMatchResult(save, playerWon = true, completedArcadeLadder = true)
        assertEquals(2, save.level)
        assertEquals(190, save.xp)
        assertEquals(2, save.arcadeWins)
        assertFalse("sky_temple" in save.unlockedArenaIds)

        save = ProgressionCalculator.applyMatchResult(save, playerWon = true, completedArcadeLadder = true)
        assertEquals(3, save.arcadeWins)
        assertTrue("sky_temple" in save.unlockedArenaIds)
    }

    @Test
    fun `verify SaveSerializer v1 migration and corruption handling`() {
        val v1Json = """
            {
              "schemaVersion": 1,
              "playerName": "VETERAN",
              "xp": 120,
              "level": 3,
              "wins": 9,
              "arcadeWins": 3
            }
        """.trimIndent()

        val migrated = SaveSerializer.fromJsonString(v1Json).getOrThrow()
        assertEquals(2, migrated.schemaVersion)
        assertEquals(9, migrated.totalWins)
        assertTrue("sky_temple" in migrated.unlockedArenaIds)
        assertTrue("nyx" in migrated.unlockedFighterIds)
        assertNotNull(migrated.lastRecoveryNote)

        val corrupted = SaveSerializer.fromJsonString("{broken_json:::")
        assertTrue(corrupted.isFailure)
    }

    @Test
    fun `verify AI controller difficulties and Tournament bracket progression`() {
        val ai = AiController()
        val engine = CombatEngine()
        val snap = engine.createInitialSnapshot(
            GameCatalog.fighterById("solara"),
            GameCatalog.fighterById("bront")
        )

        AiDifficulty.entries.forEach { diff ->
            val input = ai.computeOpponentInput(snap, GameMode.VERSUS, diff, DummyBehavior.IDLE, 0.1f)
            assertNotNull(input)
        }

        var bracket = TournamentManager.createBracket("kairo", "neon_district", AiDifficulty.HARD)
        assertTrue(bracket.isActive)
        assertEquals(2, bracket.semifinals.size)

        bracket = TournamentManager.recordPlayerMatchOutcome(bracket, playerWon = true)
        assertEquals(1, bracket.currentRoundIndex)
        assertNotNull(bracket.grandFinal)

        bracket = TournamentManager.recordPlayerMatchOutcome(bracket, playerWon = true)
        assertFalse(bracket.isActive)
        assertEquals("kairo", bracket.championFighterId)
    }
}
