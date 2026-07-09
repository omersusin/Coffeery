package co.coffeery.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun Modifier.coffeeBackground(): Modifier = this.drawBehind {
    drawRect(color = Color.Black, size = size)
}

