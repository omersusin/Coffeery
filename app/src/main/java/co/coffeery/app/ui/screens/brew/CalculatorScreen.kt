package co.coffeery.app.ui.screens.brew

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.data.local.BrewLogEntity
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.model.Equipment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import co.coffeery.app.data.model.RoastLevel
import co.coffeery.app.data.model.TempMode
import co.coffeery.app.ui.components.AccentStripeCard
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.Chip
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.EquipmentIcon
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
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.CloudBackupManager
import coil.compose.AsyncImage
import co.coffeery.app.util.BrewMath
import co.coffeery.app.util.BrewResult
import co.coffeery.app.util.Format

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

@Composable
fun CalculatorScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val eq = state.selectedEquipment ?: return
    val result = if (state.ratioMode) {
        BrewResult(
            ratioDenominator = state.manualRatio,
            coffeeGrams = state.coffeeGrams,
            waterMl = state.manualWaterMl,
            grind = eq.grind.shifted(state.roast.grindShift),
            tempMode = eq.tempMode,
            tempCelsius = if (eq.tempMode == TempMode.RANGE) {
                val mid = (eq.tempMin + eq.tempMax) / 2
                (mid + state.roast.tempOffset).coerceIn(eq.tempMin - 2, eq.tempMax)
            } else 0,
            strengthBandRes = R.string.strength_balanced_label,
        )
    } else {
        BrewMath.compute(eq, state.strength, state.roast, state.byCups, state.cups, state.waterMl)
    }
    val ctx = LocalContext.current
    val eqName = eq.displayName()
    val cloud = remember { CloudBackupManager(ctx) }

    // Refresh Google session so photo loads
    LaunchedEffect(Unit) {
        cloud.silentSignIn()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.calc_title),
            trailing = {
                if (cloud.isSignedIn()) {
                    val photoUrl = cloud.getProfilePhotoUrl()
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                vm.openRoute(co.coffeery.app.ui.screens.root.Route.Settings)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val email = cloud.getAccountEmail() ?: "?"
                            Box(
                                Modifier.fillMaxSize().background(CoffeeTheme.colors.accent),
                                contentAlignment = Alignment.Center
                            ) {
                                AppText(
                                    email.first().uppercase(),
                                    color = CoffeeTheme.colors.onAccent
                                )
                            }
                        }
                    }
                } else {
                    LineIcon(
                        Glyph.GEAR,
                        CoffeeTheme.colors.textSecondary,
                        Modifier.size(22.dp).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            vm.openRoute(co.coffeery.app.ui.screens.root.Route.Settings)
                        },
                    )
                }
            },
        )

        val streak = remember(state.brewLogs) { currentStreak(state.brewLogs) }
        if (streak >= 2) {
            AccentStripeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LineIcon(Glyph.FLAME, colors.accent, Modifier.size(22.dp))
                    AppText(
                        stringResource(R.string.brew_reminder_streak, streak),
                        style = CoffeeTheme.type.body,
                        color = colors.textPrimary,
                    )
                }
            }
        }

        CategoryChips(state, vm, eq)

        EquipmentDropdown(state, vm, eq)

        AmountSection(state, vm, eq)

        if (!state.ratioMode) {
            StrengthSection(state, vm, result)
        }

        RoastSection(state, vm)

        OutputSection(result, eq, state)

        CoffeeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIcon(Glyph.CUP, colors.accent, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                AppText(stringResource(R.string.ratio_ref_title), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            }
            Spacer(Modifier.height(6.dp))
            AppText(stringResource(R.string.ratio_1_15), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(2.dp))
            AppText(stringResource(R.string.ratio_1_16), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(2.dp))
            AppText(stringResource(R.string.ratio_1_17), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(2.dp))
            AppText(stringResource(R.string.ratio_1_18), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton(
                text = stringResource(R.string.action_save),
                modifier = Modifier.weight(1f),
            ) {
                val autoName = "$eqName · ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())}"
                vm.saveRecipe(autoName)
                Toast.makeText(ctx, R.string.recipe_saved, Toast.LENGTH_SHORT).show()
            }
            PrimaryButton(
                text = stringResource(R.string.action_start),
                modifier = Modifier.weight(1f),
            ) { vm.openRoute(co.coffeery.app.ui.screens.root.Route.Timer) }
        }
    }

}

@Composable
private fun CategoryChips(state: AppUiState, vm: AppViewModel, eq: Equipment) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrewCategory.entries.forEach { cat ->
            val isSelected = cat == eq.category
            val bg = if (isSelected) colors.accent else colors.accentSoft
            val fg = if (isSelected) colors.onAccent else colors.accent
            Box(
                modifier = Modifier
                    .clip(CoffeeShapes.pill)
                    .background(bg)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        vm.selectCategoryEquipment(cat)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                AppText(stringResource(cat.labelRes), style = CoffeeTheme.type.label, color = fg)
            }
        }
    }
}

@Composable
private fun EquipmentDropdown(state: AppUiState, vm: AppViewModel, eq: Equipment) {
    val colors = CoffeeTheme.colors
    val ctx = LocalContext.current
    val equipmentInCategory = state.equipment.filter { it.category == eq.category }
    var showPicker by remember { mutableStateOf(false) }

        CoffeeCard(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EquipmentIcon(eq, colors.accent, Modifier.size(28.dp))
                AppText(eq.displayName(), style = CoffeeTheme.type.title, modifier = Modifier.weight(1f))
                AppText("▾", style = CoffeeTheme.type.title, color = colors.accent)
            }
        }

        val videoUrl = eq.videoUrl()
        if (videoUrl != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AppText(
                    stringResource(R.string.calc_watch_video),
                    style = CoffeeTheme.type.label,
                    color = colors.accent,
                    modifier = Modifier
                        .clip(CoffeeShapes.pill)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

    if (showPicker) {
        EquipmentPickerDialog(
            equipmentList = equipmentInCategory,
            selectedId = eq.id,
            onDismiss = { showPicker = false },
            onSelect = { vm.selectEquipment(it); showPicker = false },
        )
    }
}

@Composable
private fun EquipmentPickerDialog(
    equipmentList: List<Equipment>,
    selectedId: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val colors = CoffeeTheme.colors
    CoffeeDialog(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppText(stringResource(R.string.calc_choose_gear), style = CoffeeTheme.type.headline)
            Spacer(Modifier.height(6.dp))
            equipmentList.forEach { item ->
                val isSelected = item.id == selectedId
                val bg = if (isSelected) colors.accentSoft else colors.surface
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CoffeeShapes.small)
                        .background(bg)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            onSelect(item.id)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EquipmentIcon(item, colors.accent, Modifier.size(24.dp))
                    AppText(item.displayName(), style = CoffeeTheme.type.body, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        AppText("✓", style = CoffeeTheme.type.label, color = colors.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountSection(state: AppUiState, vm: AppViewModel, eq: Equipment) {
    val colors = CoffeeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SegmentedControl(
            options = listOf(false, true),
            selected = state.ratioMode,
            label = { ratioMode -> if (ratioMode) stringResource(R.string.calc_mode_manual) else stringResource(R.string.calc_mode_auto) },
            onSelect = { vm.toggleRatioMode(it) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.ratioMode) {
            CoffeeCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppText(stringResource(R.string.calc_input_coffee), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppTextField(
                        value = if (state.coffeeGrams == 0.0) "" else Format.grams(state.coffeeGrams),
                        onValueChange = { txt ->
                            val v = txt.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                            vm.setCoffeeGrams(v)
                        },
                        hint = "15.0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppText(stringResource(R.string.calc_input_ratio), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppTextField(
                        value = if (state.manualRatio == 0.0) "" else Format.ratio(state.manualRatio),
                        onValueChange = { txt ->
                            val v = txt.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 16.0
                            vm.setManualRatio(v)
                        },
                        hint = "16",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppText(stringResource(R.string.calc_input_water_ml), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppTextField(
                        value = if (state.manualWaterMl == 0) "" else state.manualWaterMl.toString(),
                        onValueChange = { txt ->
                            val v = txt.filter { it.isDigit() }.toIntOrNull() ?: 0
                            vm.setManualWaterMl(v)
                        },
                        hint = "240",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            SegmentedControl(
                options = listOf(true, false),
                selected = state.byCups,
                label = { byCups -> if (byCups) stringResource(R.string.calc_input_cups) else stringResource(R.string.calc_input_water) },
                subtitle = { byCups -> if (byCups) "${state.cups} cups" else "${state.waterMl} ml" },
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
                textColor = colors.coffeeTextFor(state.strength),
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
    val roastFraction = state.roast.ordinal / (RoastLevel.entries.size - 1).toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppText(stringResource(R.string.calc_roast), style = CoffeeTheme.type.headline)
        SegmentedControl(
            options = RoastLevel.entries.toList(),
            selected = state.roast,
            label = { stringResource(it.labelRes) },
            onSelect = { vm.setRoast(it) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.coffeeFor(roastFraction)),
        )
        AppText(stringResource(state.roast.descRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
    }
}

@Composable
private fun OutputSection(result: co.coffeery.app.util.BrewResult, eq: Equipment, state: AppUiState) {
    val colors = CoffeeTheme.colors
    val grindColor = lerp(colors.cremaLight, colors.cremaDark, result.grind.ordinal / 6f)
    val animatedCoffee by animateFloatAsState(
        targetValue = result.coffeeGrams.toFloat(),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "coffeeAnim",
    )
    val animatedWater by animateFloatAsState(
        targetValue = result.waterMl.toFloat(),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "waterAnim",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppText(stringResource(R.string.calc_out_coffee), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        AppText(
            stringResource(R.string.calc_grams, Format.grams(animatedCoffee.toDouble())),
            style = CoffeeTheme.type.number,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(8.dp))

        AppText(stringResource(R.string.calc_out_water), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        AppText(
            stringResource(R.string.calc_ml, animatedWater.toInt().toString()),
            style = CoffeeTheme.type.number,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(8.dp))

        val tempText = when (result.tempMode) {
            TempMode.RANGE -> {
                if (state.settings.temperatureUnit == "F")
                    Format.tempF(result.tempCelsius)
                else
                    stringResource(R.string.calc_celsius, Format.temp(result.tempCelsius))
            }
            TempMode.SLOW -> stringResource(R.string.calc_temp_slow)
            TempMode.COLD -> stringResource(R.string.calc_temp_cold)
        }
        AppText(stringResource(R.string.calc_out_temp), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        AppText(tempText, style = CoffeeTheme.type.number, color = colors.textPrimary)

        Spacer(Modifier.height(8.dp))

        AppText(stringResource(R.string.calc_out_grind), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        AppText(stringResource(result.grind.labelRes), style = CoffeeTheme.type.number, color = colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        AppText(stringResource(result.grind.refRes), style = CoffeeTheme.type.caption, color = grindColor)

        if (eq.timeLabel.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Row {
                AppText(stringResource(R.string.calc_time) + ": ", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                AppText(eq.timeLabel, style = CoffeeTheme.type.caption, color = colors.textPrimary)
            }
        }
    }
}

