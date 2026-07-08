package co.coffeery.app.ui.screens.recipes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.data.local.RecipeEntity
import co.coffeery.app.data.model.RoastLevel
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.components.displayName
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppUiState
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.BrewMath
import co.coffeery.app.util.Format

@Composable
fun RecipesScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var pendingDelete by remember { mutableStateOf<RecipeEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.recipes_title))

        if (state.recipes.isEmpty()) {
            Spacer(Modifier.height(40.dp))
            AppText(
                stringResource(R.string.recipes_empty),
                style = CoffeeTheme.type.body,
                color = colors.textSecondary,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            state.recipes.forEach { recipe ->
                RecipeRow(
                    recipe = recipe,
                    state = state,
                    onClick = { vm.applyRecipe(recipe) },
                    onDelete = { pendingDelete = recipe },
                )
            }
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        CoffeeDialog(onDismiss = { pendingDelete = null }) {
            AppText(stringResource(R.string.recipes_delete_confirm), style = CoffeeTheme.type.title)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { pendingDelete = null }
                PrimaryButton(stringResource(R.string.action_delete), Modifier.weight(1f)) {
                    vm.deleteRecipe(toDelete.id); pendingDelete = null
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(recipe: RecipeEntity, state: AppUiState, onClick: () -> Unit, onDelete: () -> Unit) {
    val colors = CoffeeTheme.colors
    val eq = state.equipment.firstOrNull { it.id == recipe.equipmentId }
    val eqName = eq?.displayName() ?: "—"
    val summary = if (eq != null) {
        val roast = runCatching { RoastLevel.valueOf(recipe.roast) }.getOrDefault(RoastLevel.MEDIUM)
        val result = BrewMath.compute(eq, recipe.strength, roast, recipe.inputByCups, recipe.cups, recipe.waterMl)
        stringResource(R.string.recipes_summary, eqName, Format.grams(result.coffeeGrams), result.waterMl.toString())
    } else eqName

    CoffeeCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LineIcon((eq?.category ?: co.coffeery.app.data.model.BrewCategory.OTHER).glyph(), colors.accent, Modifier.size(28.dp))
            Column(Modifier.weight(1f)) {
                AppText(recipe.name, style = CoffeeTheme.type.headline, maxLines = 1)
                AppText(summary, style = CoffeeTheme.type.caption, color = colors.textSecondary, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CoffeeShapes.pill)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                AppText("✕", style = CoffeeTheme.type.headline, color = colors.textSecondary)
            }
        }
    }
}
