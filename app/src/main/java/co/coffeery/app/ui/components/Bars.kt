package co.coffeery.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

/** Custom bottom navigation. */
@Composable
fun <T> BottomNav(
    items: List<T>,
    selected: T,
    labelFor: @Composable (T) -> String,
    glyphFor: (T) -> Glyph,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CoffeeTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        // hairline top border
        Canvas(Modifier.fillMaxWidth().height(1.dp)) {
            drawLine(colors.outline, Offset(0f, 0f), Offset(size.width, 0f), 2f)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            items.forEach { item ->
                val isSelected = item == selected
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accent else colors.textSecondary,
                    animationSpec = tween(220),
                    label = "navColor",
                )
                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accentSoft else colors.surface,
                    animationSpec = tween(220),
                    label = "navBg",
                )
                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = tween(220),
                    label = "navScale",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onSelect(item) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CoffeeShapes.medium)
                            .background(animatedBgColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            LineIcon(
                                glyphFor(item),
                                animatedColor,
                                Modifier.size(24.dp).scale(animatedScale),
                            )
                            AppText(
                                text = labelFor(item),
                                style = CoffeeTheme.type.caption,
                                color = animatedColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Screen title header with an optional back chevron and trailing slot. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            BackButton(onBack)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            AppText(text = title, style = CoffeeTheme.type.display, color = colors.textPrimary)
            if (subtitle != null) {
                AppText(text = subtitle, style = CoffeeTheme.type.body, color = colors.textSecondary)
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CoffeeTheme.colors
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(42.dp)
            .clip(CoffeeShapes.pill)
            .background(colors.surfaceElevated)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp)) {
            val sw = size.minDimension * 0.1f
            val p = Path().apply {
                moveTo(size.width * 0.62f, size.height * 0.2f)
                lineTo(size.width * 0.36f, size.height * 0.5f)
                lineTo(size.width * 0.62f, size.height * 0.8f)
            }
            drawPath(p, colors.textPrimary, style = Stroke(width = sw, cap = StrokeCap.Round))
        }
    }
}
