package co.coffeery.app.ui.screens.recipes

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
import androidx.compose.foundation.layout.width
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
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.model.RoastLevel
import co.coffeery.app.ui.components.AccentStripeCard
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.components.displayName
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppUiState
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.NavTab
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.BrewMath
import co.coffeery.app.util.Format
import java.util.Date

@Composable
fun RecipesScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var pendingDelete by remember { mutableStateOf<RecipeEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.recipes_title))

        if (state.recipes.isEmpty()) {
            Spacer(Modifier.height(40.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LineIcon(Glyph.BOOKMARK, colors.textSecondary.copy(alpha = 0.4f), Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                AppText(stringResource(R.string.empty_recipes_title), style = CoffeeTheme.type.title, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppText(stringResource(R.string.empty_recipes_sub), style = CoffeeTheme.type.body, color = colors.textSecondary, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(R.string.empty_recipes_action),
                    onClick = { vm.selectTab(NavTab.BREW) },
                )
            }
        } else {
            val recent = state.recipes.first()
            FeaturedRecipeCard(
                recipe = recent,
                state = state,
                onViewBrew = {
                    vm.loadRecipe(recent)
                    vm.selectTab(NavTab.BREW)
                },
            )

            state.recipes.forEach { recipe ->
                RecipeRow(
                    recipe = recipe,
                    state = state,
                    onClick = {
                        vm.loadRecipe(recipe)
                        vm.selectTab(NavTab.BREW)
                    },
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
private fun FeaturedRecipeCard(recipe: RecipeEntity, state: AppUiState, onViewBrew: () -> Unit) {
    val colors = CoffeeTheme.colors
    val eq = state.equipment.firstOrNull { it.id == recipe.equipmentId }
    val eqName = eq?.displayName() ?: "—"
    val roast = runCatching { RoastLevel.valueOf(recipe.roast) }.getOrDefault(RoastLevel.MEDIUM)
    val result = if (eq != null) {
        BrewMath.compute(eq, recipe.strength, roast, recipe.inputByCups, recipe.cups, recipe.waterMl)
    } else null

    AccentStripeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                AppText(stringResource(R.string.recipe_recent), style = CoffeeTheme.type.label, color = colors.accent)
                Spacer(Modifier.height(4.dp))
                AppText(eqName, style = CoffeeTheme.type.headline, maxLines = 1)
                if (result != null) {
                    Spacer(Modifier.height(2.dp))
                    AppText(
                        "${Format.grams(result.coffeeGrams)}g · ${result.waterMl}ml · 1:${Format.ratio(result.ratioDenominator)}",
                        style = CoffeeTheme.type.caption,
                        color = colors.textSecondary,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            PrimaryButton(
                text = stringResource(R.string.recipe_view_brew),
                onClick = onViewBrew,
            )
        }
    }
}

@Composable
private fun RecipeRow(recipe: RecipeEntity, state: AppUiState, onClick: () -> Unit, onDelete: () -> Unit) {
    val colors = CoffeeTheme.colors
    val eq = state.equipment.firstOrNull { it.id == recipe.equipmentId }
    val eqName = eq?.displayName() ?: "—"
    val roast = runCatching { RoastLevel.valueOf(recipe.roast) }.getOrDefault(RoastLevel.MEDIUM)
    val result = if (eq != null) {
        BrewMath.compute(eq, recipe.strength, roast, recipe.inputByCups, recipe.cups, recipe.waterMl)
    } else null
    val dateStr = android.text.format.DateFormat.getMediumDateFormat(androidx.compose.ui.platform.LocalContext.current)
        .format(Date(recipe.createdAt))
    val strengthLabel = when {
        recipe.strength < 0.34f -> stringResource(R.string.calc_weaker)
        recipe.strength < 0.67f -> stringResource(R.string.strength_balanced_label)
        else -> stringResource(R.string.calc_stronger)
    }

    CoffeeCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(
                (eq?.category ?: BrewCategory.OTHER).glyph(),
                colors.accent,
                Modifier.size(24.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                AppText(eqName, style = CoffeeTheme.type.headline, maxLines = 1)
                if (result != null) {
                    AppText(
                        "${Format.grams(result.coffeeGrams)}g · ${result.waterMl}ml · 1:${Format.ratio(result.ratioDenominator)}",
                        style = CoffeeTheme.type.caption,
                        color = colors.textSecondary,
                        maxLines = 1,
                    )
                }
                AppText(
                    "${stringResource(roast.labelRes)} · $strengthLabel · $dateStr",
                    style = CoffeeTheme.type.caption,
                    color = colors.textSecondary,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CoffeeShapes.pill)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                AppText("✕", style = CoffeeTheme.type.headline, color = colors.textSecondary)
            }
        }
    }
}
