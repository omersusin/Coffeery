package co.coffeery.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle

/** Entry point for the design system. Provides colours, type and spacing and
 *  keeps a single identity across light/dark rather than a bare inversion. */
@Composable
fun CoffeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkCoffeeColors else LightCoffeeColors
    CompositionLocalProvider(
        LocalCoffeeColors provides colors,
        LocalCoffeeTypography provides DefaultCoffeeTypography,
        LocalCoffeeSpacing provides CoffeeSpacing(),
        content = content,
    )
}

/** Ambient accessors, mirroring how MaterialTheme exposes its tokens. */
object CoffeeTheme {
    val colors: CoffeeColors
        @Composable @ReadOnlyComposable get() = LocalCoffeeColors.current
    val type: CoffeeTypography
        @Composable @ReadOnlyComposable get() = LocalCoffeeTypography.current
    val spacing: CoffeeSpacing
        @Composable @ReadOnlyComposable get() = LocalCoffeeSpacing.current
}

/** Default text style for the app (used by [co.coffeery.app.ui.components.AppText]). */
val DefaultTextStyle: TextStyle = DefaultCoffeeTypography.body
