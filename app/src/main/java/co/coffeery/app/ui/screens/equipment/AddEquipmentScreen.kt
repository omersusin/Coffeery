package co.coffeery.app.ui.screens.equipment

import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.repo.CoffeeRepository
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.Format

@Composable
fun AddEquipmentScreen(vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(BrewCategory.POUR_OVER) }
    val defaults = CoffeeRepository.defaultsFor(category)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.add_equip_title), onBack = { vm.back() })

        AppTextField(
            value = name,
            onValueChange = { name = it },
            hint = stringResource(R.string.add_equip_name_hint),
            modifier = Modifier.fillMaxWidth(),
        )

        AppText(stringResource(R.string.add_equip_pick_category), style = CoffeeTheme.type.headline)
        BrewCategory.entries.forEach { cat ->
            val selected = cat == category
            val mod = if (selected) Modifier.fillMaxWidth().border(2.dp, colors.accent, CoffeeShapes.medium)
            else Modifier.fillMaxWidth()
            CoffeeCard(onClick = { category = cat }, modifier = mod) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LineIcon(cat.glyph(), colors.accent, Modifier.size(28.dp))
                    Column(Modifier.weight(1f)) {
                        AppText(stringResource(cat.labelRes), style = CoffeeTheme.type.headline)
                        AppText(stringResource(cat.descRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    }
                }
            }
        }

        CoffeeCard(modifier = Modifier.fillMaxWidth()) {
            AppText(stringResource(R.string.add_equip_suggested), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Suggestion(stringResource(R.string.calc_ratio), "1:" + Format.ratio(defaults.ratioDefault), Modifier.weight(1f))
                Suggestion(
                    stringResource(R.string.calc_out_temp),
                    "${defaults.tempMin}–${defaults.tempMax}°C",
                    Modifier.weight(1f),
                )
                Suggestion(stringResource(R.string.calc_out_grind), stringResource(defaults.grind.labelRes), Modifier.weight(1f))
            }
        }

        PrimaryButton(
            text = stringResource(R.string.action_add),
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
        ) { vm.addCustomEquipment(name, category) }
    }
}

@Composable
private fun Suggestion(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = CoffeeTheme.colors
    Column(modifier) {
        AppText(label, style = CoffeeTheme.type.caption, color = colors.textSecondary, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        AppText(value, style = CoffeeTheme.type.bodyStrong, color = colors.textPrimary, maxLines = 1)
    }
}
