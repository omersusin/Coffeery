package co.coffeery.app.ui.screens.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.Chip
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.Route
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun LearnScreen(vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.learn_title),
            subtitle = stringResource(R.string.learn_intro),
        )

        TroubleshootCard()

        LearnContent.cards.forEachIndexed { index, card ->
            CoffeeCard(onClick = { vm.openRoute(Route.LearnDetail(index)) }, modifier = Modifier.fillMaxWidth()) {
                AppText(stringResource(card.titleRes), style = CoffeeTheme.type.headline)
                Spacer(Modifier.height(6.dp))
                AppText(
                    stringResource(card.bodyRes),
                    style = CoffeeTheme.type.body,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                AppText(stringResource(R.string.learn_read_more), style = CoffeeTheme.type.label, color = colors.accent)
            }
        }
    }
}

@Composable
private fun TroubleshootCard() {
    val colors = CoffeeTheme.colors
    var selected by remember { mutableStateOf<Int?>(null) }
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.learn_troubleshoot_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(R.string.learn_troubleshoot_intro), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        Spacer(Modifier.height(12.dp))
        // Simple wrapping rows of chips (3 per row) — no experimental FlowRow.
        LearnContent.tasteOptions.withIndex().chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                rowItems.forEach { (index, option) ->
                    val isSel = selected == index
                    Chip(
                        text = stringResource(option.labelRes),
                        background = if (isSel) colors.accent else colors.accentSoft,
                        textColor = if (isSel) colors.onAccent else colors.accent,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { selected = if (isSel) null else index },
                    )
                }
            }
        }
        val sel = selected
        if (sel != null) {
            AppText(stringResource(LearnContent.tasteOptions[sel].adviceRes), style = CoffeeTheme.type.body)
        }
    }
}
