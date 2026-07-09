package co.coffeery.app.ui.screens.learn

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import co.coffeery.app.R
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.Chip
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.Route
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun LearnScreen(vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var activeChapterRes by remember { mutableStateOf(LearnContent.chapterOrder[0]) }
    var searchQuery by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()
    val completedChapters = state.completedChapters

    val cardTexts = LearnContent.cards.map { card -> card to (stringResource(card.titleRes) + " " + stringResource(card.bodyRes)) }
    val filteredCards = if (searchQuery.isBlank()) {
        LearnContent.cards
    } else {
        cardTexts.filter { (_, text) -> text.contains(searchQuery, true) }.map { it.first }
    }
    val searchActive = searchQuery.isNotBlank()

    LaunchedEffect(Unit) {
        scrollState.scrollTo(state.learnScrollOffset)
    }

    DisposableEffect(Unit) {
        onDispose { vm.setLearnScrollOffset(scrollState.value) }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.learn_title),
            subtitle = stringResource(R.string.learn_intro),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                hint = stringResource(R.string.search_hint_learn),
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

        StepMap(
            activeChapterRes = activeChapterRes,
            completedChapters = completedChapters,
            onChapterSelected = { idx ->
                val ch = LearnContent.chapterOrder[idx]
                activeChapterRes = ch
                val first = LearnContent.cards.indexOfFirst { it.chapterRes == ch }
                val offsetDp = 600.dp + 170.dp * first
                scope.launch {
                    scrollState.animateScrollTo(with(density) { offsetDp.toPx() }.toInt())
                }
            },
        )

        TroubleshootCard()

        ExtractionCalculatorCard()

        WaterMineralCard()

        if (searchActive && filteredCards.isEmpty()) {
            AppText(
                stringResource(R.string.search_no_results),
                style = CoffeeTheme.type.body,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                align = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        // Render a chapter header whenever the chapter changes, keeping the
        // card's global index stable for detail navigation.
        var lastChapter = 0
        filteredCards.forEachIndexed { index, card ->
            if (card.chapterRes != lastChapter) {
                lastChapter = card.chapterRes
                Spacer(Modifier.height(2.dp))
                AppText(
                    stringResource(card.chapterRes),
                    style = CoffeeTheme.type.label,
                    color = colors.accent,
                )
            }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(stringResource(R.string.learn_read_more), style = CoffeeTheme.type.label, color = colors.accent)
                    if (completedChapters.contains(card.chapterRes)) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepMap(
    activeChapterRes: Int,
    completedChapters: Set<Int>,
    onChapterSelected: (Int) -> Unit,
) {
    val colors = CoffeeTheme.colors
    val chapters = LearnContent.chapterOrder
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        items(chapters.size) { index ->
            val chapterRes = chapters[index]
            val isCompleted = chapterRes in completedChapters
            val isUnlocked = index == 0 || chapters[index - 1] in completedChapters
            val isLocked = !isCompleted && !isUnlocked
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) colors.accent else Color.Transparent)
                        .border(
                            2.dp,
                            when {
                                isCompleted -> colors.accent
                                isLocked -> colors.outline
                                else -> colors.accent
                            },
                            CircleShape,
                        )
                        .then(
                            if (isLocked) Modifier
                            else Modifier.clickable { onChapterSelected(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCompleted) {
                        AppText(
                            text = "\u2713",
                            style = CoffeeTheme.type.caption,
                            color = colors.onAccent,
                        )
                    } else {
                        AppText(
                            text = "${index + 1}",
                            style = CoffeeTheme.type.caption,
                            color = if (isLocked) colors.textSecondary else colors.accent,
                        )
                    }
                }
                if (index < chapters.size - 1) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .background(
                                if (isCompleted || chapters.getOrNull(index + 1) in completedChapters) colors.accent
                                else colors.outline,
                            ),
                    )
                    Spacer(Modifier.width(4.dp))
                }
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
        // Simple wrapping rows of chips (4 per row).
        LearnContent.tasteOptions.withIndex().chunked(4).forEach { rowItems ->
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
