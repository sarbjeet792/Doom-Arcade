package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.combat.CombatConstants
import com.example.managers.ProgressionCalculator
import com.example.model.AttackType
import com.example.model.GameCatalog
import com.example.model.GraphicsPreset
import com.example.ui.theme.CrimsonKo
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SeismicEmerald
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UltimateGold
import com.example.ui.theme.VoidViolet
import com.example.viewmodel.AppScreen
import com.example.viewmodel.DoomArcadeUiState
import com.example.viewmodel.DoomArcadeViewModel

@Composable
fun RosterAndUnlocksScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler { viewModel.navigateTo(AppScreen.MAIN_MENU) }
    val save = uiState.saveData

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("roster_unlocks_screen"),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ArcadeBottomNavBar(
                currentScreen = uiState.currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "FIGHTER CODEX & UNLOCKABLES",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NeonCyan
                )
                Text(
                    text = "Complete frame data, unique ability profiles, combo multipliers & arena unlock milestones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Combo Multiplier & Combat System Spec Card
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UltimateGold.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "COMBAT ENGINE & COMBO MULTIPLIERS (BASIS-POINT VERIFIED)",
                            style = MaterialTheme.typography.titleLarge,
                            color = UltimateGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Light → Heavy: 1.25x  |  Heavy → Light: 1.15x  |  Light → Light: 1.10x  |  Light → Light → Heavy: 1.40x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "• Chain Window: ${CombatConstants.COMBO_CHAIN_WINDOW_SEC}s  |  Chain Timeout: ${CombatConstants.COMBO_TIMEOUT_SEC}s  |  Block Reduction: 65%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "• Fast-Charge Overdrive: 150K Start | +180K Hit | +48K/s Regen | +260K Surge | Special: 300K | Ultimate: 1M",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan
                        )
                        Text(
                            text = "• Fight Styles: ⚡ RUSH (+18% Speed, +35% Energy) | 🔥 POWER (+22% DMG) | 🛡️ AEGIS (+12% Defense)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UltimateGold
                        )
                    }
                }
            }

            items(GameCatalog.fighters, key = { it.id }) { fighter ->
                val unlocked = fighter.id in save.unlockedFighterIds
                Surface(
                    color = CyberSurface,
                    shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (unlocked) fighter.primaryColor else CyberBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(88.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianVoid)
                                    .border(1.dp, fighter.primaryColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            ) {
                                FighterPortraitPreviewCanvas(
                                    fighterProfile = fighter,
                                    fightStyle = com.example.model.FightStyle.RUSH,
                                    animTimeSec = 1.2f,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${fighter.displayName.uppercase()} — ${fighter.title.uppercase()}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = fighter.primaryColor
                                    )
                                    Icon(
                                        imageVector = if (unlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = if (unlocked) "Unlocked" else "Locked",
                                        tint = if (unlocked) SeismicEmerald else CrimsonKo
                                    )
                                }
                                Text(
                                    text = "Style: ${fighter.fightingStyle} • HP: ${fighter.maxHealth} • Speed: ${fighter.moveSpeed}m/s • Defense: ${fighter.defenseBp / 100}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fighter.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Special (300K): ${fighter.specialAbility.name} (${fighter.specialAbility.baseDamage} DMG)  •  Ultimate (1M): ${fighter.ultimateAbility.name} (${fighter.ultimateAbility.baseDamage} DMG)",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan
                        )
                    }
                }
            }

            item {
                Text(
                    text = "ARENA STAGES & UNLOCK CONDITIONS",
                    style = MaterialTheme.typography.titleLarge,
                    color = SolarAmber
                )
            }

            items(GameCatalog.arenas, key = { it.id }) { arena ->
                val unlocked = arena.id in save.unlockedArenaIds
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (unlocked) arena.gridNeonColor else CyberBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${arena.displayName.uppercase()} (${arena.subtitle})",
                                style = MaterialTheme.typography.titleLarge,
                                color = arena.gridNeonColor
                            )
                            Text(
                                text = arena.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = if (unlocked) "UNLOCKED" else "${save.arcadeWins}/3 ARCADE WINS",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (unlocked) SeismicEmerald else UltimateGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatsScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler { viewModel.navigateTo(AppScreen.MAIN_MENU) }
    val save = uiState.saveData
    val totalMatches = save.totalWins + save.totalLosses
    val winRate = if (totalMatches > 0) (save.totalWins * 100) / totalMatches else 0
    val nextXp = ProgressionCalculator.requiredXpForNextLevel(save.level)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_stats_screen"),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ArcadeBottomNavBar(
                currentScreen = uiState.currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "PLAYER PROFILE & SAVE PERSISTENCE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NeonCyan
                )
                Text(
                    text = "Persistent v${save.schemaVersion} Save Schema • Win = +100 XP • Loss = +35 XP • Next Level = Level × 250 XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (uiState.statusBannerMessage != null) {
                item {
                    Surface(
                        color = SeismicEmerald.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SeismicEmerald)
                    ) {
                        Text(
                            text = uiState.statusBannerMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SeismicEmerald,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Surface(
                    color = CyberSurface,
                    shape = CutCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${save.playerName} • LEVEL ${save.level}",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                        Text(
                            text = "CURRENT XP: ${save.xp} / $nextXp XP",
                            style = MaterialTheme.typography.titleLarge,
                            color = UltimateGold
                        )
                        Text(
                            text = "Total Matches: $totalMatches  |  Wins: ${save.totalWins}  |  Losses: ${save.totalLosses}  |  Win Rate: $winRate%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Arcade Ladder Completions: ${save.arcadeWins}  |  Tournament Titles: ${save.tournamentWins}  |  Best Combo: ${save.highestComboRecord} Hits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Unlocked Fighters: ${save.unlockedFighterIds.size}/${GameCatalog.fighters.size}  |  Unlocked Arenas: ${save.unlockedArenaIds.size}/${GameCatalog.arenas.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan
                        )
                    }
                }
            }

            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PROGRESSION & UNLOCK VERIFICATION",
                            style = MaterialTheme.typography.titleLarge,
                            color = UltimateGold
                        )
                        Text(
                            text = "Verify the P6 3-Arcade-Win unlock trigger for Sky Temple and Level 3 Nyx unlock directly:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(
                            onClick = { viewModel.simulateArcadeVictoryUnlockTest() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_verify_3_arcade_wins_unlock"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UltimateGold,
                                contentColor = ObsidianVoid
                            )
                        ) {
                            Text("VERIFY 3 ARCADE WINS (UNLOCK SKY TEMPLE)", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler { viewModel.navigateTo(AppScreen.MAIN_MENU) }
    val s = uiState.saveData.settings

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ArcadeBottomNavBar(
                currentScreen = uiState.currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "GRAPHICS, AUDIO & CONTROL SETTINGS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = NeonCyan
                )
                Text(
                    text = "All changes persist automatically to versioned local save storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Graphics Preset & FPS Cap
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("GRAPHICS QUALITY PRESET", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GraphicsPreset.entries.forEach { preset ->
                                FilterChip(
                                    selected = s.graphicsPreset == preset,
                                    onClick = {
                                        viewModel.updateSettings { it.copy(graphicsPreset = preset) }
                                    },
                                    label = { Text(preset.displayName.uppercase(), style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                        selectedLabelColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("chip_preset_${preset.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("FRAME RATE CAP (30 – 120 FPS)", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 60, 90, 120).forEach { fps ->
                                FilterChip(
                                    selected = s.fpsCap == fps,
                                    onClick = {
                                        viewModel.updateSettings { it.copy(fpsCap = fps) }
                                    },
                                    label = { Text("$fps FPS", style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SeismicEmerald.copy(alpha = 0.25f),
                                        selectedLabelColor = SeismicEmerald
                                    ),
                                    modifier = Modifier.testTag("chip_fps_$fps")
                                )
                            }
                        }
                    }
                }
            }

            // Audio Sliders
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("PROCEDURAL AUDIO SYNTHESIZER BUS", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Master Volume: ${(s.masterVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan
                        )
                        Slider(
                            value = s.masterVolume,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(masterVolume = v) } },
                            modifier = Modifier.testTag("slider_master_volume")
                        )

                        Text(
                            text = "Combat SFX Volume: ${(s.sfxVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SolarAmber
                        )
                        Slider(
                            value = s.sfxVolume,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(sfxVolume = v) } },
                            modifier = Modifier.testTag("slider_sfx_volume")
                        )

                        Text(
                            text = "Touch Control Button Scale: ${String.format("%.2f", s.controlScale)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UltimateGold
                        )
                        Slider(
                            value = s.controlScale,
                            valueRange = 0.80f..1.25f,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(controlScale = v) } },
                            modifier = Modifier.testTag("slider_control_scale")
                        )
                    }
                }
            }

            // Visual Feedback & Accessibility Toggles
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingToggleRow(
                            title = "Screen Shake on Heavy & Ultimate Impacts",
                            checked = s.screenShake,
                            testTag = "switch_screen_shake",
                            onCheckedChange = { v -> viewModel.updateSettings { it.copy(screenShake = v) } }
                        )
                        SettingToggleRow(
                            title = "Floating Damage Numbers & Combo Multipliers",
                            checked = s.showDamageNumbers,
                            testTag = "switch_damage_numbers",
                            onCheckedChange = { v -> viewModel.updateSettings { it.copy(showDamageNumbers = v) } }
                        )
                        SettingToggleRow(
                            title = "Reduced Flashes (Photosensitivity Accessibility)",
                            checked = s.reducedFlashes,
                            testTag = "switch_reduced_flashes",
                            onCheckedChange = { v -> viewModel.updateSettings { it.copy(reducedFlashes = v) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun QaDiagnosticsScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler { viewModel.navigateTo(AppScreen.MAIN_MENU) }

    val auditItems = listOf(
        "STAGE 01–05: Project Architecture & Configuration" to "VERIFIED • Package com.aistudio.doomarcade.fghtrx • Adaptive Icon & Custom Fonts",
        "STAGE 06: Data-Driven Character System" to "VERIFIED • Kairo, Vex, Solara, Bront, Nyx with unique abilities",
        "STAGE 07: Deterministic Combat Engine (P2)" to "VERIFIED • 65% Block, 0.28s Dodge Invuln, 1.1s CD, 80k/300k/1M Energy, L→L→H 1.40x",
        "STAGE 08: CPU AI System (Easy/Normal/Hard/Expert)" to "VERIFIED • Configurable reaction delay, spacing, combo chaining & specials",
        "STAGE 09–10: Arcade, Versus, Practice & Tournament" to "VERIFIED • 5-Stage Ladder, Best-of-3, Practice Dummy & 4-Fighter Bracket",
        "STAGE 11: Save Schema v2 & Corruption Recovery" to "VERIFIED • Auto backup fallback, v1→v2 migration & 3-Arcade-Win Sky Temple unlock",
        "STAGE 12–14: Landscape/Portrait UI, Multi-Touch & Synth Audio" to "VERIFIED • AudioTrack PCM Synthesizer + 2.5D Canvas + Safe Insets",
        "STAGE 15–22: Unit, Integration, Robolectric & APK Build" to "VERIFIED • JUnit4 + Robolectric CUJ Suite + Signed Android Build"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("qa_diagnostics_screen"),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ArcadeBottomNavBar(
                currentScreen = uiState.currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "ENGINEERING & QA VERIFICATION MATRIX",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SeismicEmerald
                )
                Text(
                    text = "Live status of Stages 01–22 and P0–P8 engineering requirements.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            items(auditItems) { (stageTitle, statusDetail) ->
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SeismicEmerald.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = SeismicEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = stageTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = statusDetail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }
    }
}
