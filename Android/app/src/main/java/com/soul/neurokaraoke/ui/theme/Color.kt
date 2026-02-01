package com.soul.neurokaraoke.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// NEURO THEME COLORS (Cyan/Blue)
// ==========================================
val NeuroBackground = Color(0xFF121318)
val NeuroSurface = Color(0xFF1E2330)
val NeuroSurfaceVariant = Color(0xFF252B3B)
val NeuroPrimary = Color(0xFF00D9FF)      // Cyan accent
val NeuroSecondary = Color(0xFFFF6B9D)    // Pink accent
val NeuroTertiary = Color(0xFF8B5CF6)     // Purple for variety
val NeuroGradientStart = Color(0xFF00D9FF)  // Cyan (gradient start)
val NeuroGradientEnd = Color(0xFF00D9FF)    // Cyan (solid for Neuro)
val NeuroOnBackground = Color(0xFFFFFFFF)
val NeuroOnSurface = Color(0xFFB0B8C1)    // Muted text
val NeuroOnSurfaceVariant = Color(0xFF6B7280)
val NeuroDivider = Color(0xFF2A3142)

// ==========================================
// EVIL THEME COLORS (Pink/Magenta)
// ==========================================
val EvilBackground = Color(0xFF120A0E)       // Dark reddish-purple
val EvilSurface = Color(0xFF1E1318)          // Slightly lighter
val EvilSurfaceVariant = Color(0xFF2A1B22)   // Even lighter for cards
val EvilPrimary = Color(0xFFE91E8C)          // Hot pink/magenta
val EvilSecondary = Color(0xFFFF6B9D)        // Lighter pink
val EvilTertiary = Color(0xFFB34D80)         // Muted pink
val EvilGradientStart = Color(0xFFE91E8C)    // Hot pink (gradient start)
val EvilGradientEnd = Color(0xFF9C27B0)      // Purple (gradient end)
val EvilOnBackground = Color(0xFFFFFFFF)
val EvilOnSurface = Color(0xFFCBB0BC)        // Pinkish muted text
val EvilOnSurfaceVariant = Color(0xFF8B6B7A)
val EvilDivider = Color(0xFF3A2530)

// ==========================================
// DUET THEME COLORS (Amethyst Purple)
// ==========================================
val DuetBackground = Color(0xFF0E0A14)       // Dark purple
val DuetSurface = Color(0xFF1A1424)          // Slightly lighter
val DuetSurfaceVariant = Color(0xFF261E32)   // Even lighter for cards
val DuetPrimary = Color(0xFF9C5FD4)          // Amethyst purple
val DuetSecondary = Color(0xFFB388E8)        // Lighter lavender
val DuetTertiary = Color(0xFF7B4AAF)         // Deeper purple
val DuetGradientStart = Color(0xFF9C5FD4)    // Amethyst (gradient start)
val DuetGradientEnd = Color(0xFF6B3FA0)      // Deep purple (gradient end)
val DuetOnBackground = Color(0xFFFFFFFF)
val DuetOnSurface = Color(0xFFC4B8D4)        // Lavender muted text
val DuetOnSurfaceVariant = Color(0xFF8A7A9E)
val DuetDivider = Color(0xFF352A45)

// ==========================================
// SHARED / LEGACY COLORS
// ==========================================
// Keep these for backward compatibility
val Background = NeuroBackground
val Surface = NeuroSurface
val SurfaceVariant = NeuroSurfaceVariant
val Primary = NeuroPrimary
val Secondary = NeuroSecondary
val Tertiary = NeuroTertiary
val OnBackground = NeuroOnBackground
val OnSurface = NeuroOnSurface
val OnSurfaceVariant = NeuroOnSurfaceVariant
val OnPrimary = Color(0xFF000000)
val OnSecondary = Color(0xFF000000)

// Additional colors for UI elements
val CardBorder = Primary
val DividerColor = NeuroDivider
val ErrorColor = Color(0xFFFF5252)
val SuccessColor = Color(0xFF4CAF50)

// Singer-specific colors
val NeuroColor = Color(0xFF00D9FF)   // Cyan for Neuro
val EvilColor = Color(0xFFE91E8C)    // Pink for Evil
val DuetColor = Color(0xFF8B5CF6)    // Purple for Duet
val OtherColor = Color(0xFF6B7280)   // Gray for Other
