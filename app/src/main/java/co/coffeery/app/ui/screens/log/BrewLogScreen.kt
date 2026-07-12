package co.coffeery.app.ui.screens.log

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.R
import co.coffeery.app.data.local.BrewLogEntity
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.ui.components.AccentStripeCard
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.Chip
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.SegmentedControl
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.NavTab
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.BrewPdfExporter
import co.coffeery.app.util.Format
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle

data class BestRecipeSuggestion(
    val equipmentName: String,
    val equipmentId: String,
    val ratioDenominator: Double,
    val tempCelsius: Int,
    val grind: String,
)

private fun currentStreak(logs: List<BrewLogEntity>): Int {
    val days = logs.map {
        Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSet()
    var streak = 0
    var date = LocalDate.now()
    while (days.contains(date)) {
        streak++
        date = date.minusDays(1)
    }
    if (streak == 0 && !days.contains(LocalDate.now())) return 0
    return streak
}

private fun bestRecipeFromLogs(logs: List<BrewLogEntity>): BestRecipeSuggestion? {
    val rated = logs.filter { it.rating > 0 }
    if (rated.size < 3) return null
    val sorted = rated.sortedByDescending { it.rating }
    val topCount = (sorted.size * 0.3).toInt().coerceAtLeast(3)
    val top = sorted.take(topCount)
    fun medianOf(values: List<Double>): Double = values.sorted().let { it[it.size / 2] }
    fun medianOf(values: List<Int>): Int = values.sorted().let { it[it.size / 2] }
    fun modeOf(values: List<String>): String = values.groupBy { it }.maxByOrNull { it.value.size }?.key ?: ""
    val topEquipmentName = modeOf(top.map { it.equipmentName })
    if (topEquipmentName.isBlank()) return null
    val topEquipmentId = top.firstOrNull { it.equipmentName == topEquipmentName }?.equipmentId ?: return null
    val temps = top.map { it.tempCelsius }.filter { it > 0 }
    return BestRecipeSuggestion(
        equipmentName = topEquipmentName, equipmentId = topEquipmentId,
        ratioDenominator = medianOf(top.map { it.ratioDenominator }),
        tempCelsius = if (temps.isNotEmpty()) medianOf(temps) else 93,
        grind = modeOf(top.map { it.grind }),
    )
}

private fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "${m}m ${s}s"
}

private fun shareBrewAsText(context: Context, log: BrewLogEntity) {
    val stars = if (log.rating > 0) "★".repeat(log.rating) else ""
    val text = buildString {
        appendLine("☕ ${log.equipmentName}")
        appendLine("${log.coffeeGrams}g · ${log.waterMl}ml · 1:${log.ratioDenominator}")
        appendLine("Grind: ${log.grind} · ${log.tempCelsius}°C · ${formatDuration(log.totalDurationSec)}")
        if (stars.isNotEmpty()) appendLine("Rating: $stars")
        if (log.tastingNotes.isNotBlank()) appendLine("\"${log.tastingNotes}\"")
        appendLine()
        appendLine("Brewed with Coffeery ☕")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share brew"))
}

@Composable
fun BrewLogScreen(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var section by remember { mutableIntStateOf(0) }
    val sections = listOf(
        stringResource(R.string.nav_log),
        stringResource(R.string.beans_title),
        stringResource(R.string.caffeine_title),
        stringResource(R.string.stats_title),
        stringResource(R.string.achievements_title),
    )

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 24.dp)) {
        SegmentedControl(
            options = listOf(0, 1, 2, 3, 4),
            selected = section,
            label = { sections[it] },
            onSelect = { section = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        when (section) {
            0 -> BrewLogContent(state, vm)
            1 -> BeanListScreen(vm)
            2 -> CaffeineContent(state.brewLogs)
            3 -> BrewStatsSection(state.brewLogs)
            4 -> AchievementsContent(state.achievements)
        }
    }
}

@Composable
private fun BrewHeatmap(brewLogs: List<BrewLogEntity>) {
    val colors = CoffeeTheme.colors
    val today = LocalDate.now()
    val mondayOfThisWeek = today.with(DayOfWeek.MONDAY)

    val countsByDate: Map<LocalDate, Int> = brewLogs
        .groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        .mapValues { it.value.size }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
        Row(modifier = Modifier.padding(start = 28.dp)) {
            for (col in 0 until 12) {
                val weekDate = mondayOfThisWeek.minusWeeks((11 - col).toLong())
                val show = col == 0 || weekDate.month != mondayOfThisWeek.minusWeeks((11 - (col - 1)).toLong()).month
                Box(modifier = Modifier.width(14.dp)) {
                    if (show) {
                        AppText(
                            weekDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                            style = CoffeeTheme.type.caption,
                            color = colors.textSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        Row {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                val dayLabels = listOf("M", "", "W", "", "F", "", "Su")
                for (row in 0 until 7) {
                    Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
                        if (dayLabels[row].isNotEmpty()) {
                            AppText(dayLabels[row], style = CoffeeTheme.type.caption, color = colors.textSecondary)
                        }
                    }
                }
            }

            Column {
                for (row in 0 until 7) {
                    Row {
                        for (col in 0 until 12) {
                            val date = mondayOfThisWeek.minusWeeks((11 - col).toLong()).plusDays(row.toLong())
                            val isFuture = date.isAfter(today)
                            val count = countsByDate[date] ?: 0
                            val cellColor = when {
                                isFuture -> Color.Transparent
                                count >= 3 -> colors.accent
                                count == 2 -> colors.accentSoft
                                count == 1 -> colors.accentSoft.copy(alpha = 0.4f)
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                                    .then(
                                        if (!isFuture && count == 0)
                                            Modifier.border(1.dp, colors.outline, RoundedCornerShape(3.dp))
                                        else Modifier
                                    ),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(stringResource(R.string.log_brew_count), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).border(1.dp, colors.outline, RoundedCornerShape(3.dp)))
            Spacer(Modifier.width(2.dp))
            AppText("0", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(colors.accentSoft.copy(alpha = 0.4f)))
            Spacer(Modifier.width(2.dp))
            AppText("1", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(colors.accentSoft))
            Spacer(Modifier.width(2.dp))
            AppText("2", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(colors.accent))
            Spacer(Modifier.width(2.dp))
            AppText("3+", style = CoffeeTheme.type.caption, color = colors.textSecondary)
        }
    }
}

@Composable
private fun StreakBanner(streak: Int) {
    val colors = CoffeeTheme.colors
    val context = LocalContext.current
    val label = stringResource(R.string.streak_label)
    val subText = if (streak > 0) stringResource(R.string.streak_keep_going) else stringResource(R.string.streak_start)
    AccentStripeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText("$streak", style = CoffeeTheme.type.number, color = colors.accent)
                Spacer(Modifier.width(8.dp))
                AppText(label, style = CoffeeTheme.type.body, color = colors.textSecondary)
            }
            LineIcon(Glyph.FLAME, colors.accent, Modifier.size(28.dp))
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(subText, style = CoffeeTheme.type.caption, color = colors.textSecondary, modifier = Modifier.weight(1f))
            if (streak >= 7) {
                val shareText = stringResource(R.string.log_share_streak_text, streak)
                SecondaryButton(
                    text = stringResource(R.string.log_share_streak),
                    modifier = Modifier,
                ) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        }
    }
}

@Composable
private fun BrewLogContent(state: co.coffeery.app.ui.screens.root.AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var showCompare by remember { mutableStateOf(false) }

    if (state.brewLogs.isEmpty()) {
        Spacer(Modifier.height(40.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LineIcon(Glyph.CUP, colors.accent, Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            AppText(stringResource(R.string.log_empty_title),
                style = CoffeeTheme.type.title, align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppText(stringResource(R.string.log_empty_sub),
                style = CoffeeTheme.type.body, color = colors.textSecondary,
                align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.log_start_brewing),
                onClick = { vm.selectTab(NavTab.BREW) },
            )
        }
    } else {
        val streak = currentStreak(state.brewLogs)
        val grouped = state.brewLogs
            .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
            .toList()
            .sortedByDescending { it.first }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            StreakBanner(streak)
            BrewHeatmap(state.brewLogs)
            if (state.brewLogs.size >= 2) {
                CoffeeCard(modifier = Modifier.fillMaxWidth().clickable { showCompare = true }, contentPadding = 14) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            AppText(stringResource(R.string.log_compare_title), style = CoffeeTheme.type.headline, color = colors.textPrimary)
                            Spacer(Modifier.height(2.dp))
                            AppText(stringResource(R.string.log_compare_sub), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                        }
                        PrimaryButton(text = stringResource(R.string.log_compare_button), modifier = Modifier) { showCompare = true }
                    }
                }
            }
            if (state.brewLogs.size >= 3) {
                AnalyticsCard(state.brewLogs)
            }
            val best = bestRecipeFromLogs(state.brewLogs)
            if (best != null) {
                BestRecipeBanner(best, vm)
            }
            grouped.forEach { (date, logs) ->
                val dateLabel = when {
                    date == LocalDate.now() -> stringResource(R.string.log_date_today)
                    date == LocalDate.now().minusDays(1) -> stringResource(R.string.log_date_yesterday)
                    else -> date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))
                }
                Spacer(Modifier.height(4.dp))
                AppText(
                    dateLabel,
                    style = CoffeeTheme.type.label,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 2.dp),
                )
                logs.forEach { log ->
                    BrewLogCard(log, state.equipment, vm)
                }
            }
        }
        if (showCompare) {
            CompareBrewsDialog(state.brewLogs, state.equipment) { showCompare = false }
        }
    }
}

@Composable
private fun AnalyticsCard(brewLogs: List<BrewLogEntity>) {
    val colors = CoffeeTheme.colors
    val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
    val weeklyLogs = brewLogs.filter { it.timestamp >= weekAgo }
    if (weeklyLogs.isEmpty()) return

    val totalThisWeek = weeklyLogs.size

    val favoriteMethod = weeklyLogs
        .groupBy { it.equipmentName }
        .maxByOrNull { it.value.size }?.key ?: ""

    val ratedLogs = brewLogs.filter { it.rating > 0 }
    val avgRating = if (ratedLogs.isNotEmpty()) {
        String.format(Locale.US, "%.1f", ratedLogs.map { it.rating }.average())
    } else null

    val bestBean = brewLogs
        .filter { it.beanName.isNotBlank() && it.rating > 0 }
        .groupBy { it.beanName }
        .mapValues { (_, logs) -> logs.map { it.rating }.average() }
        .maxByOrNull { it.value }
        ?.let { if (it.value > 0) it.key else null }

    CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(stringResource(R.string.log_analytics_week), style = CoffeeTheme.type.label, color = colors.textSecondary)
            AppText("$totalThisWeek", style = CoffeeTheme.type.display, color = colors.accent)
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                AppText(stringResource(R.string.log_analytics_method), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                AppText(favoriteMethod, style = CoffeeTheme.type.headline, color = colors.textPrimary)
            }
            if (avgRating != null) {
                Column(horizontalAlignment = Alignment.End) {
                    AppText(stringResource(R.string.log_analytics_rating), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppText("$avgRating★", style = CoffeeTheme.type.headline, color = colors.textPrimary)
                }
            }
        }

        if (bestBean != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppText(stringResource(R.string.log_analytics_bean), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                AppText(bestBean, style = CoffeeTheme.type.headline, color = colors.accent)
            }
        }
    }
}

@Composable
private fun BestRecipeBanner(best: BestRecipeSuggestion, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    AccentStripeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        val grindStr = best.grind.lowercase().replace("_", "-")
        val tempStr = if (best.tempCelsius > 0) ", ${best.tempCelsius}°C" else ""
        AppText(stringResource(R.string.log_best_recipe_label, best.equipmentName), style = CoffeeTheme.type.caption, color = colors.accent)
        Spacer(Modifier.height(2.dp))
        AppText("1:${Format.ratio(best.ratioDenominator)}$tempStr, $grindStr grind", style = CoffeeTheme.type.headline, color = colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        AppText(stringResource(R.string.log_best_recipe_tap), style = CoffeeTheme.type.caption, color = colors.textSecondary)
    }
}

@Composable
private fun CompareBrewsDialog(logs: List<BrewLogEntity>, equipmentList: List<Equipment>, onDismiss: () -> Unit) {
    val colors = CoffeeTheme.colors
    var brew1Index by remember { mutableIntStateOf(-1) }
    var brew2Index by remember { mutableIntStateOf(-1) }

    CoffeeDialog(onDismiss = onDismiss) {
        AppText(stringResource(R.string.log_compare_title), style = CoffeeTheme.type.title, color = colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(R.string.log_compare_select), style = CoffeeTheme.type.body, color = colors.textSecondary)
        Spacer(Modifier.height(14.dp))

        CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 8) {
            logs.forEachIndexed { index, log ->
                val glyph = equipmentList.firstOrNull { it.id == log.equipmentId }?.category?.glyph() ?: Glyph.CUP
                val selected = index == brew1Index || index == brew2Index
                val bg = if (selected) colors.accentSoft.copy(alpha = 0.3f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CoffeeShapes.small)
                        .background(bg)
                        .clickable {
                            if (brew1Index == -1 || brew2Index != -1) { brew1Index = index; brew2Index = -1 }
                            else if (brew1Index != index) brew2Index = index
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LineIcon(glyph, if (selected) colors.accent else colors.textSecondary, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    AppText(log.equipmentName, style = CoffeeTheme.type.headline, color = colors.textPrimary, modifier = Modifier.weight(1f))
                    AppText(logDate(log.timestamp), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                }
            }
        }

        if (brew1Index >= 0 && brew2Index >= 0) {
            val log1 = logs[brew1Index]
            val log2 = logs[brew2Index]
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    val g1 = equipmentList.firstOrNull { it.id == log1.equipmentId }?.category?.glyph() ?: Glyph.CUP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineIcon(g1, colors.accent, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        AppText(log1.equipmentName, style = CoffeeTheme.type.headline, color = colors.textPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    AppText("${Format.grams(log1.coffeeGrams)}g \u00B7 ${log1.waterMl}ml \u00B7 1:${Format.ratio(log1.ratioDenominator)}", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppText("${log1.grind} \u00B7 ${log1.tempCelsius}\u00B0C", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    if (log1.rating > 0) AppText("\u2605".repeat(log1.rating), style = CoffeeTheme.type.caption, color = colors.accent)
                    AppText(formatDuration(log1.totalDurationSec), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    if (log1.tastingNotes.isNotBlank()) AppText("\u201C${log1.tastingNotes}\u201D", style = CoffeeTheme.type.caption, color = colors.textPrimary, maxLines = 2)
                }
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    val g2 = equipmentList.firstOrNull { it.id == log2.equipmentId }?.category?.glyph() ?: Glyph.CUP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineIcon(g2, colors.accent, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        AppText(log2.equipmentName, style = CoffeeTheme.type.headline, color = colors.textPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    AppText("${Format.grams(log2.coffeeGrams)}g \u00B7 ${log2.waterMl}ml \u00B7 1:${Format.ratio(log2.ratioDenominator)}", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppText("${log2.grind} \u00B7 ${log2.tempCelsius}\u00B0C", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    if (log2.rating > 0) AppText("\u2605".repeat(log2.rating), style = CoffeeTheme.type.caption, color = colors.accent)
                    AppText(formatDuration(log2.totalDurationSec), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    if (log2.tastingNotes.isNotBlank()) AppText("\u201C${log2.tastingNotes}\u201D", style = CoffeeTheme.type.caption, color = colors.textPrimary, maxLines = 2)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SecondaryButton(stringResource(R.string.action_close), Modifier.fillMaxWidth()) { onDismiss() }
    }
}

@Composable
private fun BrewLogCard(log: BrewLogEntity, equipment: List<Equipment>, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var showDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val glyph = equipment.firstOrNull { it.id == log.equipmentId }?.category?.glyph() ?: Glyph.CUP
    CoffeeCard(onClick = { vm.applyBrewLog(log) }, modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                LineIcon(glyph, colors.accent, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                AppText(log.equipmentName, style = CoffeeTheme.type.headline, color = colors.textPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(stringResource(R.string.log_share), style = CoffeeTheme.type.caption, color = colors.accent, modifier = Modifier.clickable { shareBrewAsText(context, log) })
                Spacer(Modifier.width(8.dp))
                AppText(stringResource(R.string.log_share_pdf), style = CoffeeTheme.type.caption, color = colors.accent, modifier = Modifier.clickable {
                    context.startActivity(Intent.createChooser(BrewPdfExporter.export(context, log), context.getString(R.string.log_share_pdf_chooser)))
                })
                Spacer(Modifier.width(12.dp))
                AppText("\u2715", style = CoffeeTheme.type.caption, color = colors.textSecondary, modifier = Modifier.padding(start = 0.dp).clickable { showDelete = true })
            }
        }
        Spacer(Modifier.height(4.dp))
        AppText(logDate(log.timestamp), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppText(stringResource(R.string.calc_grams, Format.grams(log.coffeeGrams)) + " : " + log.waterMl + " ml", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            AppText("1:${Format.ratio(log.ratioDenominator)}", style = CoffeeTheme.type.caption, color = colors.accent)
            if (log.rating > 0) AppText(stringResource(R.string.log_rating_out, log.rating), style = CoffeeTheme.type.caption, color = colors.accent)
        }
        if (log.customGrindSize.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            AppText(log.customGrindSize, style = CoffeeTheme.type.caption, color = colors.textSecondary)
        }
        if (log.flavorTags.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            val tags = log.flavorTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.take(4).forEach { tag ->
                    Chip(tag, background = colors.accentSoft, textColor = colors.accent)
                }
                if (tags.size > 4) {
                    AppText("+${tags.size - 4}", style = CoffeeTheme.type.caption, color = colors.textSecondary, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
        if (log.tastingNotes.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            AppText("\u201C${log.tastingNotes}\u201D", style = CoffeeTheme.type.body, color = colors.textPrimary, maxLines = 2)
        }
        if (!log.photoUri.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            AsyncImage(
                model = Uri.parse(log.photoUri),
                contentDescription = "Brew photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(CoffeeShapes.medium),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(Modifier.height(6.dp))
        AppText(stringResource(R.string.log_reproduce), style = CoffeeTheme.type.caption, color = colors.accent)
    }
    if (showDelete) {
        CoffeeDialog(onDismiss = { showDelete = false }) {
            AppText(stringResource(R.string.recipes_delete_confirm), style = CoffeeTheme.type.title)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showDelete = false }
                PrimaryButton(stringResource(R.string.action_delete), Modifier.weight(1f)) { vm.deleteBrewLog(log.id); showDelete = false }
            }
        }
    }
}

private fun estimateCaffeine(coffeeGrams: Double, method: String): Int {
    val mgPerGram = when {
        method.contains("espresso", true) -> 8.0
        method.contains("press", true) || method.contains("moka", true) -> 14.0
        else -> 12.0
    }
    return (coffeeGrams * mgPerGram).toInt()
}

@Composable
private fun CaffeineContent(brewLogs: List<BrewLogEntity>) {
    val colors = CoffeeTheme.colors
    val today = LocalDate.now()
    val todayLogs = brewLogs.filter {
        Instant.ofEpochMilli(it.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() == today
    }

    if (todayLogs.isEmpty()) {
        Spacer(Modifier.height(40.dp))
        AppText(
            stringResource(R.string.caffeine_none),
            style = CoffeeTheme.type.body,
            color = colors.textSecondary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        )
        return
    }

    val totalCaffeine = todayLogs.sumOf { estimateCaffeine(it.coffeeGrams, it.equipmentName) }
    val brewsCount = todayLogs.size
    val maxMg = 600
    val fillRatio = (totalCaffeine.coerceAtMost(maxMg).toFloat() / maxMg)

    val barColor = when {
        totalCaffeine <= 200 -> colors.accentSoft
        totalCaffeine <= 400 -> colors.accent
        else -> colors.cremaDark
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
                AppText(stringResource(R.string.caffeine_today), style = CoffeeTheme.type.label, color = colors.textSecondary)
                Spacer(Modifier.height(4.dp))
                AppText(
                    "$totalCaffeine ${stringResource(R.string.caffeine_total, brewsCount)}",
                    style = CoffeeTheme.type.display,
                    color = barColor,
                )
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(colors.outline.copy(alpha = 0.3f))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillRatio)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(barColor),
                    )
                }
                Spacer(Modifier.height(8.dp))
                AppText(stringResource(R.string.caffeine_limit), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            }

        todayLogs.forEach { log ->
            val mg = estimateCaffeine(log.coffeeGrams, log.equipmentName)
            CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 10) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        AppText(log.equipmentName, style = CoffeeTheme.type.headline, color = colors.textPrimary)
                        AppText(
                            "${Format.grams(log.coffeeGrams)} · ~$mg mg caffeine",
                            style = CoffeeTheme.type.caption,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

private fun logDate(timestamp: Long): String {
    val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return fmt.format(Date(timestamp))
}
