package com.example.model

import androidx.compose.ui.graphics.Color

enum class GameMode(val displayName: String, val subtitle: String) {
    ARCADE("Arcade Ladder", "5-stage escalating gauntlet • 3 wins unlock Sky Temple"),
    VERSUS("Versus Battle", "Custom 1v1 or Best-of-Three series against CPU"),
    PRACTICE("Practice Dojo", "Frame data, combo accuracy telemetry & dummy AI config"),
    TOURNAMENT("Grand Tournament", "8-fighter single-elimination championship bracket")
}

enum class AiDifficulty(
    val displayName: String,
    val reactionDelaySec: Float,
    val aggressionWeight: Float,
    val defenseChance: Float,
    val comboProbability: Float,
    val specialUsageWeight: Float
) {
    EASY("Easy", 0.52f, 0.32f, 0.15f, 0.15f, 0.20f),
    NORMAL("Normal", 0.30f, 0.55f, 0.38f, 0.45f, 0.50f),
    HARD("Hard", 0.16f, 0.76f, 0.65f, 0.75f, 0.80f),
    EXPERT("Expert", 0.07f, 0.92f, 0.86f, 0.94f, 0.96f)
}

enum class GraphicsPreset(
    val displayName: String,
    val particleMultiplier: Float,
    val enableReflections: Boolean,
    val enableDynamicGlow: Boolean,
    val shadowDetail: Int
) {
    LOW("Low", 0.35f, false, false, 0),
    MEDIUM("Medium", 0.70f, true, false, 1),
    HIGH("High", 1.0f, true, true, 2),
    ULTRA("Ultra", 1.5f, true, true, 3)
}

enum class DummyBehavior(val displayName: String) {
    IDLE("Stand Idle"),
    BLOCK_ALL("Always Guard (65% Red.)"),
    DODGE_REACTION("Evade On Startup"),
    COUNTER_ATTACK("Punish After Hit"),
    AI_SPARRING("Active CPU Sparring")
}

enum class FightStyle(
    val displayName: String,
    val badge: String,
    val description: String,
    val speedMultiplier: Float,
    val damageMultiplierBp: Int,
    val energyGainMultiplier: Float,
    val defenseBonusBp: Int
) {
    RUSH(
        displayName = "Rush Overdrive",
        badge = "⚡ RUSH",
        description = "+18% Move Speed & +35% faster Special/Ultimate meter charge",
        speedMultiplier = 1.18f,
        damageMultiplierBp = 10_000,
        energyGainMultiplier = 1.35f,
        defenseBonusBp = 0
    ),
    POWER(
        displayName = "Power Breaker",
        badge = "🔥 POWER",
        description = "+22% Strike & Ultimate Damage with heavy impact knockback",
        speedMultiplier = 1.0f,
        damageMultiplierBp = 12_200,
        energyGainMultiplier = 1.15f,
        defenseBonusBp = 0
    ),
    AEGIS(
        displayName = "Aegis Counter",
        badge = "🛡️ AEGIS",
        description = "+12% Passive Armor Defense & rapid guard energy conversion",
        speedMultiplier = 1.04f,
        damageMultiplierBp = 10_500,
        energyGainMultiplier = 1.20f,
        defenseBonusBp = 1_200
    )
}

enum class AttackType(
    val code: String,
    val energyCost: Int,
    val startupSec: Float,
    val activeSec: Float,
    val recoverySec: Float
) {
    LIGHT("L", 0, 0.09f, 0.12f, 0.16f),
    HEAVY("H", 0, 0.18f, 0.16f, 0.28f),
    SPECIAL("S", 300_000, 0.14f, 0.24f, 0.24f),
    ULTIMATE("U", 1_000_000, 0.20f, 0.38f, 0.32f)
}

enum class AbilityArchetype {
    PLASMA_WAVE,      // Kairo Special: Azure Burst
    OMNI_SLASH_BEAM,  // Kairo Ultimate: Azure Final Strike
    SHADOW_TELEPORT,  // Vex Special: Shadow Step
    VOID_SINGULARITY, // Vex Ultimate: Void Breaker
    SOLAR_PILLAR,     // Solara Special: Solar Flare
    METEOR_SUPERNOVA, // Solara Ultimate: Supernova Judgment
    ARMORED_RUSH,     // Bront Special: Seismic Charge
    TECTONIC_QUAKE,   // Bront Ultimate: Tectonic Cataclysm
    CHRONO_SLICE,     // Nyx Special: Chrono Rift
    ECLIPSE_STORM     // Nyx Ultimate: Eclipse Oblivion
}

data class AbilityDefinition(
    val id: String,
    val name: String,
    val description: String,
    val energyCost: Int,
    val baseDamage: Int,
    val rangeMeters: Float,
    val dashVelocity: Float,
    val teleportsBehind: Boolean,
    val grantsInvulnerabilitySec: Float,
    val stunDurationSec: Float,
    val knockbackForce: Float,
    val archetype: AbilityArchetype
)

data class FighterProfile(
    val id: String,
    val displayName: String,
    val title: String,
    val originArenaId: String,
    val description: String,
    val fightingStyle: String,
    val maxHealth: Int,          // Integer HP (e.g. 10,000)
    val maxEnergy: Int = 1_000_000,
    val moveSpeed: Float,        // Meters/sec in arena
    val jumpVelocity: Float,
    val lightDamage: Int,
    val heavyDamage: Int,
    val defenseBp: Int,          // Basis points (e.g. 1000 = 10% passive reduction)
    val critChanceBp: Int,       // Basis points (e.g. 1200 = 12.0%)
    val critMultiplierBp: Int,   // Basis points (e.g. 15000 = 1.50x)
    val lightReach: Float,
    val heavyReach: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val specialAbility: AbilityDefinition,
    val ultimateAbility: AbilityDefinition,
    val unlockedByDefault: Boolean = true,
    val unlockRequirementText: String = "Unlocked by default"
)

data class ArenaProfile(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val description: String,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val floorColor: Color,
    val gridNeonColor: Color,
    val hazardAccentColor: Color,
    val ambientWeather: String,
    val unlockedByDefault: Boolean = true,
    val requiredArcadeWins: Int = 0
)

data class PlayerSettings(
    val graphicsPreset: GraphicsPreset = GraphicsPreset.HIGH,
    val fpsCap: Int = 60,
    val masterVolume: Float = 0.85f,
    val sfxVolume: Float = 0.90f,
    val bgmVolume: Float = 0.65f,
    val fullscreen: Boolean = true,
    val screenShake: Boolean = true,
    val showDamageNumbers: Boolean = true,
    val reducedFlashes: Boolean = false,
    val controlScale: Float = 1.0f,
    val bestOfThreeVersus: Boolean = true
)

data class PracticeStats(
    val attempts: Int = 0,
    val hitsLanded: Int = 0,
    val totalDamageDealt: Int = 0,
    val lastHitDamage: Int = 0,
    val currentCombo: Int = 0,
    val maxCombo: Int = 0,
    val specialUses: Int = 0,
    val ultimateUses: Int = 0
) {
    val accuracyPercent: Float
        get() = if (attempts <= 0) 0f else ((hitsLanded.toFloat() / attempts.toFloat()) * 100f).coerceIn(0f, 100f)
}

data class PlayerSaveData(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val playerName: String = "ARCADE_ONE",
    val xp: Int = 0,
    val level: Int = 1,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val arcadeWins: Int = 0,
    val tournamentWins: Int = 0,
    val highestComboRecord: Int = 0,
    val unlockedFighterIds: Set<String> = setOf("kairo", "vex", "solara", "bront"),
    val unlockedArenaIds: Set<String> = setOf("neon_district", "ember_temple", "frost_forge"),
    val settings: PlayerSettings = PlayerSettings(),
    val lastSelectedFighterId: String = "kairo",
    val lastSelectedArenaId: String = "neon_district",
    val lastRecoveryNote: String? = null
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 2
    }
}

object GameCatalog {
    val fighters: List<FighterProfile> = listOf(
        FighterProfile(
            id = "kairo",
            displayName = "Kairo",
            title = "Azure Cyber-Ronin",
            originArenaId = "neon_district",
            description = "Balanced plasma-blade striker with high-speed dash pressure and precision energy bursts.",
            fightingStyle = "Kenjutsu Overdrive",
            maxHealth = 10_000,
            moveSpeed = 4.6f,
            jumpVelocity = 8.4f,
            lightDamage = 480,
            heavyDamage = 920,
            defenseBp = 1000,        // 10% base defense
            critChanceBp = 1400,     // 14% crit chance
            critMultiplierBp = 15500,// 1.55x crit damage
            lightReach = 1.48f,
            heavyReach = 1.85f,
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF005B9F),
            accentColor = Color(0xFF80F8FF),
            specialAbility = AbilityDefinition(
                id = "azure_burst",
                name = "Azure Burst",
                description = "Launches a high-velocity plasma wave while surging forward to pierce mid-range guards.",
                energyCost = 300_000,
                baseDamage = 1_450,
                rangeMeters = 3.1f,
                dashVelocity = 5.5f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.08f,
                stunDurationSec = 0.36f,
                knockbackForce = 1.6f,
                archetype = AbilityArchetype.PLASMA_WAVE
            ),
            ultimateAbility = AbilityDefinition(
                id = "azure_final_strike",
                name = "Azure Final Strike",
                description = "Unleashes an omnidirectional storm of ionized blade cuts followed by a heaven-splitting beam.",
                energyCost = 1_000_000,
                baseDamage = 3_100,
                rangeMeters = 4.2f,
                dashVelocity = 7.2f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.35f,
                stunDurationSec = 0.65f,
                knockbackForce = 2.8f,
                archetype = AbilityArchetype.OMNI_SLASH_BEAM
            ),
            unlockedByDefault = true
        ),
        FighterProfile(
            id = "vex",
            displayName = "Vex",
            title = "Void Phantom Assassin",
            originArenaId = "neon_district",
            description = "Agile shadow infiltrator with lethal critical multipliers, phase-stepping cross-ups, and dark matter bursts.",
            fightingStyle = "Null-Space Ninjutsu",
            maxHealth = 9_200,
            moveSpeed = 5.3f,
            jumpVelocity = 9.0f,
            lightDamage = 460,
            heavyDamage = 890,
            defenseBp = 750,         // 7.5% base defense
            critChanceBp = 2200,     // 22% crit chance
            critMultiplierBp = 17500,// 1.75x crit damage
            lightReach = 1.40f,
            heavyReach = 1.75f,
            primaryColor = Color(0xFFBB86FC),
            secondaryColor = Color(0xFF4A148C),
            accentColor = Color(0xFFFF4081),
            specialAbility = AbilityDefinition(
                id = "shadow_step",
                name = "Shadow Step",
                description = "Warps through null-space behind the opponent with brief invulnerability and delivers a spinal void slash.",
                energyCost = 300_000,
                baseDamage = 1_520,
                rangeMeters = 3.8f,
                dashVelocity = 0f,
                teleportsBehind = true,
                grantsInvulnerabilitySec = 0.24f,
                stunDurationSec = 0.42f,
                knockbackForce = 1.3f,
                archetype = AbilityArchetype.SHADOW_TELEPORT
            ),
            ultimateAbility = AbilityDefinition(
                id = "void_breaker",
                name = "Void Breaker",
                description = "Collapses a gravitational singularity across the arena, imploding space around the target.",
                energyCost = 1_000_000,
                baseDamage = 3_280,
                rangeMeters = 4.4f,
                dashVelocity = 3.0f,
                teleportsBehind = true,
                grantsInvulnerabilitySec = 0.38f,
                stunDurationSec = 0.70f,
                knockbackForce = 2.6f,
                archetype = AbilityArchetype.VOID_SINGULARITY
            ),
            unlockedByDefault = true
        ),
        FighterProfile(
            id = "solara",
            displayName = "Solara",
            title = "Ember Valkyrie",
            originArenaId = "ember_temple",
            description = "Relentless rushdown pyromancer wielding twin solar Chakrams with high combo extensions and aerial dominance.",
            fightingStyle = "Helios Dance",
            maxHealth = 9_600,
            moveSpeed = 4.9f,
            jumpVelocity = 8.8f,
            lightDamage = 500,
            heavyDamage = 960,
            defenseBp = 900,         // 9% base defense
            critChanceBp = 1650,     // 16.5% crit chance
            critMultiplierBp = 16000,// 1.60x crit damage
            lightReach = 1.55f,
            heavyReach = 1.98f,
            primaryColor = Color(0xFFFF6D00),
            secondaryColor = Color(0xFFB71C1C),
            accentColor = Color(0xFFFFD600),
            specialAbility = AbilityDefinition(
                id = "solar_flare",
                name = "Solar Flare",
                description = "Erupts a column of molten solar plasma beneath the opponent, launching them into a searing shockwave.",
                energyCost = 300_000,
                baseDamage = 1_580,
                rangeMeters = 2.9f,
                dashVelocity = 4.2f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.12f,
                stunDurationSec = 0.40f,
                knockbackForce = 1.9f,
                archetype = AbilityArchetype.SOLAR_PILLAR
            ),
            ultimateAbility = AbilityDefinition(
                id = "supernova_judgment",
                name = "Supernova Judgment",
                description = "Ascends in a blazing phoenix aura and calls down a cataclysmic solar flare across the stage.",
                energyCost = 1_000_000,
                baseDamage = 3_220,
                rangeMeters = 4.5f,
                dashVelocity = 6.0f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.36f,
                stunDurationSec = 0.68f,
                knockbackForce = 3.0f,
                archetype = AbilityArchetype.METEOR_SUPERNOVA
            ),
            unlockedByDefault = true
        ),
        FighterProfile(
            id = "bront",
            displayName = "Bront",
            title = "Cryo-Forge Colossus",
            originArenaId = "frost_forge",
            description = "Heavy-armored juggernaut with towering health, high passive defense, and earth-shattering kinetic gauntlets.",
            fightingStyle = "Titan Siege",
            maxHealth = 12_200,
            moveSpeed = 3.8f,
            jumpVelocity = 7.4f,
            lightDamage = 580,
            heavyDamage = 1_160,
            defenseBp = 1800,        // 18% base defense
            critChanceBp = 1100,     // 11% crit chance
            critMultiplierBp = 15000,// 1.50x crit damage
            lightReach = 1.62f,
            heavyReach = 2.15f,
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF004D40),
            accentColor = Color(0xFF69F0AE),
            specialAbility = AbilityDefinition(
                id = "seismic_charge",
                name = "Seismic Charge",
                description = "Bulls forward with hyper-alloy shoulder armor, slamming the ground to stun and launch nearby foes.",
                energyCost = 300_000,
                baseDamage = 1_720,
                rangeMeters = 2.75f,
                dashVelocity = 6.4f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.18f,
                stunDurationSec = 0.50f,
                knockbackForce = 2.3f,
                archetype = AbilityArchetype.ARMORED_RUSH
            ),
            ultimateAbility = AbilityDefinition(
                id = "tectonic_cataclysm",
                name = "Tectonic Cataclysm",
                description = "Detonates dual cryo-seismic reactors into the arena floor, triggering a full-stage shockwave.",
                energyCost = 1_000_000,
                baseDamage = 3_480,
                rangeMeters = 4.6f,
                dashVelocity = 4.5f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.40f,
                stunDurationSec = 0.78f,
                knockbackForce = 3.4f,
                archetype = AbilityArchetype.TECTONIC_QUAKE
            ),
            unlockedByDefault = true
        ),
        FighterProfile(
            id = "nyx",
            displayName = "Nyx",
            title = "Chrono Archon",
            originArenaId = "sky_temple",
            description = "Unlockable celestial cyber-valkyrie who bends frame windows with temporal lances and eclipse storms.",
            fightingStyle = "Aether Chronomancy",
            maxHealth = 10_600,
            moveSpeed = 5.1f,
            jumpVelocity = 8.9f,
            lightDamage = 530,
            heavyDamage = 1_020,
            defenseBp = 1300,
            critChanceBp = 1850,
            critMultiplierBp = 16500,
            lightReach = 1.58f,
            heavyReach = 2.02f,
            primaryColor = Color(0xFFFFD600),
            secondaryColor = Color(0xFF311B92),
            accentColor = Color(0xFF00E5FF),
            specialAbility = AbilityDefinition(
                id = "chrono_rift",
                name = "Chrono Rift",
                description = "Slices a temporal tear across the stage, freezing the target's momentum on impact.",
                energyCost = 300_000,
                baseDamage = 1_640,
                rangeMeters = 3.4f,
                dashVelocity = 5.8f,
                teleportsBehind = false,
                grantsInvulnerabilitySec = 0.20f,
                stunDurationSec = 0.48f,
                knockbackForce = 1.8f,
                archetype = AbilityArchetype.CHRONO_SLICE
            ),
            ultimateAbility = AbilityDefinition(
                id = "eclipse_oblivion",
                name = "Eclipse Oblivion",
                description = "Summons a celestial eclipse over the Sky Temple, bombarding the arena with temporal lances.",
                energyCost = 1_000_000,
                baseDamage = 3_360,
                rangeMeters = 4.6f,
                dashVelocity = 6.5f,
                teleportsBehind = true,
                grantsInvulnerabilitySec = 0.40f,
                stunDurationSec = 0.75f,
                knockbackForce = 3.0f,
                archetype = AbilityArchetype.ECLIPSE_STORM
            ),
            unlockedByDefault = false,
            unlockRequirementText = "Reach Player Level 3 or win 1 Tournament"
        )
    )

    val arenas: List<ArenaProfile> = listOf(
        ArenaProfile(
            id = "neon_district",
            displayName = "Neon District",
            subtitle = "SECTOR 09 // CYBERSKYLINE",
            description = "Rain-slicked holographic rooftops overlooking the megacity core with reactive neon gridlines.",
            skyTopColor = Color(0xFF050716),
            skyBottomColor = Color(0xFF0D214F),
            floorColor = Color(0xFF0A1128),
            gridNeonColor = Color(0xFF00E5FF),
            hazardAccentColor = Color(0xFFFF007F),
            ambientWeather = "CYBER_RAIN",
            unlockedByDefault = true,
            requiredArcadeWins = 0
        ),
        ArenaProfile(
            id = "ember_temple",
            displayName = "Ember Temple",
            subtitle = "CALDERA SANCTUM // MAGMA CORE",
            description = "Ancient basalt citadel suspended above molten lava channels and rising volcanic sparks.",
            skyTopColor = Color(0xFF1A0505),
            skyBottomColor = Color(0xFF4E1409),
            floorColor = Color(0xFF230C0A),
            gridNeonColor = Color(0xFFFF6D00),
            hazardAccentColor = Color(0xFFFFD600),
            ambientWeather = "VOLCANIC_EMBERS",
            unlockedByDefault = true,
            requiredArcadeWins = 0
        ),
        ArenaProfile(
            id = "frost_forge",
            displayName = "Frost Forge",
            subtitle = "GLACIAL FOUNDRY // SUB-ZERO",
            description = "Cryo-industrial reactor deck surrounded by jagged permafrost spires and aurora borealis beams.",
            skyTopColor = Color(0xFF041421),
            skyBottomColor = Color(0xFF0D3B56),
            floorColor = Color(0xFF0B2236),
            gridNeonColor = Color(0xFF40C4FF),
            hazardAccentColor = Color(0xFF00E676),
            ambientWeather = "CRYO_SNOW",
            unlockedByDefault = true,
            requiredArcadeWins = 0
        ),
        ArenaProfile(
            id = "sky_temple",
            displayName = "Sky Temple",
            subtitle = "AETHER CITADEL // STRATOSPHERE",
            description = "Floating celestial sanctuary above the clouds. Unlocked by conquering 3 Arcade Ladders.",
            skyTopColor = Color(0xFF12082B),
            skyBottomColor = Color(0xFF391E7A),
            floorColor = Color(0xFF1D1442),
            gridNeonColor = Color(0xFFFFD600),
            hazardAccentColor = Color(0xFF00E5FF),
            ambientWeather = "AETHER_STARS",
            unlockedByDefault = false,
            requiredArcadeWins = 3
        )
    )

    fun fighterById(id: String): FighterProfile =
        fighters.firstOrNull { it.id == id } ?: fighters.first()

    fun arenaById(id: String): ArenaProfile =
        arenas.firstOrNull { it.id == id } ?: arenas.first()
}
