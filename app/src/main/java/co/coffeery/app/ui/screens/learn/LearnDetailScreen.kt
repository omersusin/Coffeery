package co.coffeery.app.ui.screens.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun LearnDetailScreen(cardIndex: Int, vm: AppViewModel) {
    val card = LearnContent.cards.getOrNull(cardIndex) ?: run { vm.back(); return }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScreenHeader(title = "", onBack = { vm.back() })
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(card.titleRes), style = CoffeeTheme.type.display)
        Spacer(Modifier.height(8.dp))
        AppText(stringResource(card.bodyRes), style = CoffeeTheme.type.body, color = CoffeeTheme.colors.textSecondary)
    }
}
