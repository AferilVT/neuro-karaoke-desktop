package com.soul.neurokaraoke.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Theme mode enum
enum class ThemeMode {
    NEURO, EVIL, DUET, AUTO
}

// Composition local to access current theme mode
val LocalThemeMode = compositionLocalOf { ThemeMode.NEURO }

// Composition local to change theme
val LocalThemeToggle = staticCompositionLocalOf<(ThemeMode) -> Unit> { {} }

// Composition local for auto-theme: provides current singer for auto theme switching
// Values: "NEURO", "EVIL", "DUET", "OTHER" (or null if no song playing)
val LocalAutoThemeSinger = compositionLocalOf<String?> { null }

// Neuro (Cyan) color scheme
private val NeuroColorScheme = darkColorScheme(
    primary = NeuroPrimary,
    secondary = NeuroSecondary,
    tertiary = NeuroTertiary,
    background = NeuroBackground,
    surface = NeuroSurface,
    surfaceVariant = NeuroSurfaceVariant,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = NeuroOnBackground,
    onSurface = NeuroOnSurface,
    onSurfaceVariant = NeuroOnSurfaceVariant,
    error = ErrorColor,
    outline = NeuroDivider
)

// Evil (Pink) color scheme
private val EvilColorScheme = darkColorScheme(
    primary = EvilPrimary,
    secondary = EvilSecondary,
    tertiary = EvilTertiary,
    background = EvilBackground,
    surface = EvilSurface,
    surfaceVariant = EvilSurfaceVariant,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = EvilOnBackground,
    onSurface = EvilOnSurface,
    onSurfaceVariant = EvilOnSurfaceVariant,
    error = ErrorColor,
    outline = EvilDivider
)

// Duet (Amethyst Purple) color scheme
private val DuetColorScheme = darkColorScheme(
    primary = DuetPrimary,
    secondary = DuetSecondary,
    tertiary = DuetTertiary,
    background = DuetBackground,
    surface = DuetSurface,
    surfaceVariant = DuetSurfaceVariant,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = DuetOnBackground,
    onSurface = DuetOnSurface,
    onSurfaceVariant = DuetOnSurfaceVariant,
    error = ErrorColor,
    outline = DuetDivider
)

private val NeuroKaraokeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun NeuroKaraokeTheme(
    currentSinger: String? = null,
    content: @Composable () -> Unit
) {
    var themeMode by remember { mutableStateOf(ThemeMode.AUTO) }

    // Determine the effective color scheme
    val colorScheme = when (themeMode) {
        ThemeMode.NEURO -> NeuroColorScheme
        ThemeMode.EVIL -> EvilColorScheme
        ThemeMode.DUET -> DuetColorScheme
        ThemeMode.AUTO -> {
            // Auto-switch based on current singer
            when (currentSinger) {
                "EVIL" -> EvilColorScheme
                "DUET" -> DuetColorScheme
                "NEURO", "OTHER", null -> NeuroColorScheme
                else -> NeuroColorScheme
            }
        }
    }

    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalThemeToggle provides { newMode -> themeMode = newMode },
        LocalAutoThemeSinger provides currentSinger
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = NeuroKaraokeShapes,
            content = content
        )
    }
}

// Helper to get the effective theme based on mode and current singer
@Composable
private fun effectiveTheme(): ThemeMode {
    val themeMode = LocalThemeMode.current
    val currentSinger = LocalAutoThemeSinger.current

    return if (themeMode == ThemeMode.AUTO) {
        when (currentSinger) {
            "EVIL" -> ThemeMode.EVIL
            "DUET" -> ThemeMode.DUET
            else -> ThemeMode.NEURO
        }
    } else {
        themeMode
    }
}

// Helper to get theme-aware primary color
@Composable
fun themeAwarePrimary(): Color {
    return when (effectiveTheme()) {
        ThemeMode.NEURO -> NeuroPrimary
        ThemeMode.EVIL -> EvilPrimary
        ThemeMode.DUET -> DuetPrimary
        ThemeMode.AUTO -> NeuroPrimary // Fallback, shouldn't reach here
    }
}

// Helper to get theme-aware horizontal gradient brush
@Composable
fun themeAwareGradient(): Brush {
    return when (effectiveTheme()) {
        ThemeMode.NEURO -> Brush.horizontalGradient(
            colors = listOf(NeuroGradientStart, NeuroGradientEnd)
        )
        ThemeMode.EVIL -> Brush.horizontalGradient(
            colors = listOf(EvilGradientStart, EvilGradientEnd)
        )
        ThemeMode.DUET -> Brush.horizontalGradient(
            colors = listOf(DuetGradientStart, DuetGradientEnd)
        )
        ThemeMode.AUTO -> Brush.horizontalGradient(
            colors = listOf(NeuroGradientStart, NeuroGradientEnd)
        )
    }
}

// Helper to get theme-aware gradient colors as a list
@Composable
fun themeAwareGradientColors(): List<Color> {
    return when (effectiveTheme()) {
        ThemeMode.NEURO -> listOf(NeuroGradientStart, NeuroGradientEnd)
        ThemeMode.EVIL -> listOf(EvilGradientStart, EvilGradientEnd)
        ThemeMode.DUET -> listOf(DuetGradientStart, DuetGradientEnd)
        ThemeMode.AUTO -> listOf(NeuroGradientStart, NeuroGradientEnd)
    }
}
