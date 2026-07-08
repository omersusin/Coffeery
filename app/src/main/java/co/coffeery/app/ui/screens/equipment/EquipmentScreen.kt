package co.coffeery.app.ui.screens.equipment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.displayName
import co.coffeery.app.ui.components.displayTag
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppUiState
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.Route
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip

@Composable
fun EquipmentScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val builtIns = state.equipment.filter { !it.isCustom }
    val custom = state.equipment.filter { it.isCustom }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.equipment_title))

        PrimaryButton(
            text = stringResource(R.string.equipment_add),
            modifier = Modifier.fillMaxWidth(),
        ) { vm.openRoute(Route.AddEquipment) }

        AppText(stringResource(R.string.equipment_builtin), style = CoffeeTheme.type.headline, color = colors.textSecondary)
        builtIns.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                pair.forEach { eq ->
                    GearTile(eq, selected = eq.id == state.selectedEquipmentId, modifier = Modifier.weight(1f)) {
                        vm.selectEquipment(eq.id)
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        if (custom.isNotEmpty()) {
            AppText(stringResource(R.string.equipment_your), style = CoffeeTheme.type.headline, color = colors.textSecondary)
            custom.forEach { eq ->
                CustomGearRow(
                    eq = eq,
                    selected = eq.id == state.selectedEquipmentId,
                    onClick = { vm.selectEquipment(eq.id) },
                    onDelete = { vm.deleteCustomEquipment(eq.id) },
                )
            }
        }
    }
}

@Composable
private fun GearTile(eq: Equipment, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = CoffeeTheme.colors
    val outer = if (selected) modifier.border(2.dp, colors.accent, CoffeeShapes.medium) else modifier
    CoffeeCard(onClick = onClick, modifier = outer) {
        LineIcon(eq.category.glyph(), colors.accent, Modifier.size(30.dp))
        Spacer(Modifier.height(10.dp))
        AppText(eq.displayName(), style = CoffeeTheme.type.headline, maxLines = 1)
        val tag = eq.displayTag()
        if (tag != null) {
            AppText(tag, style = CoffeeTheme.type.caption, color = colors.textSecondary, maxLines = 1)
        } else {
            AppText(stringResource(eq.category.labelRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        }
    }
}

@Composable
private fun CustomGearRow(eq: Equipment, selected: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    val colors = CoffeeTheme.colors
    val outer = if (selected) Modifier.fillMaxWidth().border(2.dp, colors.accent, CoffeeShapes.medium) else Modifier.fillMaxWidth()
    CoffeeCard(onClick = onClick, modifier = outer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LineIcon(eq.category.glyph(), colors.accent, Modifier.size(28.dp))
            Column(Modifier.weight(1f)) {
                AppText(eq.displayName(), style = CoffeeTheme.type.headline, maxLines = 1)
                AppText(stringResource(eq.category.labelRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CoffeeShapes.pill)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                AppText(text = "✕", style = CoffeeTheme.type.headline, color = colors.textSecondary)
            }
        }
    }
}
