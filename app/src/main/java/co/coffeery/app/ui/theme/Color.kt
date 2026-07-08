package co.coffeery.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Coffeery's own colour tokens — deliberately not Material's ColorScheme.
 * A warm, low-chroma base with a single terracotta accent, so light and dark
 * share one identity rather than being a plain inversion.
 */
@Immutable
data class CoffeeColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val cremaLight: Color,   // slider fill at min strength
    val cremaDark: Color,    // slider fill at max strength
    val isDark: Boolean,
) {
    /** Coffee shade for the strength slider fill; darkens as strength rises. */
    fun coffeeFor(strength: Float): Color =
        lerp(cremaLight, cremaDark, strength.coerceIn(0f, 1f))
}

val LightCoffeeColors = CoffeeColors(
    background = Color(0xFFFBF7F0),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    outline = Color(0xFFE7DDCE),
    textPrimary = Color(0xFF201A14),
    textSecondary = Color(0xFF6E6152),
    accent = Color(0xFFC75B3C),
    accentSoft = Color(0xFFF3D3C4),
    onAccent = Color(0xFFFFFFFF),
    cremaLight = Color(0xFFD9B98C),
    cremaDark = Color(0xFF3B241A),
    isDark = false,
)

val DarkCoffeeColors = CoffeeColors(
    background = Color(0xFF14100D),
    surface = Color(0xFF1E1813),
    surfaceElevated = Color(0xFF2A221B),
    outline = Color(0xFF3A2F26),
    textPrimary = Color(0xFFF5EDE3),
    textSecondary = Color(0xFFB8A895),
    accent = Color(0xFFE0785B),
    accentSoft = Color(0xFF6B3628),
    onAccent = Color(0xFF1A0F0A),
    cremaLight = Color(0xFFC9A57A),
    cremaDark = Color(0xFF1C0F09),
    isDark = true,
)

val LocalCoffeeColors = staticCompositionLocalOf { LightCoffeeColors }
