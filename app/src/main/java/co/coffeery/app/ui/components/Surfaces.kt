package co.coffeery.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun CoffeeCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CoffeeShapes.medium,
    onClick: (() -> Unit)? = null,
    contentPadding: Int = 16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CoffeeTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && onClick != null) 0.985f else 1f, label = "card")

    var m = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .background(colors.surfaceElevated)
        .border(1.dp, colors.outline, shape)
    if (onClick != null) {
        m = m.clickable(interaction, indication = null) { onClick() }
    }
    Column(modifier = m.padding(contentPadding.dp), content = content)
}

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = CoffeeTheme.colors.accentSoft,
    textColor: Color = CoffeeTheme.colors.accent,
) {
    Box(
        modifier = modifier
            .clip(CoffeeShapes.pill)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        AppText(text = text, style = CoffeeTheme.type.label, color = textColor)
    }
}
