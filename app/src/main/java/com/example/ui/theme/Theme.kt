package com.example.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DoomArcadeColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001F26),
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = NeonCyan,
    secondary = SolarAmber,
    onSecondary = Color(0xFF2B1000),
    secondaryContainer = Color(0xFF4A1E00),
    onSecondaryContainer = Color(0xFFFFB580),
    tertiary = VoidViolet,
    onTertiary = Color(0xFF230046),
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = CrimsonKo,
    onError = Color.White
)

val ArcadeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
    medium = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DoomArcadeColorScheme,
        typography = Typography,
        shapes = ArcadeShapes,
        content = content
    )
}
