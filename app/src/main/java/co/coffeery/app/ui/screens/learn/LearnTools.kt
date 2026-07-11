package co.coffeery.app.ui.screens.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.theme.CoffeeColors
import androidx.compose.ui.unit.sp
import co.coffeery.app.R
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

private fun String.filterDecimal(): String {
    var dotSeen = false
    return filterIndexed { i, ch ->
        if (ch == '.' && !dotSeen) { dotSeen = true; true }
        else ch.isDigit() || (i == 0 && ch == '-' && length == 1)
    }
}

@Composable
fun ExtractionCalculatorCard() {
    val colors = CoffeeTheme.colors
    var dose by remember { mutableStateOf("15") }
    var water by remember { mutableStateOf("250") }
    var tds by remember { mutableStateOf("1.3") }

    val doseVal = dose.toDoubleOrNull()?.takeIf { it > 0 } ?: 15.0
    val waterVal = water.toDoubleOrNull()?.takeIf { it > 0 } ?: 250.0
    val tdsVal = tds.toDoubleOrNull()?.takeIf { it in 0.5..3.0 } ?: 1.3

    val ey = if (doseVal > 0) (tdsVal * waterVal / doseVal) else 0.0
    val eyColor = when {
        ey in 18.0..22.0 -> Color(0xFF5A8F3C)
        ey in 16.0..18.0 || ey in 22.0..24.0 -> colors.accent
        else -> Color(0xFFC62828)
    }
    val eyBg = when {
        ey in 18.0..22.0 -> Color(0xFF5A8F3C).copy(alpha = 0.12f)
        ey in 16.0..18.0 || ey in 22.0..24.0 -> colors.accentSoft
        else -> Color(0xFFC62828).copy(alpha = 0.10f)
    }
    val adviceRes = when {
        ey in 18.0..22.0 -> R.string.learn_extraction_ideal
        ey < 18.0 -> R.string.learn_extraction_under
        else -> R.string.learn_extraction_over
    }

    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.learn_extraction_calc_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(2.dp))
        AppText(
            stringResource(R.string.learn_extraction_calc_desc),
            style = CoffeeTheme.type.caption,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(14.dp))

        InputRow(R.string.learn_extraction_dose_label, dose, "g") { dose = it }
        Spacer(Modifier.height(8.dp))
        InputRow(R.string.learn_extraction_water_label, water, "g") { water = it }
        Spacer(Modifier.height(8.dp))
        InputRow(R.string.learn_extraction_tds_label, tds, "%") { tds = it }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CoffeeShapes.small)
                .background(eyBg),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        stringResource(R.string.learn_extraction_result),
                        style = CoffeeTheme.type.label,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(2.dp))
                    AppText(
                        String.format("%.1f%%", ey),
                        style = CoffeeTheme.type.number.copy(fontSize = 28.sp),
                        color = colors.textPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CoffeeShapes.pill)
                        .background(eyColor),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        AppText(
            stringResource(adviceRes),
            style = CoffeeTheme.type.body,
            color = eyColor,
        )
        Spacer(Modifier.height(10.dp))
        AppText(
            stringResource(R.string.learn_extraction_chart),
            style = CoffeeTheme.type.label,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(4.dp))
        BrewControlChart(
            extractionYield = ey.toFloat(),
            tds = tdsVal.toFloat(),
            colors = colors,
        )
    }
}

@Composable
fun BrewControlChart(
    extractionYield: Float,
    tds: Float,
    colors: CoffeeColors,
    modifier: Modifier = Modifier,
) {
    val idealExMin = 18f
    val idealExMax = 22f
    val idealTdsMin = 1.15f
    val idealTdsMax = 1.55f

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val accentDotRadius = with(density) { 10.dp.toPx() }
    val innerDotRadius = with(density) { 5.dp.toPx() }
    val tickLength = with(density) { 4.dp.toPx() }
    val cornerRadiusPx = with(density) { 12.dp.toPx() }
    val borderWidth = with(density) { 1.5.dp.toPx() }
    val labelStyle = TextStyle(fontSize = 10.sp)
    val axisTitleStyle = TextStyle(fontSize = 11.sp)
    val tickColor = colors.outline.copy(alpha = 0.6f)

    Canvas(modifier = modifier.fillMaxWidth().height(240.dp)) {
        val pad = 52f
        val chartW = size.width - pad * 2f
        val chartH = size.height - pad * 2f

        fun x(ex: Float): Float = pad + ((ex - 14f) / 12f) * chartW
        fun y(tdsVal: Float): Float = pad + chartH - ((tdsVal - 0.8f) / 1.0f) * chartH

        val chartLeft = x(14f)
        val chartRight = x(26f)
        val chartTop = y(1.8f)
        val chartBottom = y(0.8f)
        val chartSize = Size(chartRight - chartLeft, chartBottom - chartTop)

        drawRoundRect(
            color = colors.surface,
            topLeft = Offset(chartLeft, chartTop),
            size = chartSize,
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        )

        drawRect(
            color = colors.accent.copy(alpha = 0.30f),
            topLeft = Offset(x(idealExMin), y(idealTdsMax)),
            size = Size(x(idealExMax) - x(idealExMin), y(idealTdsMin) - y(idealTdsMax)),
        )

        drawLine(colors.outline, Offset(chartLeft, pad), Offset(chartRight, pad))
        drawLine(colors.outline, Offset(pad, chartTop), Offset(pad, chartBottom))

        val xLabels = listOf(14f to "14", 18f to "18", 22f to "22", 26f to "26")
        for ((value, text) in xLabels) {
            val tx = x(value)
            drawLine(tickColor, Offset(tx, pad - tickLength), Offset(tx, pad))
            val measured = textMeasurer.measure(text, labelStyle)
            drawText(
                textMeasurer,
                text,
                topLeft = Offset(tx - measured.size.width / 2f, pad + 4f),
                style = labelStyle,
            )
        }

        val yLabels = listOf(0.8f to "0.8", 1.15f to "1.15", 1.55f to "1.55", 1.8f to "1.8")
        for ((value, text) in yLabels) {
            val ty = y(value)
            drawLine(tickColor, Offset(pad, ty), Offset(pad + tickLength, ty))
            val measured = textMeasurer.measure(text, labelStyle)
            drawText(
                textMeasurer,
                text,
                topLeft = Offset(pad - measured.size.width - 6f, ty - measured.size.height / 2f),
                style = labelStyle,
            )
        }

        drawRoundRect(
            color = colors.outline,
            topLeft = Offset(chartLeft, chartTop),
            size = chartSize,
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = borderWidth),
        )

        if (extractionYield > 0 && tds > 0) {
            drawCircle(
                color = colors.accent,
                radius = accentDotRadius,
                center = Offset(x(extractionYield), y(tds)),
            )
            drawCircle(
                color = colors.onAccent,
                radius = innerDotRadius,
                center = Offset(x(extractionYield), y(tds)),
            )
        }

        val exTitle = "Extraction %"
        val exMeasured = textMeasurer.measure(exTitle, axisTitleStyle)
        drawText(
            textMeasurer,
            exTitle,
            topLeft = Offset(
                size.width / 2 - exMeasured.size.width / 2,
                pad - exMeasured.size.height - 6f,
            ),
            style = axisTitleStyle,
        )

        val tdsTitle = "TDS %"
        val tdsMeasured = textMeasurer.measure(tdsTitle, axisTitleStyle)
        drawText(
            textMeasurer,
            tdsTitle,
            topLeft = Offset(
                pad - tdsMeasured.size.width - 8f,
                chartTop + chartSize.height / 2 - tdsMeasured.size.height / 2,
            ),
            style = axisTitleStyle,
        )
    }
}

@Composable
private fun InputRow(
    labelRes: Int,
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit,
) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppText(
            stringResource(labelRes),
            style = CoffeeTheme.type.body,
            color = colors.textSecondary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppTextField(
                value = value,
                onValueChange = { onValueChange(it.filterDecimal()) },
                hint = "",
                modifier = Modifier.width(80.dp),
                keyboardType = KeyboardType.Decimal,
            )
            Spacer(Modifier.width(6.dp))
            AppText(suffix, style = CoffeeTheme.type.label, color = colors.textSecondary)
        }
    }
}

@Composable
fun WaterMineralCard() {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.learn_water_calc_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(2.dp))
        AppText(
            stringResource(R.string.learn_water_calc_desc),
            style = CoffeeTheme.type.caption,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(12.dp))

        TableHeader()
        Spacer(Modifier.height(6.dp))
        TableRow(
            param = stringResource(R.string.learn_water_tds_label),
            range = "75–250",
            target = "150",
            unit = "ppm",
        )
        TableRow(
            param = stringResource(R.string.learn_water_hardness_label),
            range = "50–175",
            target = "68",
            unit = "ppm CaCO₃",
        )
        TableRow(
            param = stringResource(R.string.learn_water_alkalinity_label),
            range = "40–70",
            target = "40",
            unit = "ppm CaCO₃",
        )
        TableRow(
            param = stringResource(R.string.learn_water_ph_label),
            range = "6.5–7.5",
            target = "7.0",
            unit = "",
        )
    }
}

@Composable
private fun TableHeader() {
    val colors = CoffeeTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        AppText(
            "",
            modifier = Modifier.weight(1f),
            style = CoffeeTheme.type.label,
            color = colors.textSecondary,
        )
        AppText(
            stringResource(R.string.learn_water_range),
            modifier = Modifier.width(72.dp),
            style = CoffeeTheme.type.label,
            color = colors.textSecondary,
            align = TextAlign.Center,
        )
        AppText(
            stringResource(R.string.learn_water_target),
            modifier = Modifier.width(48.dp),
            style = CoffeeTheme.type.label,
            color = colors.textSecondary,
            align = TextAlign.Center,
        )
    }
}

@Composable
private fun TableRow(
    param: String,
    range: String,
    target: String,
    unit: String,
) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AppText(param, style = CoffeeTheme.type.body, color = colors.textPrimary)
            if (unit.isNotEmpty()) {
                AppText(unit, style = CoffeeTheme.type.caption, color = colors.textSecondary)
            }
        }
        AppText(
            range,
            modifier = Modifier.width(72.dp),
            style = CoffeeTheme.type.body,
            color = colors.textSecondary,
            align = TextAlign.Center,
        )
        AppText(
            target,
            modifier = Modifier.width(48.dp),
            style = CoffeeTheme.type.body,
            color = colors.accent,
            align = TextAlign.Center,
        )
    }
}
