package co.coffeery.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.model.StepKind

/** The single, consistent line-art illustration set for the whole app. */
enum class Glyph { DROP, CONE, BOOKMARK, BOOK, CUP, WAVES, ARROW_DOWN, FLAME, SWIRL, FUNNEL, TIMER, PLUS, BEAN }

@Composable
fun LineIcon(
    glyph: Glyph,
    tint: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val sw = size.minDimension * 0.085f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round)
        drawGlyph(glyph, tint, stroke)
    }
}

private fun DrawScope.drawGlyph(glyph: Glyph, tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    when (glyph) {
        Glyph.DROP -> {
            val p = Path().apply {
                moveTo(w * 0.5f, h * 0.14f)
                cubicTo(w * 0.86f, h * 0.5f, w * 0.72f, h * 0.9f, w * 0.5f, h * 0.9f)
                cubicTo(w * 0.28f, h * 0.9f, w * 0.14f, h * 0.5f, w * 0.5f, h * 0.14f)
                close()
            }
            drawPath(p, tint, style = stroke)
        }
        Glyph.CONE, Glyph.FUNNEL -> {
            val p = Path().apply {
                moveTo(w * 0.2f, h * 0.24f)
                lineTo(w * 0.8f, h * 0.24f)
                lineTo(w * 0.56f, h * 0.66f)
                lineTo(w * 0.44f, h * 0.66f)
                close()
            }
            drawPath(p, tint, style = stroke)
            drawLine(tint, Offset(w * 0.5f, h * 0.66f), Offset(w * 0.5f, h * 0.82f), stroke.width, StrokeCap.Round)
        }
        Glyph.BOOKMARK -> {
            val p = Path().apply {
                moveTo(w * 0.28f, h * 0.14f)
                lineTo(w * 0.72f, h * 0.14f)
                lineTo(w * 0.72f, h * 0.86f)
                lineTo(w * 0.5f, h * 0.68f)
                lineTo(w * 0.28f, h * 0.86f)
                close()
            }
            drawPath(p, tint, style = stroke)
        }
        Glyph.BOOK -> {
            val p = Path().apply {
                moveTo(w * 0.5f, h * 0.24f)
                cubicTo(w * 0.36f, h * 0.16f, w * 0.2f, h * 0.18f, w * 0.16f, h * 0.22f)
                lineTo(w * 0.16f, h * 0.78f)
                cubicTo(w * 0.2f, h * 0.74f, w * 0.36f, h * 0.72f, w * 0.5f, h * 0.8f)
                cubicTo(w * 0.64f, h * 0.72f, w * 0.8f, h * 0.74f, w * 0.84f, h * 0.78f)
                lineTo(w * 0.84f, h * 0.22f)
                cubicTo(w * 0.8f, h * 0.18f, w * 0.64f, h * 0.16f, w * 0.5f, h * 0.24f)
                close()
            }
            drawPath(p, tint, style = stroke)
            drawLine(tint, Offset(w * 0.5f, h * 0.24f), Offset(w * 0.5f, h * 0.8f), stroke.width, StrokeCap.Round)
        }
        Glyph.CUP -> {
            val p = Path().apply {
                moveTo(w * 0.24f, h * 0.34f)
                lineTo(w * 0.68f, h * 0.34f)
                lineTo(w * 0.62f, h * 0.74f)
                cubicTo(w * 0.6f, h * 0.8f, w * 0.32f, h * 0.8f, w * 0.3f, h * 0.74f)
                close()
            }
            drawPath(p, tint, style = stroke)
            // handle
            val hp = Path().apply {
                moveTo(w * 0.68f, h * 0.4f)
                cubicTo(w * 0.86f, h * 0.4f, w * 0.86f, h * 0.62f, w * 0.66f, h * 0.62f)
            }
            drawPath(hp, tint, style = stroke)
        }
        Glyph.WAVES -> {
            for (i in 0..2) {
                val y = h * (0.36f + i * 0.16f)
                val p = Path().apply {
                    moveTo(w * 0.18f, y)
                    cubicTo(w * 0.34f, y - h * 0.08f, w * 0.5f, y + h * 0.08f, w * 0.66f, y)
                    cubicTo(w * 0.74f, y - h * 0.04f, w * 0.8f, y - h * 0.04f, w * 0.84f, y)
                }
                drawPath(p, tint, style = stroke)
            }
        }
        Glyph.ARROW_DOWN -> {
            drawLine(tint, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.74f), stroke.width, StrokeCap.Round)
            val p = Path().apply {
                moveTo(w * 0.3f, h * 0.54f)
                lineTo(w * 0.5f, h * 0.78f)
                lineTo(w * 0.7f, h * 0.54f)
            }
            drawPath(p, tint, style = stroke)
        }
        Glyph.FLAME -> {
            val p = Path().apply {
                moveTo(w * 0.5f, h * 0.16f)
                cubicTo(w * 0.78f, h * 0.42f, w * 0.72f, h * 0.86f, w * 0.5f, h * 0.86f)
                cubicTo(w * 0.28f, h * 0.86f, w * 0.24f, h * 0.5f, w * 0.44f, h * 0.44f)
                cubicTo(w * 0.44f, h * 0.58f, w * 0.56f, h * 0.6f, w * 0.5f, h * 0.16f)
                close()
            }
            drawPath(p, tint, style = stroke)
        }
        Glyph.SWIRL -> {
            drawArc(
                color = tint,
                startAngle = 40f,
                sweepAngle = 280f,
                useCenter = false,
                topLeft = Offset(w * 0.2f, h * 0.2f),
                size = Size(w * 0.6f, h * 0.6f),
                style = stroke,
            )
            val p = Path().apply {
                moveTo(w * 0.74f, h * 0.36f)
                lineTo(w * 0.82f, h * 0.32f)
                lineTo(w * 0.84f, h * 0.46f)
            }
            drawPath(p, tint, style = stroke)
        }
        Glyph.TIMER -> {
            drawCircle(tint, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.55f), style = stroke)
            drawLine(tint, Offset(w * 0.42f, h * 0.14f), Offset(w * 0.58f, h * 0.14f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.5f, h * 0.38f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.62f, h * 0.6f), stroke.width, StrokeCap.Round)
        }
        Glyph.PLUS -> {
            drawLine(tint, Offset(w * 0.5f, h * 0.24f), Offset(w * 0.5f, h * 0.76f), stroke.width, StrokeCap.Round)
            drawLine(tint, Offset(w * 0.24f, h * 0.5f), Offset(w * 0.76f, h * 0.5f), stroke.width, StrokeCap.Round)
        }
        Glyph.BEAN -> {
            drawOval(tint, style = stroke, topLeft = Offset(w * 0.2f, h * 0.2f), size = Size(w * 0.6f, h * 0.6f))
            val p = Path().apply {
                moveTo(w * 0.5f, h * 0.22f)
                cubicTo(w * 0.38f, h * 0.4f, w * 0.62f, h * 0.6f, w * 0.5f, h * 0.78f)
            }
            drawPath(p, tint, style = stroke)
        }
    }
}

/** Map a step kind to its illustration. */
fun StepKind.glyph(): Glyph = when (this) {
    StepKind.BLOOM, StepKind.POUR, StepKind.RINSE -> Glyph.DROP
    StepKind.SWIRL, StepKind.STIR -> Glyph.SWIRL
    StepKind.STEEP, StepKind.SKIM -> Glyph.WAVES
    StepKind.PRESS, StepKind.PLUNGE -> Glyph.ARROW_DOWN
    StepKind.DRAWDOWN -> Glyph.FUNNEL
    StepKind.HEAT, StepKind.REMOVE_HEAT -> Glyph.FLAME
    StepKind.SERVE -> Glyph.CUP
    StepKind.WAIT -> Glyph.TIMER
}

/** Map a category to its illustration for equipment tiles. */
fun BrewCategory.glyph(): Glyph = when (this) {
    BrewCategory.POUR_OVER -> Glyph.CONE
    BrewCategory.IMMERSION -> Glyph.CUP
    BrewCategory.PRESSURE -> Glyph.ARROW_DOWN
    BrewCategory.OTHER -> Glyph.BEAN
}
