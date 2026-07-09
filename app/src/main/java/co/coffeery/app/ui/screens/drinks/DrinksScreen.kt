package co.coffeery.app.ui.screens.drinks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.Route
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun DrinksScreen(vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var searchQuery by remember { mutableStateOf("") }

    val drinkTexts = DrinkContent.drinks.map { drink -> drink to stringResource(drink.nameRes) }
    val filteredDrinks = if (searchQuery.isBlank()) {
        DrinkContent.drinks
    } else {
        drinkTexts.filter { (_, name) -> name.contains(searchQuery, true) }.map { it.first }
    }
    val searchActive = searchQuery.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.drinks_title),
            subtitle = stringResource(R.string.drinks_intro),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                hint = stringResource(R.string.search_hint_drinks),
                modifier = Modifier.fillMaxWidth(),
            )
            if (searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { searchQuery = "" },
                ) {
                    AppText("✕", style = CoffeeTheme.type.headline, color = colors.textSecondary)
                }
            }
        }

        if (searchActive && filteredDrinks.isEmpty()) {
            AppText(
                stringResource(R.string.search_no_results),
                style = CoffeeTheme.type.body,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
            )
        }

        var lastGroup: DrinkGroup? = null
        filteredDrinks.forEachIndexed { index, drink ->
            if (drink.group != lastGroup) {
                lastGroup = drink.group
                Spacer(Modifier.height(2.dp))
                AppText(
                    stringResource(drink.group.labelRes),
                    style = CoffeeTheme.type.label,
                    color = colors.accent,
                )
            }
            CoffeeCard(onClick = { vm.openRoute(Route.DrinkDetail(index)) }, modifier = Modifier.fillMaxWidth()) {
                AppText(stringResource(drink.nameRes), style = CoffeeTheme.type.headline)
                Spacer(Modifier.height(6.dp))
                AppText(
                    stringResource(drink.summaryRes),
                    style = CoffeeTheme.type.body,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                AppText(stringResource(R.string.drink_read_more), style = CoffeeTheme.type.label, color = colors.accent)
            }
        }
    }
}
