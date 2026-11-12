package com.example.managers

import android.content.Context
import com.example.model.GraphicsPreset
import com.example.model.PlayerSaveData
import com.example.model.PlayerSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max

object ProgressionCalculator {
    const val XP_PER_WIN = 100
    const val XP_PER_LOSS = 35
    const val LEVEL_XP_MULTIPLIER = 250
    const val ARCADE_WINS_FOR_SKY_TEMPLE = 3
    const val LEVEL_FOR_NYX_UNLOCK = 3

    /**
     * Existing P5 XP rule: Level requirement = current level * 250.
     */
    fun requiredXpForNextLevel(currentLevel: Int): Int =
        max(1, currentLevel) * LEVEL_XP_MULTIPLIER

    /**
     * Applies match outcome XP, computes any level-ups, and evaluates unlock conditions
     * for arenas (`sky_temple` after 3 Arcade wins) and characters (`nyx` at Level 3 or 1 Tournament win).
     */
    fun applyMatchResult(
        currentSave: PlayerSaveData,
        playerWon: Boolean,
        completedArcadeLadder: Boolean = false,
        completedTournamentVictory: Boolean = false,
        matchMaxCombo: Int = 0
    ): PlayerSaveData {
        val baseGain = if (playerWon) XP_PER_WIN else XP_PER_LOSS
        val bonusGain = when {
            completedTournamentVictory -> 150
            completedArcadeLadder -> 120
            else -> 0
        }

        var xp = max(0, currentSave.xp) + baseGain + bonusGain
        var level = max(1, currentSave.level)

        while (xp >= requiredXpForNextLevel(level)) {
            xp -= requiredXpForNextLevel(level)
            level++
        }

        val nextWins = currentSave.totalWins + (if (playerWon) 1 else 0)
        val nextLosses = currentSave.totalLosses + (if (!playerWon) 1 else 0)
        val nextArcadeWins = currentSave.arcadeWins + (if (completedArcadeLadder) 1 else 0)
        val nextTournamentWins = currentSave.tournamentWins + (if (completedTournamentVictory) 1 else 0)
        val nextBestCombo = max(currentSave.highestComboRecord, matchMaxCombo)

        val unlockedArenas = currentSave.unlockedArenaIds.toMutableSet().apply {
            add("neon_district")
            add("ember_temple")
            add("frost_forge")
            if (nextArcadeWins >= ARCADE_WINS_FOR_SKY_TEMPLE) {
                add("sky_temple")
            }
        }

        val unlockedFighters = currentSave.unlockedFighterIds.toMutableSet().apply {
            add("kairo")
            add("vex")
            add("solara")
            add("bront")
            if (level >= LEVEL_FOR_NYX_UNLOCK || nextTournamentWins >= 1) {
                add("nyx")
            }
        }

        return currentSave.copy(
            xp = xp,
            level = level,
            totalWins = nextWins,
            totalLosses = nextLosses,
            arcadeWins = nextArcadeWins,
            tournamentWins = nextTournamentWins,
            highestComboRecord = nextBestCombo,
            unlockedFighterIds = unlockedFighters,
            unlockedArenaIds = unlockedArenas
        )
    }
}

object SaveSerializer {

    fun toJsonString(save: PlayerSaveData): String {
        val v = sanitizeSaveData(save)
        val s = v.settings
        val fightersCsv = v.unlockedFighterIds.sorted().joinToString(",")
        val arenasCsv = v.unlockedArenaIds.sorted().joinToString(",")
        return buildString {
            appendLine("{")
            appendLine("  \"schemaVersion\": ${v.schemaVersion},")
            appendLine("  \"playerName\": \"${escape(v.playerName)}\",")
            appendLine("  \"xp\": ${v.xp},")
            appendLine("  \"level\": ${v.level},")
            appendLine("  \"totalWins\": ${v.totalWins},")
            appendLine("  \"totalLosses\": ${v.totalLosses},")
            appendLine("  \"arcadeWins\": ${v.arcadeWins},")
            appendLine("  \"tournamentWins\": ${v.tournamentWins},")
            appendLine("  \"highestComboRecord\": ${v.highestComboRecord},")
            appendLine("  \"lastSelectedFighterId\": \"${escape(v.lastSelectedFighterId)}\",")
            appendLine("  \"lastSelectedArenaId\": \"${escape(v.lastSelectedArenaId)}\",")
            appendLine("  \"unlockedFighterIds\": \"$fightersCsv\",")
            appendLine("  \"unlockedArenaIds\": \"$arenasCsv\",")
            appendLine("  \"graphicsPreset\": \"${s.graphicsPreset.name}\",")
            appendLine("  \"fpsCap\": ${s.fpsCap},")
            appendLine("  \"masterVolume\": ${s.masterVolume},")
            appendLine("  \"sfxVolume\": ${s.sfxVolume},")
            appendLine("  \"bgmVolume\": ${s.bgmVolume},")
            appendLine("  \"fullscreen\": ${s.fullscreen},")
            appendLine("  \"screenShake\": ${s.screenShake},")
            appendLine("  \"showDamageNumbers\": ${s.showDamageNumbers},")
            appendLine("  \"reducedFlashes\": ${s.reducedFlashes},")
            appendLine("  \"controlScale\": ${s.controlScale},")
            appendLine("  \"bestOfThreeVersus\": ${s.bestOfThreeVersus}")
            append("}")
        }
    }

    /**
     * Parses and validates a JSON save string, migrating v1 saves to v2 and recovering
     * gracefully with safe defaults if corrupted.
     */
    fun fromJsonString(rawJson: String?): Result<PlayerSaveData> {
        if (rawJson.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Empty save payload"))
        }
        val trimmed = rawJson.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return Result.failure(IllegalArgumentException("Malformed JSON envelope"))
        }

        return try {
            val map = parseFlatJsonMap(trimmed)
            if (map.isEmpty()) {
                return Result.failure(IllegalArgumentException("No valid fields in JSON"))
            }

            val version = map["schemaVersion"]?.toIntOrNull() ?: 1

            val unlockedFighters = mutableSetOf("kairo", "vex", "solara", "bront")
            map["unlockedFighterIds"]?.split(",")?.forEach { token ->
                val clean = token.trim().trim('"', '[', ']')
                if (clean.isNotBlank()) unlockedFighters.add(clean)
            }

            val unlockedArenas = mutableSetOf("neon_district", "ember_temple", "frost_forge")
            map["unlockedArenaIds"]?.split(",")?.forEach { token ->
                val clean = token.trim().trim('"', '[', ']')
                if (clean.isNotBlank()) unlockedArenas.add(clean)
            }

            val totalWins = if (version <= 1 && map.containsKey("wins") && !map.containsKey("totalWins")) {
                map["wins"]?.toIntOrNull() ?: 0
            } else {
                map["totalWins"]?.toIntOrNull() ?: 0
            }

            val presetName = map["graphicsPreset"] ?: GraphicsPreset.HIGH.name
            val preset = GraphicsPreset.entries.firstOrNull { it.name.equals(presetName, ignoreCase = true) }
                ?: GraphicsPreset.HIGH

            val settings = PlayerSettings(
                graphicsPreset = preset,
                fpsCap = (map["fpsCap"]?.toIntOrNull() ?: 60).coerceIn(30, 120),
                masterVolume = (map["masterVolume"]?.toFloatOrNull() ?: 0.85f).coerceIn(0f, 1f),
                sfxVolume = (map["sfxVolume"]?.toFloatOrNull() ?: 0.90f).coerceIn(0f, 1f),
                bgmVolume = (map["bgmVolume"]?.toFloatOrNull() ?: 0.65f).coerceIn(0f, 1f),
                fullscreen = map["fullscreen"]?.toBooleanStrictOrNull() ?: true,
                screenShake = map["screenShake"]?.toBooleanStrictOrNull() ?: true,
                showDamageNumbers = map["showDamageNumbers"]?.toBooleanStrictOrNull() ?: true,
                reducedFlashes = map["reducedFlashes"]?.toBooleanStrictOrNull() ?: false,
                controlScale = (map["controlScale"]?.toFloatOrNull() ?: 1.0f).coerceIn(0.75f, 1.35f),
                bestOfThreeVersus = map["bestOfThreeVersus"]?.toBooleanStrictOrNull() ?: true
            )

            val parsed = PlayerSaveData(
                schemaVersion = PlayerSaveData.CURRENT_SCHEMA_VERSION,
                playerName = (map["playerName"] ?: "ARCADE_ONE").ifBlank { "ARCADE_ONE" },
                xp = map["xp"]?.toIntOrNull() ?: 0,
                level = map["level"]?.toIntOrNull() ?: 1,
                totalWins = totalWins,
                totalLosses = map["totalLosses"]?.toIntOrNull() ?: 0,
                arcadeWins = map["arcadeWins"]?.toIntOrNull() ?: 0,
                tournamentWins = map["tournamentWins"]?.toIntOrNull() ?: 0,
                highestComboRecord = map["highestComboRecord"]?.toIntOrNull() ?: 0,
                unlockedFighterIds = unlockedFighters,
                unlockedArenaIds = unlockedArenas,
                settings = settings,
                lastSelectedFighterId = map["lastSelectedFighterId"] ?: "kairo",
                lastSelectedArenaId = map["lastSelectedArenaId"] ?: "neon_district",
                lastRecoveryNote = if (version < PlayerSaveData.CURRENT_SCHEMA_VERSION) {
                    "Migrated save schema v$version → v${PlayerSaveData.CURRENT_SCHEMA_VERSION}"
                } else null
            )

            Result.success(sanitizeSaveData(parsed))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escape(raw: String): String =
        raw.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun parseFlatJsonMap(json: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val pairRegex = Regex("\"([A-Za-z0-9_]+)\"\\s*:\\s*(\"[^\"]*\"|true|false|-?[0-9]+(?:\\.[0-9]+)?)")
        val matches = pairRegex.findAll(json).toList()
        if (matches.isEmpty()) {
            throw IllegalArgumentException("Invalid JSON key-value structure")
        }
        for (match in matches) {
            val key = match.groupValues[1]
            val rawVal = match.groupValues[2].trim().removeSurrounding("\"")
            result[key] = rawVal
        }
        return result
    }

    fun sanitizeSaveData(data: PlayerSaveData): PlayerSaveData {
        val safeLevel = data.level.coerceAtLeast(1)
        val safeXp = data.xp.coerceAtLeast(0)
        val safeArcadeWins = data.arcadeWins.coerceAtLeast(0)
        val safeTourneyWins = data.tournamentWins.coerceAtLeast(0)

        val arenas = data.unlockedArenaIds.toMutableSet().apply {
            add("neon_district")
            add("ember_temple")
            add("frost_forge")
            if (safeArcadeWins >= ProgressionCalculator.ARCADE_WINS_FOR_SKY_TEMPLE) {
                add("sky_temple")
            }
        }
        val fighters = data.unlockedFighterIds.toMutableSet().apply {
            add("kairo")
            add("vex")
            add("solara")
            add("bront")
            if (safeLevel >= ProgressionCalculator.LEVEL_FOR_NYX_UNLOCK || safeTourneyWins >= 1) {
                add("nyx")
            }
        }

        val s = data.settings
        val safeSettings = s.copy(
            fpsCap = s.fpsCap.coerceIn(30, 120),
            masterVolume = s.masterVolume.coerceIn(0f, 1f),
            sfxVolume = s.sfxVolume.coerceIn(0f, 1f),
            bgmVolume = s.bgmVolume.coerceIn(0f, 1f),
            controlScale = s.controlScale.coerceIn(0.75f, 1.35f)
        )

        return data.copy(
            schemaVersion = PlayerSaveData.CURRENT_SCHEMA_VERSION,
            xp = safeXp,
            level = safeLevel,
            totalWins = data.totalWins.coerceAtLeast(0),
            totalLosses = data.totalLosses.coerceAtLeast(0),
            arcadeWins = safeArcadeWins,
            tournamentWins = safeTourneyWins,
            highestComboRecord = data.highestComboRecord.coerceAtLeast(0),
            unlockedFighterIds = fighters,
            unlockedArenaIds = arenas,
            settings = safeSettings
        )
    }
}

class SaveManager(context: Context) {
    private val primaryFile = File(context.filesDir, "doom_arcade_save_v2.json")
    private val backupFile = File(context.filesDir, "doom_arcade_save_v2.bak")

    fun loadSaveData(): PlayerSaveData {
        // 1. Try primary file
        if (primaryFile.exists()) {
            val primaryResult = runCatching { primaryFile.readText() }
                .mapCatching { SaveSerializer.fromJsonString(it).getOrThrow() }
            if (primaryResult.isSuccess) {
                return primaryResult.getOrThrow()
            }
        }

        // 2. Fallback to backup file if primary was corrupted
        if (backupFile.exists()) {
            val backupResult = runCatching { backupFile.readText() }
                .mapCatching { SaveSerializer.fromJsonString(it).getOrThrow() }
            if (backupResult.isSuccess) {
                val recovered = backupResult.getOrThrow().copy(
                    lastRecoveryNote = "Recovered player profile from backup after primary save corruption."
                )
                saveData(recovered)
                return recovered
            }
        }

        // 3. Initialize clean default save
        val fresh = PlayerSaveData()
        saveData(fresh)
        return fresh
    }

    fun saveData(data: PlayerSaveData): Boolean {
        return try {
            val sanitized = SaveSerializer.sanitizeSaveData(data)
            val json = SaveSerializer.toJsonString(sanitized)
            if (primaryFile.exists()) {
                val existingCheck = SaveSerializer.fromJsonString(primaryFile.readText())
                if (existingCheck.isSuccess) {
                    primaryFile.copyTo(backupFile, overwrite = true)
                }
            }
            primaryFile.writeText(json)
            if (!backupFile.exists()) {
                backupFile.writeText(json)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun simulateCorruptPrimarySaveForTest() {
        primaryFile.writeText("{corrupted_json_syntax: [:::")
    }
}
