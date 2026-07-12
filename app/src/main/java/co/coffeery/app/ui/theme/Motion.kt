package co.coffeery.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Duration.Companion.milliseconds

data class MotionTokens(
    val instant: Long = 50,
    val fast: Long = 150,
    val base: Long = 250,
    val slow: Long = 400,
    val deliberate: Long = 600,
    val easeOut: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f),
    val easeIn: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f),
    val easeInOut: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
)

val LocalMotionTokens = staticCompositionLocalOf { MotionTokens() }

object CoffeeMotion {
    val standard = CubicBezierEasing(0.45f, 0f, 0.15f, 1f)
    val emphasized = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    const val quick = 120
    const val normal = 220
    const val slow = 420

    val press = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh,
    )
    val cardExpand = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val page = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = 0.001f,
    )
    val counter = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
