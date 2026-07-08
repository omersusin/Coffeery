package co.coffeery.app.ui.screens.brew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.data.model.RoastLevel
import co.coffeery.app.data.model.TempMode
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.Chip
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.components.SegmentedControl
import co.coffeery.app.ui.components.Stepper
import co.coffeery.app.ui.components.StrengthSlider
import co.coffeery.app.ui.components.displayName
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppUiState
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.BrewMath
import co.coffeery.app.util.Format

@Composable
fun CalculatorScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val eq = state.selectedEquipment ?: return
    val result = BrewMath.compute(eq, state.strength, state.roast, state.byCups, state.cups, state.waterMl)
    var showSave by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.calc_title))

        GearSelector(eq) { vm.selectTab(co.coffeery.app.ui.screens.root.NavTab.GEAR) }

        AmountSection(state, vm, eq)

        StrengthSection(state, vm, result)

        RoastSection(state, vm)

        OutputSection(result, eq)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton(
                text = stringResource(R.string.action_save),
                modifier = Modifier.weight(1f),
            ) { showSave = true }
            PrimaryButton(
                text = stringResource(R.string.action_start),
                modifier = Modifier.weight(1f),
            ) { vm.openRoute(co.coffeery.app.ui.screens.root.Route.Timer) }
        }
    }

    if (showSave) {
        SaveRecipeDialog(
            onDismiss = { showSave = false },
            onSave = { name -> vm.saveRecipe(name); showSave = false },
        )
    }
}

@Composable
private fun GearSelector(eq: Equipment, onClick: () -> Unit) {
    val colors = CoffeeTheme.colors
    CoffeeCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LineIcon(eq.category.glyph(), colors.accent, Modifier.size(34.dp))
            Column(Modifier.weight(1f)) {
                AppText(stringResource(R.string.calc_choose_gear), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                AppText(eq.displayName(), style = CoffeeTheme.type.title, color = colors.textPrimary)
            }
            AppText(stringResource(R.string.calc_change), style = CoffeeTheme.type.label, color = colors.accent)
        }
    }
}

@Composable
private fun AmountSection(state: AppUiState, vm: AppViewModel, eq: Equipment) {
    val colors = CoffeeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SegmentedControl(
            options = listOf(true, false),
            selected = state.byCups,
            label = { byCups -> if (byCups) stringResource(R.string.calc_input_cups) else stringResource(R.string.calc_input_water) },
            onSelect = { vm.setByCups(it) },
            modifier = Modifier.fillMaxWidth(),
        )
        CoffeeCard(modifier = Modifier.fillMaxWidth()) {
            if (state.byCups) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        AppText(stringResource(R.string.calc_input_cups), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                        AppText(
                            stringResource(R.string.calc_cups_value, state.cups),
                            style = CoffeeTheme.type.title,
                        )
                    }
                    Stepper(value = state.cups, onChange = { vm.setCups(it) })
                }
            } else {
                AppText(stringResource(R.string.calc_input_water), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                AppTextField(
                    value = if (state.waterMl == 0) "" else state.waterMl.toString(),
                    onValueChange = { txt -> vm.setWater(txt.filter { it.isDigit() }.toIntOrNull() ?: 0) },
                    hint = "500",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StrengthSection(state: AppUiState, vm: AppViewModel, result: co.coffeery.app.util.BrewResult) {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(stringResource(R.string.calc_strength), style = CoffeeTheme.type.headline, modifier = Modifier.weight(1f))
            Chip(
                text = stringResource(
                    R.string.strength_ratio_summary,
                    Format.ratio(result.ratioDenominator),
                    stringResource(result.strengthBandRes),
                ),
                background = colors.coffeeFor(state.strength),
                textColor = if (state.strength > 0.45f) colors.onAccent else colors.textPrimary,
            )
        }
        Spacer(Modifier.height(10.dp))
        StrengthSlider(value = state.strength, onValueChange = { vm.setStrength(it) }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AppText(stringResource(R.string.calc_weaker), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            AppText(stringResource(R.string.calc_stronger), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        }
    }
}

@Composable
private fun RoastSection(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppText(stringResource(R.string.calc_roast), style = CoffeeTheme.type.headline)
        SegmentedControl(
            options = RoastLevel.entries.toList(),
            selected = state.roast,
            label = { stringResource(it.labelRes) },
            onSelect = { vm.setRoast(it) },
            modifier = Modifier.fillMaxWidth(),
        )
        AppText(stringResource(state.roast.descRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
    }
}

@Composable
private fun OutputSection(result: co.coffeery.app.util.BrewResult, eq: Equipment) {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Metric(R.string.calc_out_coffee, stringResource(R.string.calc_grams, Format.grams(result.coffeeGrams)), Modifier.weight(1f))
            Metric(R.string.calc_out_water, stringResource(R.string.calc_ml, result.waterMl.toString()), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val tempText = when (result.tempMode) {
                TempMode.RANGE -> stringResource(R.string.calc_celsius, Format.temp(result.tempCelsius))
                TempMode.SLOW -> stringResource(R.string.calc_temp_slow)
                TempMode.COLD -> stringResource(R.string.calc_temp_cold)
            }
            Metric(R.string.calc_out_temp, tempText, Modifier.weight(1f))
            Metric(R.string.calc_out_grind, stringResource(result.grind.labelRes), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        AppText(stringResource(result.grind.refRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        if (eq.timeLabel.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Row {
                AppText(stringResource(R.string.calc_time) + ": ", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                AppText(eq.timeLabel, style = CoffeeTheme.type.caption, color = colors.textPrimary)
            }
        }
    }
}

@Composable
private fun Metric(labelRes: Int, value: String, modifier: Modifier = Modifier) {
    val colors = CoffeeTheme.colors
    Column(modifier) {
        AppText(stringResource(labelRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        Spacer(Modifier.height(2.dp))
        AppText(value, style = CoffeeTheme.type.number, color = colors.textPrimary, maxLines = 1)
    }
}

@Composable
private fun SaveRecipeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    CoffeeDialog(onDismiss = onDismiss) {
        AppText(stringResource(R.string.save_recipe_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(14.dp))
        AppTextField(
            value = name,
            onValueChange = { name = it },
            hint = stringResource(R.string.recipe_name_hint),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { onDismiss() }
            PrimaryButton(stringResource(R.string.action_save), Modifier.weight(1f), enabled = name.isNotBlank()) { onSave(name) }
        }
    }
}
