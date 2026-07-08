package co.coffeery.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeTheme

/**
 * Bespoke strength slider. The filled portion is coloured like coffee and
 * darkens as the value rises — a deliberate micro-interaction, not a stock
 * Material slider.
 */
@Composable
fun StrengthSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CoffeeTheme.colors
    var width by remember { mutableFloatStateOf(0f) }

    fun update(x: Float) {
        if (width > 0f) onValueChange((x / width).coerceIn(0f, 1f))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .onSizeChanged { width = it.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { offset -> update(offset.x) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> update(offset.x) },
                    onDrag = { change, _ -> change.consume(); update(change.position.x) },
                )
            },
    ) {
        val trackH = 16.dp.toPx()
        val thumbR = 14.dp.toPx()
        val cy = size.height / 2f
        val corner = CornerRadius(trackH / 2f, trackH / 2f)
        val v = value.coerceIn(0f, 1f)

        // Background track
        drawRoundRect(
            color = colors.outline,
            topLeft = Offset(0f, cy - trackH / 2f),
            size = Size(size.width, trackH),
            cornerRadius = corner,
        )
        // Coffee-coloured fill
        val fillW = v * size.width
        if (fillW > 0f) {
            drawRoundRect(
                color = colors.coffeeFor(v),
                topLeft = Offset(0f, cy - trackH / 2f),
                size = Size(fillW.coerceAtLeast(trackH), trackH),
                cornerRadius = corner,
            )
        }
        // Thumb: filled disc + accent ring + coffee core
        val tx = fillW.coerceIn(thumbR, size.width - thumbR)
        drawCircle(color = colors.surfaceElevated, radius = thumbR, center = Offset(tx, cy))
        drawCircle(
            color = colors.accent,
            radius = thumbR,
            center = Offset(tx, cy),
            style = Stroke(width = 3.dp.toPx()),
        )
        drawCircle(color = colors.coffeeFor(v), radius = thumbR * 0.45f, center = Offset(tx, cy))
    }
}
