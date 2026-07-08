package co.coffeery.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import co.coffeery.app.ui.theme.CoffeeTheme

/** BasicText wrapper — our replacement for Material's Text component. */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = CoffeeTheme.type.body,
    color: Color = Color.Unspecified,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val resolvedColor = if (color == Color.Unspecified) CoffeeTheme.colors.textPrimary else color
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(TextStyle(color = resolvedColor, textAlign = align ?: TextAlign.Start)),
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AppText(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = CoffeeTheme.type.body,
    color: Color = Color.Unspecified,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AppText(stringResource(textRes), modifier, style, color, align, maxLines, overflow)
