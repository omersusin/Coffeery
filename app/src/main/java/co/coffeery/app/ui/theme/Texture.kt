package co.coffeery.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Random

fun Modifier.coffeeBackground(colors: CoffeeColors): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(colors.background, colors.backgroundEnd),
        ),
        size = size,
    )
    val grainAlpha = 0.03f
    val rng = Random(42)
    val grainCount = (size.width * size.height * 0.0004f).toInt().coerceIn(200, 800)
    for (i in 0 until grainCount) {
        val x = rng.nextFloat() * size.width
        val y = rng.nextFloat() * size.height
        val a = grainAlpha * (0.5f + 0.5f * rng.nextFloat())
        drawCircle(
            color = Color.Black.copy(alpha = a),
            radius = 1f + rng.nextFloat() * 1.5f,
            center = Offset(x, y),
        )
    }
}
