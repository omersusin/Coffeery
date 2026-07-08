package co.coffeery.app.ui.screens.root

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.ui.components.BottomNav
import co.coffeery.app.ui.screens.brew.BrewTimerScreen
import co.coffeery.app.ui.screens.brew.CalculatorScreen
import co.coffeery.app.ui.screens.equipment.AddEquipmentScreen
import co.coffeery.app.ui.screens.drinks.DrinkDetailScreen
import co.coffeery.app.ui.screens.drinks.DrinksScreen
import co.coffeery.app.ui.screens.equipment.EquipmentScreen
import co.coffeery.app.ui.screens.learn.LearnDetailScreen
import co.coffeery.app.ui.screens.learn.LearnScreen
import co.coffeery.app.ui.screens.log.BrewLogScreen
import co.coffeery.app.ui.screens.recipes.RecipesScreen
import co.coffeery.app.ui.theme.CoffeeTheme

private val routeTransition =
    slideInHorizontally { it / 4 } + fadeIn() togetherWith
        slideOutHorizontally { -it / 4 } + fadeOut()

@Composable
fun RootScreen(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.route !is Route.Tabs) { vm.back() }
    CoffeeTheme(themeMode = state.themeMode) {
        val colors = CoffeeTheme.colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AnimatedContent(
                        targetState = state.route,
                        transitionSpec = { routeTransition },
                        label = "route",
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

@Composable
private fun TabContent(state: AppUiState, vm: AppViewModel) {
    AnimatedContent(
        targetState = state.tab,
        transitionSpec = { routeTransition },
        label = "tab",
    ) { tab ->
        when (tab) {
            NavTab.BREW -> CalculatorScreen(state, vm)
            NavTab.GEAR -> EquipmentScreen(state, vm)
            NavTab.RECIPES -> RecipesScreen(state, vm)
            NavTab.LOG -> BrewLogScreen(vm)
            NavTab.DRINKS -> DrinksScreen(vm)
            NavTab.LEARN -> LearnScreen(vm)
        }
    }
}
