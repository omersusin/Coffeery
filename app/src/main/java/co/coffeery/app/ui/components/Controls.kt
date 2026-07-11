package co.coffeery.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    subtitle: (@Composable (T) -> String)? = null,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = modifier
            .clip(CoffeeShapes.pill)
            .background(colors.surface)
            .border(1.dp, colors.outline, CoffeeShapes.pill)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val bg = if (isSelected) colors.accent else colors.surface
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CoffeeShapes.pill)
                    .background(bg)
                    .coffeeClickable { onSelect(option) }
                    .padding(vertical = if (subtitle != null) 8.dp else 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (subtitle != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AppText(
                            text = label(option),
                            style = CoffeeTheme.type.label,
                            color = if (isSelected) colors.onAccent else colors.textSecondary,
                            align = TextAlign.Center,
                        )
                        AppText(
                            text = subtitle(option),
                            style = CoffeeTheme.type.caption,
                            color = if (isSelected) colors.onAccent.copy(alpha = 0.7f) else colors.textSecondary.copy(alpha = 0.7f),
                            align = TextAlign.Center,
                        )
                    }
                } else {
                    AppText(
                        text = label(option),
                        style = CoffeeTheme.type.label,
                        color = if (isSelected) colors.onAccent else colors.textSecondary,
                        align = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Rounded +/- stepper for cup count. */
@Composable
fun Stepper(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 12,
) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StepButton("–", enabled = value > min) { onChange((value - 1).coerceAtLeast(min)) }
        AppText(
            text = value.toString(),
            style = CoffeeTheme.type.title,
            color = colors.textPrimary,
        )
        StepButton("+", enabled = value < max) { onChange((value + 1).coerceAtMost(max)) }
    }
}

@Composable
private fun RowScope.StepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = CoffeeTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CoffeeShapes.pill)
            .border(1.5.dp, if (enabled) colors.accent else colors.outline, CoffeeShapes.pill)
            .coffeeClickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = symbol,
            style = CoffeeTheme.type.title,
            color = if (enabled) colors.accent else colors.outline,
        )
    }
}

/** BasicTextField styled to the design system, with a placeholder. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    val colors = CoffeeTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(CoffeeShapes.small)
            .background(colors.surface)
            .border(1.dp, colors.outline, CoffeeShapes.small)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        singleLine = singleLine,
        textStyle = CoffeeTheme.type.body.merge(androidx.compose.ui.text.TextStyle(color = colors.textPrimary)),
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                AppText(text = hint, style = CoffeeTheme.type.body, color = colors.textSecondary)
            }
            inner()
        },
    )
}
