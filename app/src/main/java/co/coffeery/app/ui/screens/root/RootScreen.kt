package co.coffeery.app.ui.screens.root

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.R
import co.coffeery.app.ui.components.BottomNav
import co.coffeery.app.ui.components.SegmentedControl
import co.coffeery.app.ui.screens.brew.BrewTimerScreen
import co.coffeery.app.ui.screens.brew.CalculatorScreen
import co.coffeery.app.ui.screens.equipment.AddEquipmentScreen
import co.coffeery.app.ui.screens.drinks.DrinkDetailScreen
import co.coffeery.app.ui.screens.drinks.DrinksScreen
import co.coffeery.app.ui.screens.equipment.EquipmentScreen
import co.coffeery.app.ui.screens.learn.LearnDetailScreen
import co.coffeery.app.ui.screens.learn.LearnScreen
import co.coffeery.app.ui.screens.log.BrewLogScreen
import co.coffeery.app.ui.screens.onboarding.OnboardingScreen
import co.coffeery.app.ui.screens.recipes.RecipesScreen
import co.coffeery.app.ui.theme.CoffeeMotion
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.ui.theme.coffeeBackground

@Composable
fun RootScreen(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.route !is Route.Tabs) { vm.back() }
    CoffeeTheme(themeMode = state.themeMode, palette = state.palette) {
    if (!state.hasCompletedOnboarding) {
        OnboardingScreen(vm)
    } else {
        val colors = CoffeeTheme.colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .coffeeBackground(colors),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                AnimatedContent(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    targetState = state.route,
                    transitionSpec = {
                        val forward = targetState !is Route.Tabs
                        if (forward) {
                            (slideInHorizontally(tween(CoffeeMotion.normal, easing = CoffeeMotion.standard)) { it / 4 } + fadeIn(tween(CoffeeMotion.normal)))
                                .togetherWith(fadeOut(tween(CoffeeMotion.quick)))
                        } else {
                            fadeIn(tween(CoffeeMotion.normal))
                                .togetherWith(slideOutHorizontally(tween(CoffeeMotion.normal, easing = CoffeeMotion.standard)) { -it / 4 } + fadeOut(tween(CoffeeMotion.quick)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "routeTransition",
                ) { route ->
                    when (route) {
                        is Route.Timer -> BrewTimerScreen(state, vm)
                        is Route.AddEquipment -> AddEquipmentScreen(vm)
                        is Route.LearnDetail -> LearnDetailScreen(route.cardIndex, vm)
                        is Route.DrinkDetail -> DrinkDetailScreen(route.index, vm)
                        is Route.Settings -> SettingsScreen(vm)
                        is Route.Tabs -> TabContent(state, vm)
                    }
                }
                if (state.route is Route.Tabs) {
                    BottomNav(
                        items = NavTab.entries.toList(),
                        selected = state.tab,
                        labelFor = { stringResource(it.labelRes) },
                        glyphFor = { it.glyph },
                        onSelect = { vm.selectTab(it) },
                    )
                }
            }
        }
    }
    }
}

private enum class RecipesSubTab { BREW_RECIPES, DRINKS }

@Composable
private fun TabContent(state: AppUiState, vm: AppViewModel) {
    key(state.tab) {
        when (state.tab) {
            NavTab.BREW -> CalculatorScreen(state, vm)
            NavTab.GEAR -> EquipmentScreen(state, vm)
            NavTab.RECIPES -> {
                var recipeSubTab by rememberSaveable { mutableStateOf(RecipesSubTab.BREW_RECIPES) }
                Column(modifier = Modifier.fillMaxSize()) {
                    SegmentedControl(
                        options = RecipesSubTab.entries.toList(),
                        selected = recipeSubTab,
                        label = { stringResource(if (it == RecipesSubTab.BREW_RECIPES) R.string.nav_recipes else R.string.nav_drinks) },
                        onSelect = { recipeSubTab = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                    when (recipeSubTab) {
                        RecipesSubTab.BREW_RECIPES -> RecipesScreen(state, vm)
                        RecipesSubTab.DRINKS -> DrinksScreen(vm)
                    }
                }
            }
            NavTab.LOG -> BrewLogScreen(vm)
            NavTab.LEARN -> LearnScreen(vm)
        }
    }
}
