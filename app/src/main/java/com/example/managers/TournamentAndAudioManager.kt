package com.example.managers

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.combat.CombatAudioEvent
import com.example.model.AiDifficulty
import com.example.model.GameCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class TournamentMatch(
    val id: String,
    val roundIndex: Int, // 0 = Semifinals, 1 = Grand Finals
    val matchIndex: Int,
    val fighterAId: String,
    val fighterBId: String,
    val winnerId: String? = null,
    val involvesPlayer: Boolean = false
)

data class TournamentBracketState(
    val isActive: Boolean = false,
    val playerFighterId: String = "kairo",
    val arenaId: String = "neon_district",
    val difficulty: AiDifficulty = AiDifficulty.NORMAL,
    val currentRoundIndex: Int = 0, // 0 = Semifinals, 1 = Finals, 2 = Completed
    val semifinals: List<TournamentMatch> = emptyList(),
    val grandFinal: TournamentMatch? = null,
    val championFighterId: String? = null
)

object TournamentManager {

    fun createBracket(
        playerFighterId: String,
        arenaId: String,
        difficulty: AiDifficulty
    ): TournamentBracketState {
        val otherFighters = GameCatalog.fighters
            .map { it.id }
            .filter { it != playerFighterId }
            .shuffled()
            .take(3)

        val semi1 = TournamentMatch(
            id = "semi_1",
            roundIndex = 0,
            matchIndex = 0,
            fighterAId = playerFighterId,
            fighterBId = otherFighters.getOrElse(0) { "vex" },
            involvesPlayer = true
        )
        val semi2 = TournamentMatch(
            id = "semi_2",
            roundIndex = 0,
            matchIndex = 1,
            fighterAId = otherFighters.getOrElse(1) { "solara" },
            fighterBId = otherFighters.getOrElse(2) { "bront" },
            involvesPlayer = false
        )

        return TournamentBracketState(
            isActive = true,
            playerFighterId = playerFighterId,
            arenaId = arenaId,
            difficulty = difficulty,
            currentRoundIndex = 0,
            semifinals = listOf(semi1, semi2),
            grandFinal = null,
            championFighterId = null
        )
    }

    fun recordPlayerMatchOutcome(
        bracket: TournamentBracketState,
        playerWon: Boolean
    ): TournamentBracketState {
        if (!bracket.isActive) return bracket

        if (bracket.currentRoundIndex == 0) {
            val semi1 = bracket.semifinals[0]
            val semi2 = bracket.semifinals[1]
            val semi1Winner = if (playerWon) semi1.fighterAId else semi1.fighterBId
            // Deterministic or higher-stat resolution for CPU vs CPU semifinal
            val fA = GameCatalog.fighterById(semi2.fighterAId)
            val fB = GameCatalog.fighterById(semi2.fighterBId)
            val semi2Winner = if (fA.maxHealth + fA.heavyDamage >= fB.maxHealth + fB.heavyDamage) fA.id else fB.id

            val updatedSemis = listOf(
                semi1.copy(winnerId = semi1Winner),
                semi2.copy(winnerId = semi2Winner)
            )

            return if (playerWon) {
                bracket.copy(
                    currentRoundIndex = 1,
                    semifinals = updatedSemis,
                    grandFinal = TournamentMatch(
                        id = "grand_final",
                        roundIndex = 1,
                        matchIndex = 0,
                        fighterAId = semi1Winner,
                        fighterBId = semi2Winner,
                        involvesPlayer = true
                    )
                )
            } else {
                // Player eliminated in Semifinals; CPU wins tournament
                bracket.copy(
                    isActive = false,
                    currentRoundIndex = 2,
                    semifinals = updatedSemis,
                    grandFinal = TournamentMatch(
                        id = "grand_final",
                        roundIndex = 1,
                        matchIndex = 0,
                        fighterAId = semi1Winner,
                        fighterBId = semi2Winner,
                        winnerId = semi2Winner,
                        involvesPlayer = false
                    ),
                    championFighterId = semi2Winner
                )
            }
        } else if (bracket.currentRoundIndex == 1 && bracket.grandFinal != null) {
            val gf = bracket.grandFinal
            val champion = if (playerWon) gf.fighterAId else gf.fighterBId
            return bracket.copy(
                isActive = false,
                currentRoundIndex = 2,
                grandFinal = gf.copy(winnerId = champion),
                championFighterId = champion
            )
        }

        return bracket
    }
}

/**
 * Procedural Arcade Audio Synthesizer using Android's AudioTrack.
 * Generates zero-latency retro-cyber sound effects for swings, impacts, blocks, dodges,
 * special/ultimate bursts, and K.O. chimes while respecting Master and SFX volumes.
 */
class ProceduralAudioEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    fun playCombatEvent(
        event: CombatAudioEvent,
        masterVolume: Float,
        sfxVolume: Float
    ) {
        val gain = (masterVolume * sfxVolume).coerceIn(0f, 1f)
        if (gain <= 0.02f) return

        scope.launch {
            when (event) {
                CombatAudioEvent.LightSwing ->
                    synthesizeTone(startHz = 520f, endHz = 220f, durationMs = 65, gain = gain * 0.45f)
                CombatAudioEvent.HeavySwing ->
                    synthesizeTone(startHz = 360f, endHz = 120f, durationMs = 110, gain = gain * 0.55f)
                is CombatAudioEvent.HitImpact -> {
                    val start = if (event.isCrit) 880f else if (event.isHeavy) 280f else 420f
                    val end = if (event.isCrit) 180f else 75f
                    synthesizeTone(
                        startHz = start,
                        endHz = end,
                        durationMs = if (event.isHeavy) 135 else 85,
                        gain = gain * 0.75f,
                        addNoise = true
                    )
                }
                CombatAudioEvent.GuardImpact ->
                    synthesizeTone(startHz = 640f, endHz = 480f, durationMs = 70, gain = gain * 0.5f)
                CombatAudioEvent.DodgeWhoosh ->
                    synthesizeTone(startHz = 240f, endHz = 690f, durationMs = 95, gain = gain * 0.4f)
                is CombatAudioEvent.SpecialCast ->
                    synthesizeTone(startHz = 310f, endHz = 940f, durationMs = 190, gain = gain * 0.7f)
                is CombatAudioEvent.UltimateCast ->
                    synthesizeTone(startHz = 180f, endHz = 1240f, durationMs = 280, gain = gain * 0.85f, addNoise = true)
                CombatAudioEvent.Knockout ->
                    synthesizeTone(startHz = 620f, endHz = 95f, durationMs = 360, gain = gain * 0.9f, addNoise = true)
            }
        }
    }

    fun playUiClick(masterVolume: Float, sfxVolume: Float) {
        val gain = (masterVolume * sfxVolume).coerceIn(0f, 1f)
        if (gain <= 0.02f) return
        scope.launch {
            synthesizeTone(startHz = 740f, endHz = 980f, durationMs = 45, gain = gain * 0.35f)
        }
    }

    private fun synthesizeTone(
        startHz: Float,
        endHz: Float,
        durationMs: Int,
        gain: Float,
        addNoise: Boolean = false
    ) {
        runCatching {
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(64)
            val pcm = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples.toFloat()
                val freq = startHz + (endHz - startHz) * progress
                val envelope = (1f - progress) * gain
                phase += (2.0 * PI * freq) / sampleRate
                var sample = sin(phase).toFloat()
                if (addNoise) {
                    val pseudoNoise = sin(i * 12.9898).toFloat() * 0.35f
                    sample = (sample * 0.7f + pseudoNoise).coerceIn(-1f, 1f)
                }
                pcm[i] = (sample * envelope * Short.MAX_VALUE).toInt().coerceIn(
                    Short.MIN_VALUE.toInt(),
                    Short.MAX_VALUE.toInt()
                ).toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20L)
            track.stop()
            track.release()
        }
    }
}
