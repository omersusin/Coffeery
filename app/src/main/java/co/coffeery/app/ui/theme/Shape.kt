package co.coffeery.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Corner + spacing system. Generous rounding is part of the identity. */
object CoffeeShapes {
    val small = RoundedCornerShape(10.dp)
    val medium = RoundedCornerShape(18.dp)
    val large = RoundedCornerShape(26.dp)
    val pill = RoundedCornerShape(50)
}

class CoffeeSpacing {
    val xs: Dp = 4.dp
    val s: Dp = 8.dp
    val m: Dp = 12.dp
    val l: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}

val LocalCoffeeSpacing = staticCompositionLocalOf { CoffeeSpacing() }
