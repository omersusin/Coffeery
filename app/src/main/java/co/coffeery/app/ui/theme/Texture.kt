package co.coffeery.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import kotlin.random.Random

@Composable
fun Modifier.coffeeBackground(): Modifier {
    val bg = CoffeeTheme.colors.background
    return this.drawWithCache {
        val random = Random(42)
        val density = 4
        val w = size.width.toInt().coerceAtLeast(1)
        val h = size.height.toInt().coerceAtLeast(1)
        val bitmap = ImageBitmap(w, h)
        val canvas = Canvas(bitmap)
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val alpha = random.nextFloat() * 0.02f
                if (alpha > 0.002f) {
                    canvas.drawRect(
                        Offset(x.toFloat(), y.toFloat()),
                        Size(density.toFloat(), density.toFloat()),
                        Paint().apply { color = Color.Black.copy(alpha = alpha) },
                    )
                }
                x += density
            }
            y += density
        }
        onDrawWithContent {
            drawRect(color = bg, size = size)
            drawContent()
            drawImage(bitmap)
        }
    }
}
