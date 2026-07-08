package co.coffeery.app.ui.screens.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.R
import co.coffeery.app.data.local.BrewLogEntity
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.Format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BestRecipeSuggestion(
    val equipmentName: String,
    val equipmentId: String,
    val ratioDenominator: Double,
    val tempCelsius: Int,
    val grind: String,
)

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
        equipmentName = topEquipmentName,
        equipmentId = topEquipmentId,
        ratioDenominator = medianOf(top.map { it.ratioDenominator }),
        tempCelsius = if (temps.isNotEmpty()) medianOf(temps) else 93,
        grind = modeOf(top.map { it.grind }),
    )
}

@Composable
fun BrewLogScreen(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = CoffeeTheme.colors

    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 28.dp)) {
        ScreenHeader(title = stringResource(R.string.nav_log))

        if (state.brewLogs.isEmpty()) {
            Spacer(Modifier.height(80.dp))
            AppText(
                stringResource(R.string.log_empty),
                style = CoffeeTheme.type.body,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                val best = bestRecipeFromLogs(state.brewLogs)
                if (best != null) {
                    item(key = "best_recipe") {
                        BestRecipeBanner(best, vm)
                    }
                }
                items(state.brewLogs, key = { it.id }) { log ->
                    BrewLogCard(log, vm)
                }
            }
        }
    }
}

@Composable
private fun BestRecipeBanner(best: BestRecipeSuggestion, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val tempStr = if (best.tempCelsius > 0) ", ${best.tempCelsius}°C" else ""
    val grindStr = best.grind.lowercase().replace('_', ' ')

    CoffeeCard(
        onClick = { vm.selectEquipment(best.equipmentId) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        AppText(
            stringResource(R.string.log_best_recipe_label, best.equipmentName),
            style = CoffeeTheme.type.caption,
            color = colors.accent,
        )
        Spacer(Modifier.height(2.dp))
        AppText(
            "1:${Format.ratio(best.ratioDenominator)}$tempStr, $grindStr grind",
            style = CoffeeTheme.type.headline,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(2.dp))
        AppText(
            stringResource(R.string.log_best_recipe_tap),
            style = CoffeeTheme.type.caption,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun BrewLogCard(log: BrewLogEntity, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var showDelete by remember { mutableStateOf(false) }

    CoffeeCard(
        onClick = { vm.applyBrewLog(log) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                AppText(log.equipmentName, style = CoffeeTheme.type.headline, color = colors.textPrimary)
                Spacer(Modifier.height(2.dp))
                AppText(
                    logDate(log.timestamp) + " · " + stringResource(R.string.calc_grams, Format.grams(log.coffeeGrams)) + " : " + log.waterMl + " ml",
                    style = CoffeeTheme.type.caption,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppText("1:${Format.ratio(log.ratioDenominator)}", style = CoffeeTheme.type.caption, color = colors.accent)
                    if (log.rating > 0) {
                        AppText("\u2605".repeat(log.rating), style = CoffeeTheme.type.caption, color = colors.accent)
                    }
                }
                if (log.customGrindSize.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    AppText(log.customGrindSize, style = CoffeeTheme.type.caption, color = colors.textSecondary)
                }
                if (log.tastingNotes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    AppText(log.tastingNotes, style = CoffeeTheme.type.body, color = colors.textPrimary, maxLines = 2)
                }
                Spacer(Modifier.height(6.dp))
                AppText(
                    stringResource(R.string.log_reproduce),
                    style = CoffeeTheme.type.caption,
                    color = colors.accent,
                )
            }
            AppText(
                "\u2715",
                style = CoffeeTheme.type.caption,
                color = colors.textSecondary,
                modifier = Modifier.padding(start = 8.dp).clickable { showDelete = true },
            )
        }
    }

    if (showDelete) {
        CoffeeDialog(onDismiss = { showDelete = false }) {
            AppText(stringResource(R.string.recipes_delete_confirm), style = CoffeeTheme.type.title)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showDelete = false }
                PrimaryButton(stringResource(R.string.action_delete), Modifier.weight(1f)) {
                    vm.deleteBrewLog(log.id)
                    showDelete = false
                }
            }
        }
    }
}

private fun logDate(timestamp: Long): String {
    val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return fmt.format(Date(timestamp))
}
