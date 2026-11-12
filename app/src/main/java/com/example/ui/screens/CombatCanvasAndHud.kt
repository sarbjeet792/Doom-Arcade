package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.combat.CombatArenaSnapshot
import com.example.combat.CombatConstants
import com.example.combat.FighterRuntimeState
import com.example.combat.FighterState
import com.example.managers.ProgressionCalculator
import com.example.model.ArenaProfile
import com.example.model.AttackType
import com.example.model.DummyBehavior
import com.example.model.FightStyle
import com.example.model.FighterProfile
import com.example.model.GameMode
import com.example.model.GraphicsPreset
import com.example.ui.theme.CrimsonKo
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SeismicEmerald
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UltimateGold
import com.example.ui.theme.VoidViolet
import com.example.viewmodel.DoomArcadeUiState
import com.example.viewmodel.DoomArcadeViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun CombatMatchScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler {
        viewModel.togglePauseMatch()
    }

    val snap = uiState.combatSnapshot
    val settings = uiState.saveData.settings
    val arena = uiState.selectedArenaProfile

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("combat_match_screen")
    ) {
        val isWideLandscape = maxWidth > maxHeight * 1.15f

        if (isWideLandscape) {
            Box(modifier = Modifier.fillMaxSize()) {
                Arena25DCanvas(
                    snapshot = snap,
                    arena = arena,
                    graphicsPreset = settings.graphicsPreset,
                    enableScreenShake = settings.screenShake,
                    showDamageNumbers = settings.showDamageNumbers,
                    reducedFlashes = settings.reducedFlashes,
                    modifier = Modifier.fillMaxSize()
                )

                CombatTopHud(
                    uiState = uiState,
                    onSelectStyle = { viewModel.selectFightStyle(it) },
                    onPauseClick = { viewModel.togglePauseMatch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                if (uiState.selectedMode == GameMode.PRACTICE) {
                    PracticeOverlayBar(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 104.dp)
                    )
                }

                TouchArcadeControlDeck(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                CombatTopHud(
                    uiState = uiState,
                    onSelectStyle = { viewModel.selectFightStyle(it) },
                    onPauseClick = { viewModel.togglePauseMatch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )

                if (uiState.selectedMode == GameMode.PRACTICE) {
                    PracticeOverlayBar(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, arena.gridNeonColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                ) {
                    Arena25DCanvas(
                        snapshot = snap,
                        arena = arena,
                        graphicsPreset = settings.graphicsPreset,
                        enableScreenShake = settings.screenShake,
                        showDamageNumbers = settings.showDamageNumbers,
                        reducedFlashes = settings.reducedFlashes,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                TouchArcadeControlDeck(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(CyberSurface.copy(alpha = 0.95f), ObsidianVoid)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }

        if (snap.isRoundOver) {
            RoundResultOverlayModal(
                uiState = uiState,
                onNextOrRematch = { viewModel.proceedToNextRoundOrAction() },
                onForceRematch = { viewModel.rematchImmediately() },
                onExitToMenu = { viewModel.navigateTo(com.example.viewmodel.AppScreen.MAIN_MENU) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (uiState.isMatchPaused && !snap.isRoundOver) {
            PauseMenuModal(
                uiState = uiState,
                onResume = { viewModel.togglePauseMatch() },
                onRestartMatch = { viewModel.rematchImmediately() },
                onOpenSettings = { viewModel.navigateTo(com.example.viewmodel.AppScreen.SETTINGS) },
                onExitToMenu = { viewModel.navigateTo(com.example.viewmodel.AppScreen.MAIN_MENU) },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun CombatTopHud(
    uiState: DoomArcadeUiState,
    onSelectStyle: (FightStyle) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snap = uiState.combatSnapshot
    val p1 = snap.player
    val p2 = snap.opponent

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Fight Style Stance Selector Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FightStyle.entries.forEach { style ->
                    val active = uiState.selectedFightStyle == style
                    val styleColor = when (style) {
                        FightStyle.RUSH -> NeonCyan
                        FightStyle.POWER -> SolarAmber
                        FightStyle.AEGIS -> SeismicEmerald
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) styleColor.copy(alpha = 0.24f) else CyberSurfaceElevated)
                            .border(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) styleColor else CyberBorder,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectStyle(style) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("hud_style_${style.name.lowercase()}")
                    ) {
                        Text(
                            text = style.badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) styleColor else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${uiState.measuredFps} FPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = SeismicEmerald,
                    modifier = Modifier.testTag("hud_fps_counter")
                )
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CutCornerShape(6.dp))
                        .background(CyberSurfaceElevated)
                        .border(1.dp, CyberBorder, CutCornerShape(6.dp))
                        .testTag("btn_pause_match")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Match",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FighterHudColumn(
                fighter = p1,
                roundsWon = snap.playerRoundsWon,
                alignRight = false,
                modifier = Modifier
                    .weight(1f)
                    .testTag("hud_player_panel")
            )

            Box(
                modifier = Modifier
                    .width(58.dp)
                    .height(52.dp)
                    .clip(CutCornerShape(10.dp))
                    .background(CyberSurfaceElevated)
                    .border(1.5.dp, UltimateGold, CutCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.selectedMode == GameMode.PRACTICE) "∞" else "${snap.roundTimerSec.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = UltimateGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RND ${snap.roundNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            FighterHudColumn(
                fighter = p2,
                roundsWon = snap.opponentRoundsWon,
                alignRight = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("hud_opponent_panel")
            )
        }
    }
}

@Composable
private fun FighterHudColumn(
    fighter: FighterRuntimeState,
    roundsWon: Int,
    alignRight: Boolean,
    modifier: Modifier = Modifier
) {
    val hpRatio = (fighter.health.toFloat() / fighter.profile.maxHealth.toFloat()).coerceIn(0f, 1f)
    val energyRatio = (fighter.energy.toFloat() / CombatConstants.MAX_ENERGY.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!alignRight) {
                Text(
                    text = fighter.profile.displayName.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = fighter.profile.primaryColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                RoundWinPips(roundsWon = roundsWon, color = UltimateGold)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${fighter.health}/${fighter.profile.maxHealth}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            } else {
                Text(
                    text = "${fighter.health}/${fighter.profile.maxHealth}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                RoundWinPips(roundsWon = roundsWon, color = UltimateGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fighter.profile.displayName.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = fighter.profile.primaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CutCornerShape(4.dp))
                .background(Color(0xFF1B0912))
                .border(1.dp, fighter.profile.primaryColor.copy(alpha = 0.7f), CutCornerShape(4.dp))
        ) {
            val hpColor = when {
                hpRatio > 0.5f -> fighter.profile.primaryColor
                hpRatio > 0.25f -> SolarAmber
                else -> CrimsonKo
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(hpRatio)
                    .align(if (alignRight) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            listOf(hpColor, hpColor.copy(alpha = 0.75f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Fast-Charge Overdrive Energy Bar (0 .. 1,000,000)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0A1224))
                .border(
                    width = if (fighter.canUseUltimate) 1.5.dp else 0.8.dp,
                    color = if (fighter.canUseUltimate) UltimateGold else if (fighter.canUseSpecial) NeonCyan else CyberBorder,
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            val barBrush = when {
                fighter.canUseUltimate -> Brush.horizontalGradient(listOf(SolarAmber, UltimateGold, Color.White))
                fighter.canUseSpecial -> Brush.horizontalGradient(listOf( Color(0xFF2979FF), NeonCyan))
                else -> Brush.horizontalGradient(listOf(Color(0xFF304FFE), Color(0xFF00B0FF)))
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(energyRatio)
                    .align(if (alignRight) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(barBrush)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.SpaceBetween
        ) {
            val readyTag = when {
                fighter.canUseUltimate -> "★ ULTRA READY"
                fighter.canUseSpecial -> "⚡ SPECIAL READY"
                else -> "${fighter.energy / 1000}K / 1000K"
            }
            Text(
                text = "${fighter.fightStyle.badge} • $readyTag",
                style = MaterialTheme.typography.labelMedium,
                color = if (fighter.canUseUltimate) UltimateGold else if (fighter.canUseSpecial) NeonCyan else TextSecondary,
                fontSize = 10.sp
            )
            if (fighter.comboCount > 1) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${fighter.comboCount} HIT! ${fighter.lastComboLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = UltimateGold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun RoundWinPips(roundsWon: Int, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(2) { idx ->
            val filled = idx < roundsWon
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (filled) color else Color.Transparent)
                    .border(1.dp, color, CircleShape)
            )
        }
    }
}

@Composable
private fun PracticeOverlayBar(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val stats = uiState.practiceStats
    Surface(
        modifier = modifier.testTag("practice_hud_panel"),
        color = CyberSurface.copy(alpha = 0.90f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATTEMPTS: ${stats.attempts}  •  HITS: ${stats.hitsLanded}  •  ACC: ${String.format("%.0f", stats.accuracyPercent)}%  •  DMG: ${stats.totalDamageDealt}  •  MAX COMBO: ${stats.maxCombo}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val entries = DummyBehavior.entries
                            val next = entries[(uiState.practiceDummyBehavior.ordinal + 1) % entries.size]
                            viewModel.setPracticeDummyBehavior(next)
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_cycle_dummy_behavior"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DUMMY: ${uiState.practiceDummyBehavior.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 10.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetPracticeSession() },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_reset_practice_stats"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Practice Stats",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET", style = MaterialTheme.typography.labelMedium, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

/**
 * Reusable live animated 2.5D Fighter Preview Canvas for the Modern Home Dashboard & Fighter Codex.
 */
@Composable
fun FighterPortraitPreviewCanvas(
    fighterProfile: FighterProfile,
    fightStyle: FightStyle,
    animTimeSec: Float,
    modifier: Modifier = Modifier
) {
    val previewState = remember(fighterProfile, fightStyle, animTimeSec) {
        FighterRuntimeState(
            profile = fighterProfile,
            isPlayerOne = true,
            fightStyle = fightStyle,
            x = 0f,
            y = 0f,
            facingRight = true,
            energy = CombatConstants.MAX_ENERGY,
            state = FighterState.IDLE,
            stateTimerSec = animTimeSec,
            auraIntensity = 0.85f
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val floorY = h * 0.86f
        val center = Offset(w * 0.5f, floorY)

        // Glowing Holographic Pedestal
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(fighterProfile.primaryColor.copy(alpha = 0.45f), Color.Transparent),
                center = center,
                radius = w * 0.42f
            ),
            topLeft = Offset(w * 0.1f, floorY - h * 0.08f),
            size = Size(w * 0.8f, h * 0.16f)
        )

        drawFighter25D(
            fighter = previewState,
            screenPos = center,
            floorY = floorY,
            scalePx = minOf(w, h) * 0.62f,
            enableReflections = true,
            reducedFlashes = false
        )
    }
}

@Composable
private fun Arena25DCanvas(
    snapshot: CombatArenaSnapshot,
    arena: ArenaProfile,
    graphicsPreset: GraphicsPreset,
    enableScreenShake: Boolean,
    showDamageNumbers: Boolean,
    reducedFlashes: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.testTag("arena_25d_canvas")) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.64f
        val stageFloorY = h * 0.84f

        val shakePx = if (enableScreenShake && snapshot.screenShakeMagnitude > 0.01f) {
            sin(snapshot.roundTimerSec * 95f) * snapshot.screenShakeMagnitude * 15f
        } else 0f

        withTransform({
            translate(left = shakePx, top = abs(shakePx) * 0.4f)
        }) {
            // 1. Sky Backdrop
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(arena.skyTopColor, arena.skyBottomColor),
                    startY = 0f,
                    endY = horizonY
                ),
                size = Size(w, horizonY)
            )

            // Subtle Ultimate Cinema Darkening when a fighter unleashes an Ultimate or Special
            if (snapshot.specialCutInTimerSec > 0f && !reducedFlashes) {
                val dimAlpha = (snapshot.specialCutInTimerSec * 0.42f).coerceIn(0f, 0.55f)
                drawRect(
                    color = Color.Black.copy(alpha = dimAlpha),
                    size = Size(w, h)
                )
            }

            // 2. Arena-Specific 2.5D Architectural Pillars / Skyline
            val pillarCount = 8
            for (i in 0 until pillarCount) {
                val px = (i.toFloat() / pillarCount) * w + (w / (pillarCount * 2f))
                val pillarH = h * (0.22f + (i % 3) * 0.08f)
                val pillarW = w * 0.07f
                drawRoundRect(
                    color = arena.floorColor.copy(alpha = 0.85f),
                    topLeft = Offset(px - pillarW / 2f, horizonY - pillarH),
                    size = Size(pillarW, pillarH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                if (graphicsPreset.enableDynamicGlow) {
                    drawLine(
                        color = arena.gridNeonColor.copy(alpha = 0.45f),
                        start = Offset(px, horizonY - pillarH + 8f),
                        end = Offset(px, horizonY),
                        strokeWidth = 3f
                    )
                }
            }

            // 3. 2.5D Perspective Stage Floor with Neon Grid
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(arena.floorColor, ObsidianVoid),
                    startY = horizonY,
                    endY = h
                ),
                topLeft = Offset(0f, horizonY),
                size = Size(w, h - horizonY)
            )

            val centerX = w * 0.5f
            for (g in -6..6) {
                val topX = centerX + g * (w * 0.055f)
                val botX = centerX + g * (w * 0.14f)
                drawLine(
                    color = arena.gridNeonColor.copy(alpha = 0.28f),
                    start = Offset(topX, horizonY),
                    end = Offset(botX, h),
                    strokeWidth = 2f
                )
            }
            for (r in 1..4) {
                val ry = horizonY + (h - horizonY) * (r / 4f)
                drawLine(
                    color = arena.gridNeonColor.copy(alpha = 0.24f),
                    start = Offset(0f, ry),
                    end = Offset(w, ry),
                    strokeWidth = 1.5f
                )
            }

            fun worldToScreen(wx: Float, wy: Float, zOffset: Float = 0f): Offset {
                val normX = (wx / (CombatConstants.STAGE_HALF_WIDTH * 1.15f)).coerceIn(-1f, 1f)
                val sx = centerX + normX * (w * 0.42f)
                val sy = stageFloorY - (wy * (h * 0.14f)) + (zOffset * (h * 0.05f))
                return Offset(sx, sy)
            }

            // 5. Shockwaves
            snapshot.shockwaves.forEach { sw ->
                val progress = (sw.ageSec / sw.lifetimeSec).coerceIn(0f, 1f)
                val center = worldToScreen(sw.worldX, sw.worldY)
                val radiusPx = sw.maxRadius * (w * 0.08f) * progress
                val alpha = (1f - progress) * (if (reducedFlashes) 0.4f else 0.85f)
                drawCircle(
                    color = sw.color.copy(alpha = alpha),
                    radius = radiusPx,
                    center = center,
                    style = Stroke(width = 6f * (1f - progress * 0.5f))
                )
            }

            // 6. Draw Both Fighters + Bespoke Animated Special/Ultimate Effects
            val scalePx = minOf(w, h) * 0.24f
            val fightersByDepth = listOf(snapshot.player, snapshot.opponent).sortedBy { it.zOffset }
            fightersByDepth.forEach { fighter ->
                val opponent = if (fighter.isPlayerOne) snapshot.opponent else snapshot.player
                val fPos = worldToScreen(fighter.x, fighter.y, fighter.zOffset)
                val oppPos = worldToScreen(opponent.x, opponent.y, opponent.zOffset)

                drawFighter25D(
                    fighter = fighter,
                    screenPos = fPos,
                    floorY = stageFloorY + fighter.zOffset * (h * 0.05f),
                    scalePx = scalePx,
                    enableReflections = graphicsPreset.enableReflections,
                    reducedFlashes = reducedFlashes
                )

                if (fighter.state == FighterState.SPECIAL_ATTACK || fighter.state == FighterState.ULTIMATE_ATTACK) {
                    drawAnimatedSpecialAndUltimateEffects(
                        attacker = fighter,
                        attackerScreenPos = fPos,
                        defenderScreenPos = oppPos,
                        floorY = stageFloorY,
                        scalePx = scalePx,
                        reducedFlashes = reducedFlashes
                    )
                }
            }

            // 7. Hit Sparks & Particles
            val maxParticlesToDraw = (snapshot.particles.size * graphicsPreset.particleMultiplier).toInt()
            snapshot.particles.take(maxParticlesToDraw).forEach { pt ->
                val progress = (pt.ageSec / pt.lifetimeSec).coerceIn(0f, 1f)
                val pos = worldToScreen(pt.worldX, pt.worldY)
                drawCircle(
                    color = pt.color.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                    radius = pt.radius * (1f - progress * 0.5f),
                    center = pos
                )
            }

            // 8. Floating Damage Numbers & Combo Callouts
            if (showDamageNumbers) {
                snapshot.floatingTexts.forEach { ft ->
                    val progress = (ft.ageSec / ft.lifetimeSec).coerceIn(0f, 1f)
                    val pos = worldToScreen(ft.worldX, ft.worldY)
                    val alpha = (1f - progress).coerceIn(0f, 1f)
                    val mainLayout = textMeasurer.measure(
                        text = ft.text,
                        style = TextStyle(
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (ft.isCrit) 18.sp else 14.sp,
                            color = ft.color.copy(alpha = alpha)
                        )
                    )
                    drawText(
                        textLayoutResult = mainLayout,
                        topLeft = Offset(pos.x - mainLayout.size.width / 2f, pos.y - mainLayout.size.height)
                    )
                    if (ft.subText.isNotBlank()) {
                        val subLayout = textMeasurer.measure(
                            text = ft.subText,
                            style = TextStyle(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = UltimateGold.copy(alpha = alpha)
                            )
                        )
                        drawText(
                            textLayoutResult = subLayout,
                            topLeft = Offset(pos.x - subLayout.size.width / 2f, pos.y + 2f)
                        )
                    }
                }
            }

            // 9. Special / Ultimate Cinematic Cut-In Flash Banner
            if (snapshot.specialCutInBanner.isNotBlank() && snapshot.specialCutInTimerSec > 0f) {
                val cutInLayout = textMeasurer.measure(
                    text = snapshot.specialCutInBanner,
                    style = TextStyle(
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                )
                val barY = h * 0.18f
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            snapshot.specialCutInColor.copy(alpha = 0.85f),
                            ObsidianVoid.copy(alpha = 0.92f),
                            snapshot.specialCutInColor.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(0f, barY - 8f),
                    size = Size(w, cutInLayout.size.height + 16f)
                )
                drawText(
                    textLayoutResult = cutInLayout,
                    topLeft = Offset(centerX - cutInLayout.size.width / 2f, barY)
                )
            }

            // 10. Round Announcement Banner
            if (snapshot.announcementBanner.isNotBlank() && snapshot.announcementTimerSec > 0f) {
                val bannerLayout = textMeasurer.measure(
                    text = snapshot.announcementBanner,
                    style = TextStyle(
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = UltimateGold
                    )
                )
                val bx = centerX - bannerLayout.size.width / 2f
                val by = h * 0.28f
                drawRoundRect(
                    color = ObsidianVoid.copy(alpha = 0.84f),
                    topLeft = Offset(bx - 24f, by - 12f),
                    size = Size(bannerLayout.size.width + 48f, bannerLayout.size.height + 24f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawText(textLayoutResult = bannerLayout, topLeft = Offset(bx, by))
            }
        }
    }
}

/**
 * Renders detailed 2.5D Fighter anatomy with character-specific physical build,
 * articulated multi-segment limbs, dynamic facial features (eyes, eyebrows, scars,
 * battle-cry mouth/mask/visor), flowing hair/cape physics, and overdrive aura flames.
 */
private fun DrawScope.drawFighter25D(
    fighter: FighterRuntimeState,
    screenPos: Offset,
    floorY: Float,
    scalePx: Float,
    enableReflections: Boolean,
    reducedFlashes: Boolean
) {
    val dir = if (fighter.facingRight) 1f else -1f
    val primary = fighter.profile.primaryColor
    val secondary = fighter.profile.secondaryColor
    val accent = fighter.profile.accentColor
    val isBront = fighter.profile.id == "bront"
    val isVex = fighter.profile.id == "vex"
    val isSolara = fighter.profile.id == "solara"
    val isNyx = fighter.profile.id == "nyx"

    // Character physical build proportions
    val physiqueWidthFactor = when {
        isBront -> 1.32f
        isVex -> 0.90f
        else -> 1.04f
    }
    val physiqueHeightFactor = when {
        isBront -> 1.12f
        isVex -> 0.96f
        else -> 1.0f
    }

    // 1. Ground Shadow & Neon Floor Reflection
    val shadowScale = (1f - (floorY - screenPos.y) / (scalePx * 3f)).coerceIn(0.35f, 1f)
    drawOval(
        color = Color.Black.copy(alpha = 0.58f * shadowScale),
        topLeft = Offset(screenPos.x - scalePx * 0.42f * physiqueWidthFactor * shadowScale, floorY - scalePx * 0.08f),
        size = Size(scalePx * 0.84f * physiqueWidthFactor * shadowScale, scalePx * 0.16f)
    )

    if (enableReflections && abs(floorY - screenPos.y) < 12f) {
        drawOval(
            color = primary.copy(alpha = 0.22f),
            topLeft = Offset(screenPos.x - scalePx * 0.34f, floorY + 4f),
            size = Size(scalePx * 0.68f, scalePx * 0.12f)
        )
    }

    // 2. Overdrive Energy Aura (Special Ready >= 300k, Ultimate Overdrive == 1M)
    if (fighter.canUseSpecial && !reducedFlashes) {
        val auraColor = if (fighter.canUseUltimate) UltimateGold else primary
        val pulse = 0.88f + sin(fighter.stateTimerSec * 14f) * 0.14f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = if (fighter.canUseUltimate) 0.42f else 0.24f),
                    Color.Transparent
                ),
                center = Offset(screenPos.x, screenPos.y - scalePx * 0.52f),
                radius = scalePx * 0.72f * pulse
            ),
            topLeft = Offset(screenPos.x - scalePx * 0.65f, screenPos.y - scalePx * 1.15f),
            size = Size(scalePx * 1.3f, scalePx * 1.2f)
        )

        // Rising aura flame wisps
        val flameCount = if (fighter.canUseUltimate) 5 else 3
        for (fIdx in 0 until flameCount) {
            val phase = (fighter.stateTimerSec * 3.5f + fIdx * 0.37f) % 1f
            val fx = screenPos.x + (fIdx - flameCount / 2) * (scalePx * 0.18f)
            val fy = screenPos.y - phase * scalePx * 1.05f
            drawCircle(
                color = auraColor.copy(alpha = (1f - phase) * 0.65f),
                radius = scalePx * 0.06f * (1f - phase * 0.5f),
                center = Offset(fx, fy)
            )
        }
    }

    // 3. Dodge Invulnerability Afterimages
    if (fighter.isDodgeInvulnerable) {
        for (ghost in 1..2) {
            drawCircle(
                color = NeonCyan.copy(alpha = 0.28f / ghost),
                radius = scalePx * 0.44f,
                center = Offset(
                    screenPos.x - dir * scalePx * 0.22f * ghost,
                    screenPos.y - scalePx * 0.48f
                )
            )
        }
    }

    withTransform({
        translate(left = screenPos.x, top = screenPos.y)
        rotate(degrees = fighter.hitLeanAngleDeg, pivot = Offset.Zero)
        scale(
            scaleX = if (fighter.state == FighterState.DEFEATED) 1.28f else (2f - fighter.squashStretchY),
            scaleY = if (fighter.state == FighterState.DEFEATED) 0.32f else fighter.squashStretchY,
            pivot = Offset.Zero
        )
    }) {
        val bodyH = scalePx * 0.96f * physiqueHeightFactor
        val torsoW = scalePx * 0.36f * physiqueWidthFactor
        val breathe = sin(fighter.stateTimerSec * 6.5f) * (scalePx * 0.024f)
        val stride = if (fighter.state == FighterState.MOVING) {
            sin(fighter.stateTimerSec * 16f) * scalePx * 0.20f
        } else 0f

        // 4. Flowing Back Cape / Scarf / Wings / Waist Sash Physics
        val windWave = sin(fighter.stateTimerSec * 10f - fighter.velocityX * 0.4f) * scalePx * 0.12f
        if (isVex || isSolara || isNyx) {
            val capePath = Path().apply {
                moveTo(-dir * torsoW * 0.25f, -bodyH * 0.78f + breathe)
                quadraticTo(
                    -dir * (torsoW * 0.95f + abs(windWave)),
                    -bodyH * 0.50f + windWave * 0.5f,
                    -dir * (torsoW * 1.15f + windWave),
                    -bodyH * 0.18f
                )
                lineTo(-dir * torsoW * 0.35f, -bodyH * 0.36f)
                close()
            }
            drawPath(
                path = capePath,
                brush = Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.78f), primary.copy(alpha = 0.22f))
                )
            )
        } else {
            // Kairo / Bront kinetic waist sash
            drawLine(
                color = accent.copy(alpha = 0.85f),
                start = Offset(-dir * torsoW * 0.30f, -bodyH * 0.40f),
                end = Offset(-dir * (torsoW * 0.85f + abs(windWave)), -bodyH * 0.22f + windWave * 0.4f),
                strokeWidth = scalePx * 0.06f,
                cap = StrokeCap.Round
            )
        }

        // 5. Multi-Segment Articulated Legs (Thigh -> Knee -> Shin -> Armored Boot)
        val hipY = -bodyH * 0.40f
        val kneeY = -bodyH * 0.21f
        val backHipX = -dir * torsoW * 0.22f
        val frontHipX = dir * torsoW * 0.22f

        val backKnee = Offset(backHipX - stride * 0.55f, kneeY)
        val backFoot = Offset(backHipX - dir * torsoW * 0.14f - stride, 0f)
        drawLine(secondary, Offset(backHipX, hipY), backKnee, strokeWidth = scalePx * 0.135f * physiqueWidthFactor, cap = StrokeCap.Round)
        drawLine(secondary, backKnee, backFoot, strokeWidth = scalePx * 0.115f * physiqueWidthFactor, cap = StrokeCap.Round)
        // Armored Knee Guard & Boot
        drawCircle(accent, radius = scalePx * 0.045f, center = backKnee)
        drawRoundRect(
            color = primary,
            topLeft = Offset(backFoot.x - scalePx * 0.09f, -scalePx * 0.06f),
            size = Size(scalePx * 0.20f, scalePx * 0.07f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        val frontKnee = Offset(frontHipX + stride * 0.55f, kneeY)
        val frontFoot = Offset(frontHipX + dir * torsoW * 0.18f + stride, 0f)
        drawLine(primary.copy(alpha = 0.92f), Offset(frontHipX, hipY), frontKnee, strokeWidth = scalePx * 0.135f * physiqueWidthFactor, cap = StrokeCap.Round)
        drawLine(secondary, frontKnee, frontFoot, strokeWidth = scalePx * 0.115f * physiqueWidthFactor, cap = StrokeCap.Round)
        drawCircle(accent, radius = scalePx * 0.048f, center = frontKnee)
        drawRoundRect(
            color = accent,
            topLeft = Offset(frontFoot.x - scalePx * 0.09f, -scalePx * 0.06f),
            size = Size(scalePx * 0.21f, scalePx * 0.07f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 6. Sculpted Muscular Torso, Pectoral Plates, Abdominal Segments & Championship Belt
        val torsoPath = Path().apply {
            moveTo(-torsoW * 0.60f, -bodyH * 0.82f + breathe)
            lineTo(torsoW * 0.60f, -bodyH * 0.82f + breathe)
            lineTo(torsoW * 0.40f, -bodyH * 0.38f)
            lineTo(-torsoW * 0.40f, -bodyH * 0.38f)
            close()
        }
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(listOf(primary, secondary))
        )

        // Pectoral / Chest Plate Definition
        drawLine(
            color = accent.copy(alpha = 0.65f),
            start = Offset(-torsoW * 0.42f, -bodyH * 0.66f + breathe),
            end = Offset(torsoW * 0.42f, -bodyH * 0.66f + breathe),
            strokeWidth = scalePx * 0.025f
        )
        // Abdominal Armor Ribs
        for (rib in 0..1) {
            val ry = -bodyH * (0.55f - rib * 0.07f) + breathe * 0.5f
            drawLine(
                color = ObsidianVoid.copy(alpha = 0.55f),
                start = Offset(-torsoW * 0.26f, ry),
                end = Offset(torsoW * 0.26f, ry),
                strokeWidth = scalePx * 0.02f
            )
        }

        // Shoulder Pauldrons
        drawCircle(
            color = secondary,
            radius = scalePx * 0.11f * physiqueWidthFactor,
            center = Offset(-torsoW * 0.56f, -bodyH * 0.78f + breathe)
        )
        drawCircle(
            color = primary,
            radius = scalePx * 0.12f * physiqueWidthFactor,
            center = Offset(torsoW * 0.56f, -bodyH * 0.78f + breathe)
        )

        // Cyber Core Reactor & Waist Belt
        drawCircle(
            color = accent,
            radius = scalePx * 0.075f,
            center = Offset(dir * torsoW * 0.10f, -bodyH * 0.67f + breathe)
        )
        drawRoundRect(
            color = UltimateGold,
            topLeft = Offset(-torsoW * 0.42f, -bodyH * 0.42f),
            size = Size(torsoW * 0.84f, scalePx * 0.055f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 7. Detailed Head, Hair/Helm & Expressive Facial Features
        val headRadius = scalePx * (if (isBront) 0.165f else 0.155f)
        val headCenter = Offset(dir * scalePx * 0.04f, -bodyH * 0.97f + breathe)

        // Nyx Celestial Halo behind head
        if (isNyx) {
            drawCircle(
                color = UltimateGold.copy(alpha = 0.85f),
                radius = headRadius * 1.45f,
                center = headCenter,
                style = Stroke(width = scalePx * 0.03f)
            )
        }

        // Base Face / Skin-Tone or Cyber-Visor Head
        val faceTone = when (fighter.profile.id) {
            "kairo" -> Color(0xFFE8C3A9)
            "solara" -> Color(0xFFD9A17C)
            "vex" -> Color(0xFF2B233D)
            "bront" -> Color(0xFF37474F)
            else -> Color(0xFFE2D6F5)
        }
        drawCircle(
            color = faceTone,
            radius = headRadius,
            center = headCenter
        )

        // Sculpted Chin / Jawline Contour
        drawArc(
            color = secondary.copy(alpha = 0.65f),
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius),
            size = Size(headRadius * 2f, headRadius * 2f),
            style = Stroke(width = scalePx * 0.025f)
        )

        // Expressive Eyes (Iris + Pupil + Fierce Angled Eyebrows)
        val eyeX = headCenter.x + dir * headRadius * 0.38f
        val eyeY = headCenter.y - headRadius * 0.12f
        val eyeColor = if (fighter.canUseUltimate) UltimateGold else accent

        // Eye Sclera & Glowing Iris
        drawOval(
            color = Color.White,
            topLeft = Offset(eyeX - headRadius * 0.24f, eyeY - headRadius * 0.12f),
            size = Size(headRadius * 0.48f, headRadius * 0.24f)
        )
        drawCircle(
            color = eyeColor,
            radius = headRadius * 0.11f,
            center = Offset(eyeX + dir * headRadius * 0.05f, eyeY)
        )
        drawCircle(
            color = ObsidianVoid,
            radius = headRadius * 0.045f,
            center = Offset(eyeX + dir * headRadius * 0.06f, eyeY)
        )

        // Dynamic Eyebrow Angle (Fierce glare when attacking, raised when hit)
        val browTilt = when (fighter.state) {
            FighterState.ATTACKING, FighterState.SPECIAL_ATTACK, FighterState.ULTIMATE_ATTACK -> dir * headRadius * 0.15f
            FighterState.TAKING_DAMAGE, FighterState.STUNNED -> -dir * headRadius * 0.12f
            else -> dir * headRadius * 0.06f
        }
        drawLine(
            color = ObsidianVoid,
            start = Offset(eyeX - headRadius * 0.26f, eyeY - headRadius * 0.22f - browTilt),
            end = Offset(eyeX + headRadius * 0.26f, eyeY - headRadius * 0.22f + browTilt),
            strokeWidth = scalePx * 0.026f,
            cap = StrokeCap.Round
        )

        // Facial Scar / Cheek Markings & Mouth / Mask Features
        when (fighter.profile.id) {
            "kairo" -> {
                // Left-eye vertical ronin scar
                drawLine(
                    color = CrimsonKo.copy(alpha = 0.7f),
                    start = Offset(eyeX - dir * headRadius * 0.08f, eyeY - headRadius * 0.28f),
                    end = Offset(eyeX - dir * headRadius * 0.08f, eyeY + headRadius * 0.28f),
                    strokeWidth = scalePx * 0.016f
                )
                // Glowing Cyber Oni Half-Mask covering lower jaw
                val maskPath = Path().apply {
                    moveTo(headCenter.x - headRadius * 0.65f, headCenter.y + headRadius * 0.08f)
                    lineTo(headCenter.x + headRadius * 0.85f * dir, headCenter.y + headRadius * 0.05f)
                    lineTo(headCenter.x + headRadius * 0.45f * dir, headCenter.y + headRadius * 0.92f)
                    lineTo(headCenter.x - headRadius * 0.45f * dir, headCenter.y + headRadius * 0.82f)
                    close()
                }
                drawPath(maskPath, color = secondary)
                // Oni Fang Vent
                drawLine(
                    color = NeonCyan,
                    start = Offset(headCenter.x + dir * headRadius * 0.15f, headCenter.y + headRadius * 0.45f),
                    end = Offset(headCenter.x + dir * headRadius * 0.62f, headCenter.y + headRadius * 0.35f),
                    strokeWidth = scalePx * 0.024f,
                    cap = StrokeCap.Round
                )
                // Spiky Wind-Swept Cyber-Ronin Hair
                for (spike in 0..3) {
                    val angleOffset = spike * 0.28f
                    drawLine(
                        color = ObsidianVoid,
                        start = Offset(headCenter.x - dir * headRadius * 0.2f, headCenter.y - headRadius * 0.65f),
                        end = Offset(
                            headCenter.x - dir * headRadius * (1.25f + spike * 0.18f),
                            headCenter.y - headRadius * (1.15f - angleOffset) + windWave * 0.2f
                        ),
                        strokeWidth = scalePx * 0.045f,
                        cap = StrokeCap.Round
                    )
                }
            }
            "vex" -> {
                // Stealth Assassin Cowl & Glowing Twin Ocular Slits
                drawArc(
                    color = secondary,
                    startAngle = 150f,
                    sweepAngle = 240f,
                    useCenter = true,
                    topLeft = Offset(headCenter.x - headRadius * 1.15f, headCenter.y - headRadius * 1.15f),
                    size = Size(headRadius * 2.3f, headRadius * 2.3f)
                )
                drawLine(
                    color = accent,
                    start = Offset(eyeX - headRadius * 0.18f, eyeY),
                    end = Offset(eyeX + dir * headRadius * 0.35f, eyeY - headRadius * 0.06f),
                    strokeWidth = scalePx * 0.035f,
                    cap = StrokeCap.Round
                )
            }
            "solara" -> {
                // Expressive Battle-Cry Mouth / Smirk
                val isShouting = fighter.currentAttack != null
                if (isShouting) {
                    drawOval(
                        color = CrimsonKo,
                        topLeft = Offset(eyeX - headRadius * 0.14f, headCenter.y + headRadius * 0.28f),
                        size = Size(headRadius * 0.32f, headRadius * 0.24f)
                    )
                } else {
                    drawLine(
                        color = Color(0xFF6D2C1B),
                        start = Offset(eyeX - headRadius * 0.15f, headCenter.y + headRadius * 0.38f),
                        end = Offset(eyeX + headRadius * 0.18f, headCenter.y + headRadius * 0.32f),
                        strokeWidth = scalePx * 0.022f,
                        cap = StrokeCap.Round
                    )
                }
                // Blazing Solar Diadem Crown & Flowing Ponytail
                drawLine(
                    color = UltimateGold,
                    start = Offset(headCenter.x, headCenter.y - headRadius * 0.75f),
                    end = Offset(headCenter.x + dir * headRadius * 0.65f, headCenter.y - headRadius * 1.35f),
                    strokeWidth = scalePx * 0.042f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = SolarAmber,
                    start = Offset(headCenter.x - dir * headRadius * 0.5f, headCenter.y - headRadius * 0.4f),
                    end = Offset(headCenter.x - dir * headRadius * 1.75f, headCenter.y - headRadius * 0.15f + windWave * 0.4f),
                    strokeWidth = scalePx * 0.065f,
                    cap = StrokeCap.Round
                )
            }
            "bront" -> {
                // Horned Cryo-Titan Helmet & Glowing Emerald T-Visor
                drawLine(
                    color = accent,
                    start = Offset(headCenter.x - dir * headRadius * 0.1f, eyeY),
                    end = Offset(headCenter.x + dir * headRadius * 0.85f, eyeY),
                    strokeWidth = scalePx * 0.055f,
                    cap = StrokeCap.Round
                )
                // Twin Titan Horns
                drawLine(
                    color = UltimateGold,
                    start = Offset(headCenter.x, headCenter.y - headRadius * 0.7f),
                    end = Offset(headCenter.x + dir * headRadius * 0.95f, headCenter.y - headRadius * 1.45f),
                    strokeWidth = scalePx * 0.05f,
                    cap = StrokeCap.Round
                )
            }
            else -> {
                // Nyx Starlight Crown & Serene Expression
                drawLine(
                    color = UltimateGold,
                    start = Offset(eyeX - headRadius * 0.12f, headCenter.y + headRadius * 0.36f),
                    end = Offset(eyeX + headRadius * 0.16f, headCenter.y + headRadius * 0.34f),
                    strokeWidth = scalePx * 0.02f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 8. Multi-Jointed Articulated Arms (Shoulder -> Elbow -> Gauntlet Hand) & Weapon
        val atk = fighter.currentAttack
        val extendFactor = if (atk != null) {
            sin(fighter.attackProgress * Math.PI.toFloat())
        } else 0f

        val shoulder = Offset(dir * torsoW * 0.38f, -bodyH * 0.75f + breathe)
        val elbow = Offset(
            shoulder.x + dir * scalePx * (0.16f + extendFactor * 0.22f),
            shoulder.y + scalePx * (0.10f - extendFactor * 0.12f)
        )
        val hand = Offset(
            elbow.x + dir * scalePx * (0.20f + extendFactor * (if (atk == AttackType.HEAVY) 0.44f else 0.30f)),
            elbow.y - scalePx * (0.04f + extendFactor * 0.08f)
        )

        // Upper Arm & Forearm
        drawLine(secondary, shoulder, elbow, strokeWidth = scalePx * 0.115f * physiqueWidthFactor, cap = StrokeCap.Round)
        drawLine(primary, elbow, hand, strokeWidth = scalePx * 0.105f * physiqueWidthFactor, cap = StrokeCap.Round)
        // Glowing Gauntlet Knuckle
        drawCircle(accent, radius = scalePx * 0.065f * physiqueWidthFactor, center = hand)

        // Character-Specific Weapon
        when (fighter.profile.id) {
            "kairo" -> {
                val bladeTip = Offset(
                    hand.x + dir * scalePx * (0.48f + extendFactor * 0.38f),
                    hand.y - scalePx * (0.24f - extendFactor * 0.16f)
                )
                drawLine(accent, hand, bladeTip, strokeWidth = scalePx * 0.055f, cap = StrokeCap.Round)
                drawLine(Color.White, hand, bladeTip, strokeWidth = scalePx * 0.022f, cap = StrokeCap.Round)
            }
            "vex" -> {
                val daggerTip = Offset(hand.x + dir * scalePx * 0.36f, hand.y + scalePx * 0.08f)
                drawLine(accent, hand, daggerTip, strokeWidth = scalePx * 0.05f, cap = StrokeCap.Round)
            }
            "solara" -> {
                drawCircle(
                    color = accent,
                    radius = scalePx * (0.17f + extendFactor * 0.12f),
                    center = hand,
                    style = Stroke(width = scalePx * 0.045f)
                )
            }
            "bront" -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(hand.x - scalePx * 0.14f, hand.y - scalePx * 0.14f),
                    size = Size(scalePx * 0.28f, scalePx * 0.28f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
            else -> {
                drawLine(
                    color = UltimateGold,
                    start = Offset(hand.x - dir * scalePx * 0.15f, hand.y),
                    end = Offset(hand.x + dir * scalePx * 0.58f, hand.y - scalePx * 0.12f),
                    strokeWidth = scalePx * 0.05f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 9. Guard Hex-Shield Barrier (65% Damage Reduction)
        if (fighter.isBlockingActive) {
            drawArc(
                color = NeonCyan.copy(alpha = 0.76f),
                startAngle = if (dir > 0) -65f else 115f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(-scalePx * 0.54f, -bodyH * 1.05f),
                size = Size(scalePx * 1.08f, bodyH * 1.05f),
                style = Stroke(width = scalePx * 0.065f, cap = StrokeCap.Round)
            )
        }

        if (fighter.flashAlpha > 0.05f && !reducedFlashes) {
            drawCircle(
                color = Color.White.copy(alpha = fighter.flashAlpha * 0.45f),
                radius = scalePx * 0.44f,
                center = Offset(0f, -bodyH * 0.55f)
            )
        }
    }
}

/**
 * Renders bespoke multi-phase animated Special (300K) and Ultimate (1M) move visuals
 * tailored to each fighter's unique ability archetype.
 */
private fun DrawScope.drawAnimatedSpecialAndUltimateEffects(
    attacker: FighterRuntimeState,
    attackerScreenPos: Offset,
    defenderScreenPos: Offset,
    floorY: Float,
    scalePx: Float,
    reducedFlashes: Boolean
) {
    val isUlt = attacker.state == FighterState.ULTIMATE_ATTACK
    val progress = attacker.attackProgress
    val dir = if (attacker.facingRight) 1f else -1f
    val primary = attacker.profile.primaryColor
    val accent = attacker.profile.accentColor
    val alphaScale = if (reducedFlashes) 0.55f else 0.92f

    when (attacker.profile.id) {
        "kairo" -> {
            if (!isUlt) {
                // Azure Burst: Traveling crescent plasma wave + lightning arcs
                val waveX = attackerScreenPos.x + dir * (scalePx * 0.5f + progress * scalePx * 2.2f)
                val waveY = attackerScreenPos.y - scalePx * 0.58f
                drawArc(
                    color = accent.copy(alpha = (1f - progress * 0.4f) * alphaScale),
                    startAngle = if (dir > 0) -75f else 105f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(waveX - scalePx * 0.45f, waveY - scalePx * 0.55f),
                    size = Size(scalePx * 0.90f, scalePx * 1.10f),
                    style = Stroke(width = scalePx * 0.11f, cap = StrokeCap.Round)
                )
            } else {
                // Azure Final Strike: 6 Criss-crossing Judgment Katana Slashes + Orbital Plasma Beam
                for (s in 0 until 6) {
                    val angle = (s * 30f + progress * 180f) * (Math.PI.toFloat() / 180f)
                    val len = scalePx * 1.35f
                    val cx = defenderScreenPos.x
                    val cy = defenderScreenPos.y - scalePx * 0.55f
                    drawLine(
                        color = if (s % 2 == 0) accent.copy(alpha = alphaScale) else Color.White.copy(alpha = alphaScale),
                        start = Offset(cx - cos(angle) * len, cy - sin(angle) * len),
                        end = Offset(cx + cos(angle) * len, cy + sin(angle) * len),
                        strokeWidth = scalePx * 0.06f,
                        cap = StrokeCap.Round
                    )
                }
                val beamW = scalePx * 0.72f * sin(progress * Math.PI.toFloat())
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, primary.copy(alpha = alphaScale), Color.White, primary.copy(alpha = alphaScale), Color.Transparent)
                    ),
                    topLeft = Offset(defenderScreenPos.x - beamW / 2f, 0f),
                    size = Size(beamW, floorY)
                )
            }
        }
        "vex" -> {
            if (!isUlt) {
                // Shadow Step: Twin X-shaped void slash trails across the opponent
                val cx = defenderScreenPos.x
                val cy = defenderScreenPos.y - scalePx * 0.55f
                val slashLen = scalePx * 0.85f * sin(progress * Math.PI.toFloat()).coerceAtLeast(0.2f)
                drawLine(
                    color = accent.copy(alpha = alphaScale),
                    start = Offset(cx - slashLen, cy - slashLen),
                    end = Offset(cx + slashLen, cy + slashLen),
                    strokeWidth = scalePx * 0.07f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = primary.copy(alpha = alphaScale),
                    start = Offset(cx - slashLen, cy + slashLen),
                    end = Offset(cx + slashLen, cy - slashLen),
                    strokeWidth = scalePx * 0.07f,
                    cap = StrokeCap.Round
                )
            } else {
                // Void Breaker: Spinning Gravitational Black-Hole Singularity
                val center = Offset(defenderScreenPos.x, defenderScreenPos.y - scalePx * 0.60f)
                val maxR = scalePx * 1.3f * sin(progress * Math.PI.toFloat()).coerceAtLeast(0.25f)
                drawCircle(
                    color = ObsidianVoid,
                    radius = maxR * 0.55f,
                    center = center
                )
                drawCircle(
                    color = primary.copy(alpha = alphaScale),
                    radius = maxR * 0.82f,
                    center = center,
                    style = Stroke(width = scalePx * 0.08f)
                )
                drawCircle(
                    color = accent.copy(alpha = alphaScale),
                    radius = maxR,
                    center = center,
                    style = Stroke(width = scalePx * 0.04f)
                )
            }
        }
        "solara" -> {
            if (!isUlt) {
                // Solar Flare: Rising molten fire pillar beneath opponent
                val pillarW = scalePx * 0.65f
                val pillarH = scalePx * 1.8f * sin(progress * Math.PI.toFloat()).coerceAtLeast(0.2f)
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(UltimateGold, SolarAmber, CrimsonKo.copy(alpha = 0.2f))),
                    topLeft = Offset(defenderScreenPos.x - pillarW / 2f, floorY - pillarH),
                    size = Size(pillarW, pillarH),
                    cornerRadius = CornerRadius(pillarW / 2f, pillarW / 2f)
                )
            } else {
                // Supernova Judgment: 5 Diagonal Solar Meteors Raining Across Stage
                for (m in 0 until 5) {
                    val mProg = ((progress * 1.6f - m * 0.12f)).coerceIn(0f, 1f)
                    val mx = defenderScreenPos.x + (m - 2) * scalePx * 0.45f - (1f - mProg) * dir * scalePx * 1.2f
                    val my = mProg * floorY
                    drawCircle(
                        color = UltimateGold.copy(alpha = alphaScale),
                        radius = scalePx * 0.22f,
                        center = Offset(mx, my)
                    )
                    drawLine(
                        color = SolarAmber.copy(alpha = alphaScale * 0.8f),
                        start = Offset(mx, my),
                        end = Offset(mx - dir * scalePx * 0.65f, my - scalePx * 0.65f),
                        strokeWidth = scalePx * 0.16f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        "bront" -> {
            // Seismic Charge / Tectonic Cataclysm: Erupting Cryo-Seismic Spires along the stage floor
            val spireCount = if (isUlt) 5 else 2
            for (sp in 1..spireCount) {
                val sx = attackerScreenPos.x + dir * sp * scalePx * 0.55f
                val spireH = scalePx * (if (isUlt) 1.15f else 0.72f) * sin(progress * Math.PI.toFloat()).coerceAtLeast(0.2f)
                val spirePath = Path().apply {
                    moveTo(sx - scalePx * 0.18f, floorY)
                    lineTo(sx, floorY - spireH)
                    lineTo(sx + scalePx * 0.18f, floorY)
                    close()
                }
                drawPath(
                    path = spirePath,
                    brush = Brush.verticalGradient(listOf(accent.copy(alpha = alphaScale), primary.copy(alpha = 0.4f)))
                )
            }
        }
        else -> {
            // Nyx Chrono Rift / Eclipse Oblivion: Celestial Eclipse Corona & Golden Temporal Lances
            val center = Offset(defenderScreenPos.x, defenderScreenPos.y - scalePx * 0.95f)
            drawCircle(
                color = UltimateGold.copy(alpha = alphaScale),
                radius = scalePx * (if (isUlt) 0.75f else 0.45f),
                center = center,
                style = Stroke(width = scalePx * 0.06f)
            )
            val lanceCount = if (isUlt) 6 else 3
            for (l in 0 until lanceCount) {
                val lx = defenderScreenPos.x + (l - lanceCount / 2) * scalePx * 0.32f
                drawLine(
                    color = UltimateGold.copy(alpha = alphaScale),
                    start = Offset(lx, center.y),
                    end = Offset(lx, floorY),
                    strokeWidth = scalePx * 0.05f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun TouchArcadeControlDeck(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val p1 = uiState.combatSnapshot.player
    val scale = uiState.saveData.settings.controlScale.coerceIn(0.8f, 1.25f)

    var leftHeld by remember { mutableStateOf(false) }
    var rightHeld by remember { mutableStateOf(false) }
    var blockHeld by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("touch_controls_deck"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left Cluster: Movement (Left / Right) + Jump + Guard + Dodge
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ArcadeTouchButton(
                    label = "JUMP",
                    subLabel = "SPACE",
                    color = NeonCyan,
                    enabled = p1.isAlive,
                    widthDp = (64 * scale).toInt(),
                    heightDp = (48 * scale).toInt(),
                    testTag = "btn_touch_jump",
                    onPressStart = { viewModel.triggerJump() },
                    onPressEnd = {}
                )
                ArcadeTouchButton(
                    label = "GUARD",
                    subLabel = "-65% DMG",
                    color = if (blockHeld) UltimateGold else Color(0xFF40C4FF),
                    enabled = p1.isAlive,
                    widthDp = (70 * scale).toInt(),
                    heightDp = (48 * scale).toInt(),
                    testTag = "btn_touch_block",
                    onPressStart = {
                        blockHeld = true
                        viewModel.setBlockHeld(true)
                    },
                    onPressEnd = {
                        blockHeld = false
                        viewModel.setBlockHeld(false)
                    }
                )
                ArcadeTouchButton(
                    label = "DODGE",
                    subLabel = if (p1.dodgeCooldownTimerSec > 0f) "${String.format("%.1f", p1.dodgeCooldownTimerSec)}s" else "0.28s INV",
                    color = VoidViolet,
                    enabled = p1.canDodge,
                    widthDp = (70 * scale).toInt(),
                    heightDp = (48 * scale).toInt(),
                    testTag = "btn_touch_dodge",
                    onPressStart = { viewModel.triggerDodge() },
                    onPressEnd = {}
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArcadeTouchButton(
                    label = "◀ LEFT",
                    subLabel = "A",
                    color = if (leftHeld) NeonCyan else CyberBorder,
                    enabled = p1.isAlive,
                    widthDp = (102 * scale).toInt(),
                    heightDp = (52 * scale).toInt(),
                    testTag = "btn_touch_left",
                    onPressStart = {
                        leftHeld = true
                        viewModel.setMoveAxis(-1f)
                    },
                    onPressEnd = {
                        leftHeld = false
                        viewModel.setMoveAxis(if (rightHeld) 1f else 0f)
                    }
                )
                ArcadeTouchButton(
                    label = "RIGHT ▶",
                    subLabel = "D",
                    color = if (rightHeld) NeonCyan else CyberBorder,
                    enabled = p1.isAlive,
                    widthDp = (102 * scale).toInt(),
                    heightDp = (52 * scale).toInt(),
                    testTag = "btn_touch_right",
                    onPressStart = {
                        rightHeld = true
                        viewModel.setMoveAxis(1f)
                    },
                    onPressEnd = {
                        rightHeld = false
                        viewModel.setMoveAxis(if (leftHeld) -1f else 0f)
                    }
                )
            }
        }

        // Right Cluster: Overdrive Surge (+260K), Special (300K), Ultimate (1M), Light (J), Heavy (K)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ArcadeTouchButton(
                    label = "⚡ SURGE",
                    subLabel = "+260K EN",
                    color = SeismicEmerald,
                    enabled = p1.canSurgeEnergy,
                    widthDp = (68 * scale).toInt(),
                    heightDp = (48 * scale).toInt(),
                    testTag = "btn_touch_surge",
                    onPressStart = { viewModel.triggerEnergySurge() },
                    onPressEnd = {}
                )
                ArcadeTouchButton(
                    label = "SPECIAL",
                    subLabel = if (p1.canUseSpecial) "READY!" else "300K EN",
                    color = NeonCyan,
                    enabled = p1.canUseSpecial,
                    widthDp = (76 * scale).toInt(),
                    heightDp = (48 * scale).toInt(),
                    testTag = "btn_touch_special",
                    onPressStart = { viewModel.triggerSpecialAttack() },
                    onPressEnd = {}
                )
                ArcadeTouchButton(
                    label = "ULTIMATE",
                    subLabel = if (p1.canUseUltimate) "★ MAX!" else "1000K EN",
                    color = UltimateGold,
                    enabled = p1.canUseUltimate,
                    widthDp = (82 * scale).toInt(),
                    heightDp = (48 * scale).toInt(),
                    testTag = "btn_touch_ultimate",
                    onPressStart = { viewModel.triggerUltimateAttack() },
                    onPressEnd = {}
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArcadeTouchButton(
                    label = "LIGHT (J)",
                    subLabel = "${p1.profile.lightDamage} DMG",
                    color = SeismicEmerald,
                    enabled = p1.isAlive,
                    widthDp = (108 * scale).toInt(),
                    heightDp = (52 * scale).toInt(),
                    testTag = "btn_touch_light",
                    onPressStart = { viewModel.triggerLightAttack() },
                    onPressEnd = {}
                )
                ArcadeTouchButton(
                    label = "HEAVY (K)",
                    subLabel = "${p1.profile.heavyDamage} DMG",
                    color = SolarAmber,
                    enabled = p1.isAlive,
                    widthDp = (108 * scale).toInt(),
                    heightDp = (52 * scale).toInt(),
                    testTag = "btn_touch_heavy",
                    onPressStart = { viewModel.triggerHeavyAttack() },
                    onPressEnd = {}
                )
            }
        }
    }
}

@Composable
private fun ArcadeTouchButton(
    label: String,
    subLabel: String,
    color: Color,
    enabled: Boolean,
    widthDp: Int,
    heightDp: Int,
    testTag: String,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    val activeAlpha = if (enabled) 1f else 0.35f
    Box(
        modifier = Modifier
            .width(max(48, widthDp).dp)
            .height(max(48, heightDp).dp)
            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .background(
                if (enabled) CyberSurfaceElevated.copy(alpha = 0.92f)
                else CyberSurface.copy(alpha = 0.55f)
            )
            .border(
                width = if (enabled) 1.6.dp else 1.dp,
                color = color.copy(alpha = activeAlpha),
                shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressEnd()
                    }
                )
            }
            .clickable(enabled = enabled) {
                onPressStart()
                onPressEnd()
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelMedium,
                color = color.copy(alpha = activeAlpha),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RoundResultOverlayModal(
    uiState: DoomArcadeUiState,
    onNextOrRematch: () -> Unit,
    onForceRematch: () -> Unit,
    onExitToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snap = uiState.combatSnapshot
    val playerWonRound = snap.winnerIsPlayer == true
    val accent = if (playerWonRound) NeonCyan else CrimsonKo

    Surface(
        modifier = modifier
            .padding(20.dp)
            .width(380.dp)
            .testTag("round_result_modal"),
        color = CyberSurface.copy(alpha = 0.96f),
        shape = CutCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, accent)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (snap.isMatchOver) {
                    if (snap.playerRoundsWon > snap.opponentRoundsWon) "VICTORY!" else "DEFEAT"
                } else {
                    "ROUND ${snap.roundNumber} COMPLETE"
                },
                style = MaterialTheme.typography.displayMedium,
                color = accent
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${snap.player.profile.displayName} ${snap.playerRoundsWon}  —  ${snap.opponentRoundsWon} ${snap.opponent.profile.displayName}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Max Combo: ${snap.playerMaxCombo} Hits  •  Damage Dealt: ${snap.playerTotalDamageDealt}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (snap.isMatchOver && uiState.selectedMode != GameMode.PRACTICE) {
                Spacer(modifier = Modifier.height(6.dp))
                val xpEarned = if (snap.playerRoundsWon > snap.opponentRoundsWon) "+100 XP" else "+35 XP"
                Text(
                    text = "PROGRESSION REWARD: $xpEarned • LEVEL ${uiState.saveData.level} (${uiState.saveData.xp}/${ProgressionCalculator.requiredXpForNextLevel(uiState.saveData.level)} XP)",
                    style = MaterialTheme.typography.labelMedium,
                    color = UltimateGold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val primaryBtnLabel = when {
                !snap.isMatchOver -> "NEXT ROUND"
                uiState.selectedMode == GameMode.ARCADE && snap.playerRoundsWon > snap.opponentRoundsWon && uiState.arcadeStageIndex < 4 ->
                    "NEXT LADDER STAGE (${uiState.arcadeStageIndex + 2}/5)"
                uiState.selectedMode == GameMode.TOURNAMENT -> "RETURN TO BRACKET"
                else -> "REMATCH"
            }

            Button(
                onClick = onNextOrRematch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_round_next_action"),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = ObsidianVoid)
            ) {
                Text(primaryBtnLabel, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onForceRematch,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_round_rematch")
                ) {
                    Text("REMATCH", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onExitToMenu,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_round_main_menu")
                ) {
                    Text("MAIN MENU", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun PauseMenuModal(
    uiState: DoomArcadeUiState,
    onResume: () -> Unit,
    onRestartMatch: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(20.dp)
            .width(340.dp)
            .testTag("pause_menu_modal"),
        color = CyberSurface.copy(alpha = 0.96f),
        shape = CutCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "MATCH PAUSED",
                style = MaterialTheme.typography.headlineLarge,
                color = NeonCyan
            )
            Text(
                text = "${uiState.selectedPlayerProfile.displayName} vs ${uiState.selectedOpponentProfile.displayName} • ${uiState.selectedArenaProfile.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_pause_resume"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianVoid)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESUME COMBAT", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onRestartMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_pause_restart")
            ) {
                Text("RESTART MATCH", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_pause_settings")
            ) {
                Text("GRAPHICS & AUDIO SETTINGS", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onExitToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_pause_exit")
            ) {
                Text("QUIT TO MAIN MENU", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
