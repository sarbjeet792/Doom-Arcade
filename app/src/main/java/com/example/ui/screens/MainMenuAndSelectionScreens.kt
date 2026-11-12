package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.managers.ProgressionCalculator
import com.example.model.AiDifficulty
import com.example.model.ArenaProfile
import com.example.model.FightStyle
import com.example.model.FighterProfile
import com.example.model.GameCatalog
import com.example.model.GameMode
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
fun MainMenuScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    val save = uiState.saveData
    val nextLevelXp = ProgressionCalculator.requiredXpForNextLevel(save.level)
    val xpProgress = (save.xp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)
    val activeFighter = uiState.selectedPlayerProfile

    val infiniteTransition = rememberInfiniteTransition(label = "dashboard_idle")
    val previewAnimSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "preview_anim_sec"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_menu_screen"),
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
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Modern Cyber-Arcade Hero Command Header + Instant Fight CTA
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp))
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.75f), CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_arcade_1794471694303),
                        contentDescription = "Doom Arcade Hero Stage",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ObsidianVoid.copy(alpha = 0.92f),
                                        ObsidianVoid.copy(alpha = 0.68f),
                                        ObsidianVoid.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                                ) {
                                    Text(
                                        text = "OVERDRIVE EDITION • ${save.settings.fpsCap} FPS",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NeonCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "LVL ${save.level} (${save.xp}/$nextLevelXp XP)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = UltimateGold,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "DOOM ARCADE",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Active: ${activeFighter.displayName} • ${uiState.selectedFightStyle.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeFighter.primaryColor
                            )
                        }

                        Button(
                            onClick = { viewModel.launchQuickFight() },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_quick_fight"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UltimateGold,
                                contentColor = ObsidianVoid
                            ),
                            shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QUICK FIGHT", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // 2. Modern 2x2 Bento Combat Modes Grid (All 4 Modes Visible Above the Fold)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BentoModeCard(
                            mode = GameMode.ARCADE,
                            icon = Icons.Default.Whatshot,
                            accentColor = SolarAmber,
                            badgeText = if ("sky_temple" in save.unlockedArenaIds) "SKY TEMPLE OPEN" else "${save.arcadeWins}/3 WINS → SKY TEMPLE",
                            testTag = "mode_card_arcade",
                            onClick = { viewModel.selectGameMode(GameMode.ARCADE) },
                            modifier = Modifier.weight(1f)
                        )
                        BentoModeCard(
                            mode = GameMode.VERSUS,
                            icon = Icons.Default.SportsKabaddi,
                            accentColor = NeonCyan,
                            badgeText = if (uiState.bestOfThreeSeries) "BEST OF 3 SERIES" else "SINGLE MATCH",
                            testTag = "mode_card_versus",
                            onClick = { viewModel.selectGameMode(GameMode.VERSUS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BentoModeCard(
                            mode = GameMode.PRACTICE,
                            icon = Icons.Default.FitnessCenter,
                            accentColor = SeismicEmerald,
                            badgeText = "FRAME & DUMMY DOJO",
                            testTag = "mode_card_practice",
                            onClick = { viewModel.selectGameMode(GameMode.PRACTICE) },
                            modifier = Modifier.weight(1f)
                        )
                        BentoModeCard(
                            mode = GameMode.TOURNAMENT,
                            icon = Icons.Default.EmojiEvents,
                            accentColor = VoidViolet,
                            badgeText = "8-FIGHTER BRACKET",
                            testTag = "mode_card_tournament",
                            onClick = { viewModel.selectGameMode(GameMode.TOURNAMENT) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Interactive Fighter & Fight Style Showcase Hub
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_fighter_hub"),
                    color = CyberSurface,
                    shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, activeFighter.primaryColor.copy(alpha = 0.65f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Live Animated 2.5D Fighter Preview with Facial/Physical Features & Aura
                            Box(
                                modifier = Modifier
                                    .width(115.dp)
                                    .height(135.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(ObsidianVoid, activeFighter.secondaryColor.copy(alpha = 0.45f))
                                        )
                                    )
                                    .border(1.dp, activeFighter.primaryColor, RoundedCornerShape(10.dp))
                            ) {
                                FighterPortraitPreviewCanvas(
                                    fighterProfile = activeFighter,
                                    fightStyle = uiState.selectedFightStyle,
                                    animTimeSec = previewAnimSec,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${activeFighter.displayName.uppercase()} — ${activeFighter.title.uppercase()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = activeFighter.primaryColor
                                )
                                Text(
                                    text = "Style: ${activeFighter.fightingStyle} • HP ${activeFighter.maxHealth} • Crit ${activeFighter.critChanceBp / 100}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Special (300K): ${activeFighter.specialAbility.name} • Ultimate (1M): ${activeFighter.ultimateAbility.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeonCyan,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Quick Fighter Switcher Row
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(GameCatalog.fighters, key = { "dash_${it.id}" }) { f ->
                                        val unlocked = f.id in save.unlockedFighterIds
                                        val isCurrent = f.id == activeFighter.id
                                        FilterChip(
                                            selected = isCurrent,
                                            enabled = unlocked,
                                            onClick = { viewModel.selectPlayerFighter(f.id) },
                                            label = {
                                                Text(
                                                    text = f.displayName.uppercase(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontSize = 10.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = f.primaryColor.copy(alpha = 0.25f),
                                                selectedLabelColor = f.primaryColor
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fight Style Stance Selector Bar
                        Text(
                            text = "COMBAT STANCE / FIGHT STYLE (${uiState.selectedFightStyle.description})",
                            style = MaterialTheme.typography.labelMedium,
                            color = UltimateGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FightStyle.entries.forEach { style ->
                                val selected = uiState.selectedFightStyle == style
                                val styleAccent = when (style) {
                                    FightStyle.RUSH -> NeonCyan
                                    FightStyle.POWER -> SolarAmber
                                    FightStyle.AEGIS -> SeismicEmerald
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.selectFightStyle(style) }
                                        .testTag("dashboard_style_${style.name.lowercase()}"),
                                    color = if (selected) styleAccent.copy(alpha = 0.20f) else CyberSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) styleAccent else CyberBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = style.badge,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) styleAccent else Color.White,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = style.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Player Telemetry & Progression Strip
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.PROFILE_STATS) }
                        .testTag("card_player_progression_summary"),
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${save.playerName} • WINS: ${save.totalWins} • BEST COMBO: ${save.highestComboRecord} HITS",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "UNLOCKS: ${save.unlockedFighterIds.size}/${GameCatalog.fighters.size} FIGHTERS",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonCyan,
                            trackColor = CyberSurfaceElevated
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoModeCard(
    mode: GameMode,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = CyberSurface,
        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor.copy(alpha = 0.68f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CutCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.18f))
                        .border(1.dp, accentColor, CutCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.displayName,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Launch ${mode.displayName}",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mode.displayName.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ArcadeBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = CyberSurface,
        contentColor = Color.White,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.MAIN_MENU,
            onClick = { onNavigate(AppScreen.MAIN_MENU) },
            icon = { Icon(Icons.Default.SportsKabaddi, contentDescription = "Dashboard") },
            label = { Text("HOME", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.testTag("nav_tab_modes")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.ROSTER_UNLOCKS,
            onClick = { onNavigate(AppScreen.ROSTER_UNLOCKS) },
            icon = { Icon(Icons.Default.MilitaryTech, contentDescription = "Fighters") },
            label = { Text("FIGHTERS", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.testTag("nav_tab_roster")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE_STATS,
            onClick = { onNavigate(AppScreen.PROFILE_STATS) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("PROFILE", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.testTag("nav_tab_profile")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SETTINGS,
            onClick = { onNavigate(AppScreen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("SETTINGS", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.testTag("nav_tab_settings")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.QA_DIAGNOSTICS,
            onClick = { onNavigate(AppScreen.QA_DIAGNOSTICS) },
            icon = { Icon(Icons.Default.Security, contentDescription = "QA Audit") },
            label = { Text("QA AUDIT", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.testTag("nav_tab_audit")
        )
    }
}

@Composable
fun SelectionScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    val save = uiState.saveData
    val playerProfile = uiState.selectedPlayerProfile
    val opponentProfile = uiState.selectedOpponentProfile
    val arenaProfile = uiState.selectedArenaProfile

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("selection_screen"),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Surface(
                color = CyberSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${playerProfile.displayName.uppercase()} (${uiState.selectedFightStyle.badge}) VS ${opponentProfile.displayName.uppercase()}",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonCyan
                        )
                        Text(
                            text = "${arenaProfile.displayName} • ${uiState.selectedDifficulty.displayName} AI",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { viewModel.launchSelectedMode() },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("btn_start_match"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = ObsidianVoid
                        ),
                        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.selectedMode == GameMode.TOURNAMENT) "GENERATE BRACKET" else "ENTER ARENA",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_back_from_selection")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Main Menu",
                            tint = NeonCyan
                        )
                    }
                    Column {
                        Text(
                            text = "${uiState.selectedMode.displayName.uppercase()} SETUP",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Text(
                            text = uiState.selectedMode.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 1. Select Player Fighter
            item {
                Text(
                    text = "1. SELECT YOUR FIGHTER",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(GameCatalog.fighters, key = { it.id }) { fighter ->
                        val unlocked = fighter.id in save.unlockedFighterIds
                        val selected = fighter.id == uiState.selectedPlayerFighterId
                        FighterSelectCard(
                            fighter = fighter,
                            unlocked = unlocked,
                            selected = selected,
                            testTag = "select_player_${fighter.id}",
                            onClick = {
                                if (unlocked) viewModel.selectPlayerFighter(fighter.id)
                            }
                        )
                    }
                }
            }

            // Select Fight Style Stance
            item {
                Text(
                    text = "FIGHT STYLE STANCE",
                    style = MaterialTheme.typography.titleLarge,
                    color = UltimateGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FightStyle.entries.forEach { style ->
                        val selected = uiState.selectedFightStyle == style
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectFightStyle(style) },
                            label = {
                                Text(
                                    text = "${style.badge} (${style.displayName})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("select_style_${style.name.lowercase()}")
                        )
                    }
                }
            }

            // Selected Fighter Ability & Stat Breakdown
            item {
                FighterAbilityPreviewPanel(fighter = playerProfile)
            }

            // 2. Opponent Selection or 5-Stage Ladder Preview
            item {
                if (uiState.selectedMode == GameMode.ARCADE) {
                    Text(
                        text = "2. FIVE-STAGE ARCADE LADDER",
                        style = MaterialTheme.typography.titleLarge,
                        color = SolarAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.arcadeLadder.forEach { stage ->
                            val opp = GameCatalog.fighterById(stage.opponentId)
                            val stgArena = GameCatalog.arenaById(stage.arenaId)
                            Surface(
                                color = CyberSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "STAGE ${stage.stageNumber}: ${opp.displayName.uppercase()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = opp.primaryColor
                                    )
                                    Text(
                                        text = "${stgArena.displayName} • ${stage.difficulty.displayName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else if (uiState.selectedMode != GameMode.TOURNAMENT) {
                    Text(
                        text = "2. SELECT CPU OPPONENT",
                        style = MaterialTheme.typography.titleLarge,
                        color = CrimsonKo
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(GameCatalog.fighters, key = { "opp_${it.id}" }) { fighter ->
                            val selected = fighter.id == uiState.selectedOpponentFighterId
                            FighterSelectCard(
                                fighter = fighter,
                                unlocked = true,
                                selected = selected,
                                testTag = "select_opponent_${fighter.id}",
                                onClick = { viewModel.selectOpponentFighter(fighter.id) }
                            )
                        }
                    }
                }
            }

            // 3. Select Arena
            if (uiState.selectedMode != GameMode.ARCADE) {
                item {
                    Text(
                        text = "3. SELECT COMBAT ARENA",
                        style = MaterialTheme.typography.titleLarge,
                        color = UltimateGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(GameCatalog.arenas, key = { it.id }) { arena ->
                            val unlocked = arena.id in save.unlockedArenaIds
                            val selected = arena.id == uiState.selectedArenaId
                            ArenaSelectCard(
                                arena = arena,
                                unlocked = unlocked,
                                selected = selected,
                                currentArcadeWins = save.arcadeWins,
                                testTag = "select_arena_${arena.id}",
                                onClick = {
                                    if (unlocked) viewModel.selectArena(arena.id)
                                }
                            )
                        }
                    }
                }
            }

            // 4. AI Difficulty & Series Format
            if (uiState.selectedMode != GameMode.ARCADE) {
                item {
                    Surface(
                        color = CyberSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CPU AI DIFFICULTY",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AiDifficulty.entries.forEach { diff ->
                                    val selected = uiState.selectedDifficulty == diff
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.selectDifficulty(diff) },
                                        label = {
                                            Text(
                                                text = diff.displayName.uppercase(),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                            selectedLabelColor = NeonCyan
                                        ),
                                        modifier = Modifier.testTag("chip_difficulty_${diff.name.lowercase()}")
                                    )
                                }
                            }

                            if (uiState.selectedMode == GameMode.VERSUS) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Best-of-Three Series (First to 2 Rounds)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Disable for single-round Sudden Death match",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = uiState.bestOfThreeSeries,
                                        onCheckedChange = { viewModel.toggleBestOfThree(it) },
                                        modifier = Modifier.testTag("switch_best_of_three")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FighterSelectCard(
    fighter: FighterProfile,
    unlocked: Boolean,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val borderColor = when {
        selected -> fighter.primaryColor
        unlocked -> CyberBorder
        else -> CyberBorder.copy(alpha = 0.4f)
    }
    Surface(
        modifier = Modifier
            .width(156.dp)
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag(testTag),
        color = if (selected) CyberSurfaceElevated else CyberSurface,
        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(fighter.primaryColor.copy(alpha = 0.22f))
                        .border(1.5.dp, fighter.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (unlocked) {
                        Text(
                            text = fighter.displayName.take(1),
                            style = MaterialTheme.typography.titleLarge,
                            color = fighter.primaryColor
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Fighter",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (selected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = fighter.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = fighter.displayName.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = if (unlocked) Color.White else TextSecondary
            )
            Text(
                text = if (unlocked) fighter.title else fighter.unlockRequirementText,
                style = MaterialTheme.typography.labelMedium,
                color = if (unlocked) fighter.primaryColor else CrimsonKo,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "HP ${fighter.maxHealth} • DEF ${fighter.defenseBp / 100}% • CRIT ${fighter.critChanceBp / 100}%",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FighterAbilityPreviewPanel(fighter: FighterProfile) {
    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, fighter.primaryColor.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${fighter.displayName.uppercase()} — ${fighter.fightingStyle.uppercase()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = fighter.primaryColor
                    )
                    Text(
                        text = fighter.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CyberSurfaceElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SPECIAL (300K EN): ${fighter.specialAbility.name.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${fighter.specialAbility.baseDamage} Base DMG • ${fighter.specialAbility.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = CyberSurfaceElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "ULTIMATE (1M EN): ${fighter.ultimateAbility.name.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = UltimateGold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${fighter.ultimateAbility.baseDamage} Base DMG • ${fighter.ultimateAbility.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArenaSelectCard(
    arena: ArenaProfile,
    unlocked: Boolean,
    selected: Boolean,
    currentArcadeWins: Int,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(190.dp)
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag(testTag),
        color = if (selected) CyberSurfaceElevated else CyberSurface,
        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) arena.gridNeonColor else CyberBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(listOf(arena.skyTopColor, arena.skyBottomColor))
                    )
                    .border(1.dp, arena.gridNeonColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!unlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Arena",
                            tint = UltimateGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$currentArcadeWins/${arena.requiredArcadeWins} ARCADE WINS",
                            style = MaterialTheme.typography.labelMedium,
                            color = UltimateGold
                        )
                    }
                } else {
                    Text(
                        text = arena.ambientWeather,
                        style = MaterialTheme.typography.labelMedium,
                        color = arena.gridNeonColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = arena.displayName.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = if (unlocked) Color.White else TextSecondary
            )
            Text(
                text = arena.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TournamentBracketScreen(
    uiState: DoomArcadeUiState,
    viewModel: DoomArcadeViewModel
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    val bracket = uiState.tournamentBracket

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tournament_bracket_screen"),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }
                    Column {
                        Text(
                            text = "DOOM ARCADE GRAND TOURNAMENT",
                            style = MaterialTheme.typography.headlineLarge,
                            color = UltimateGold
                        )
                        Text(
                            text = "Arena: ${GameCatalog.arenaById(bracket.arenaId).displayName} • Difficulty: ${bracket.difficulty.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "SEMIFINAL BRACKET",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )

                bracket.semifinals.forEach { match ->
                    val fA = GameCatalog.fighterById(match.fighterAId)
                    val fB = GameCatalog.fighterById(match.fighterBId)
                    Surface(
                        color = CyberSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (match.involvesPlayer) NeonCyan else CyberBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${fA.displayName.uppercase()} ${if (match.involvesPlayer) "(YOU)" else ""}",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (match.winnerId == fA.id) SeismicEmerald else fA.primaryColor
                            )
                            Text("VS", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                            Text(
                                text = fB.displayName.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (match.winnerId == fB.id) SeismicEmerald else fB.primaryColor
                            )
                        }
                    }
                }

                Text(
                    text = "CHAMPIONSHIP GRAND FINAL",
                    style = MaterialTheme.typography.titleLarge,
                    color = UltimateGold
                )

                val gf = bracket.grandFinal
                Surface(
                    color = CyberSurfaceElevated,
                    shape = CutCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, UltimateGold)
                ) {
                    if (gf == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Awaiting Semifinal Victors...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    } else {
                        val fA = GameCatalog.fighterById(gf.fighterAId)
                        val fB = GameCatalog.fighterById(gf.fighterBId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fA.displayName.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = fA.primaryColor
                            )
                            Text("GRAND FINAL", style = MaterialTheme.typography.labelLarge, color = UltimateGold)
                            Text(
                                text = fB.displayName.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = fB.primaryColor
                            )
                        }
                    }
                }

                if (bracket.championFighterId != null) {
                    val champ = GameCatalog.fighterById(bracket.championFighterId)
                    Surface(
                        color = UltimateGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, UltimateGold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOURNAMENT CHAMPION: ${champ.displayName.uppercase()}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = UltimateGold
                            )
                            Text(
                                text = if (bracket.championFighterId == bracket.playerFighterId) {
                                    "You conquered the Grand Tournament! +250 Total XP & Nyx Chrono Archon Unlocked!"
                                } else {
                                    "Eliminated in bracket. Train and challenge the tournament again!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btn_tournament_exit")
                ) {
                    Text("MAIN MENU", style = MaterialTheme.typography.labelLarge)
                }

                if (bracket.isActive && bracket.championFighterId == null) {
                    Button(
                        onClick = { viewModel.startCurrentTournamentMatch() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_tournament_fight_next"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UltimateGold,
                            contentColor = ObsidianVoid
                        )
                    ) {
                        Text(
                            text = if (bracket.currentRoundIndex == 0) "FIGHT SEMIFINAL" else "FIGHT GRAND FINAL",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
