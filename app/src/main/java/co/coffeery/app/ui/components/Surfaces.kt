package co.coffeery.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

fun Modifier.coffeeElevation(shape: Shape, colors: CoffeeColors): Modifier = this.shadow(
    elevation = 14.dp,
    shape = shape,
    ambientColor = colors.accent.copy(alpha = 0.16f),
    spotColor = colors.cremaDark.copy(alpha = 0.24f),
    clip = false,
)

@Composable
fun CoffeeCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CoffeeShapes.medium,
    onClick: (() -> Unit)? = null,
    contentPadding: Int = 16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CoffeeTheme.colors

    var m = modifier
        .clip(shape)
        .background(colors.surfaceElevated)
        .border(1.dp, colors.outline, shape)
    if (onClick != null) {
        m = m.coffeeClickable { onClick() }
    }
    Column(modifier = m.padding(contentPadding.dp), content = content)
}

@Composable
fun AccentStripeCard(
    modifier: Modifier = Modifier,
    contentPadding: Int = 16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CoffeeTheme.colors
    Row(modifier = modifier.fillMaxWidth().clip(CoffeeShapes.medium).background(colors.accentSoft.copy(alpha = 0.3f))) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(colors.accent))
        Column(modifier = Modifier.padding(contentPadding.dp), content = content)
    }
}

@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CoffeeTheme.colors
    Column(
        modifier = modifier
            .coffeeElevation(CoffeeShapes.medium, colors)
            .clip(CoffeeShapes.medium)
            .background(colors.surfaceElevated)
            .padding(24.dp),
        content = content,
    )
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
