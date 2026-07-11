package co.coffeery.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = CoffeeShapes.pill,
    onClick: () -> Unit,
) {
    val colors = CoffeeTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 120.dp)
            .clip(shape)
            .background(if (enabled) colors.accent else colors.outline)
            .coffeeClickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = text,
            style = CoffeeTheme.type.headline,
            color = if (enabled) colors.onAccent else colors.textSecondary,
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = CoffeeTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 120.dp)
            .clip(CoffeeShapes.pill)
            .background(colors.surface)
            .border(1.5.dp, colors.outline, CoffeeShapes.pill)
            .coffeeClickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = text,
            style = CoffeeTheme.type.headline,
            color = if (enabled) colors.textPrimary else colors.textSecondary,
        )
    }
}
