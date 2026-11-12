package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CombatMatchScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.ProfileStatsScreen
import com.example.ui.screens.QaDiagnosticsScreen
import com.example.ui.screens.RosterAndUnlocksScreen
import com.example.ui.screens.SelectionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TournamentBracketScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianVoid
import com.example.viewmodel.AppScreen
import com.example.viewmodel.DoomArcadeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianVoid
                ) {
                    DoomArcadeApp()
                }
            }
        }
    }
}

@Composable
fun DoomArcadeApp(
    viewModel: DoomArcadeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(
        targetState = uiState.currentScreen,
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.SPLASH, AppScreen.MAIN_MENU -> MainMenuScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.SELECTION -> SelectionScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.TOURNAMENT_BRACKET -> TournamentBracketScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.COMBAT_MATCH -> CombatMatchScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.ROSTER_UNLOCKS -> RosterAndUnlocksScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.PROFILE_STATS -> ProfileStatsScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(uiState = uiState, viewModel = viewModel)
            AppScreen.QA_DIAGNOSTICS -> QaDiagnosticsScreen(uiState = uiState, viewModel = viewModel)
        }
    }
}
