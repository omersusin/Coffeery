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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.lerp
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
                    scrollState.scrollTo(with(density) { offsetDp.toPx() }.toInt())
                }
            },
        )

        TroubleshootCard()

        ProTipsCard()

        QuickRatioCard()

        GrindSizeCard()

        BrewTroubleshooterCard()

        FlavorWheelCard()

        ExtractionCalculatorCard()

        WaterMineralCard()

        GlossaryCard()

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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        chapters.forEachIndexed { index, chapterRes ->
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

@Composable
private fun ProTipsCard() {
    val colors = CoffeeTheme.colors
    val tips = listOf(
        R.string.pro_tip_1, R.string.pro_tip_2, R.string.pro_tip_3, R.string.pro_tip_4,
        R.string.pro_tip_5, R.string.pro_tip_6, R.string.pro_tip_7, R.string.pro_tip_8,
        R.string.pro_tip_9, R.string.pro_tip_10,
    )
    var current by remember { mutableStateOf(kotlin.random.Random.nextInt(tips.size)) }
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.pro_tips_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(8.dp))
        AppText(stringResource(tips[current]), style = CoffeeTheme.type.body, color = colors.textSecondary)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            AppText(
                stringResource(R.string.pro_tips_next),
                style = CoffeeTheme.type.label,
                color = colors.accent,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { current = (current + 1) % tips.size },
            )
        }
    }
}

@Composable
private fun QuickRatioCard() {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.ratio_ref_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(8.dp))
        AppText(stringResource(R.string.ratio_1_15), style = CoffeeTheme.type.body, color = colors.textSecondary)
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(R.string.ratio_1_16), style = CoffeeTheme.type.body, color = colors.textSecondary)
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(R.string.ratio_1_17), style = CoffeeTheme.type.body, color = colors.textSecondary)
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(R.string.ratio_1_18), style = CoffeeTheme.type.body, color = colors.textSecondary)
    }
}

@Composable
private fun GrindSizeCard() {
    val colors = CoffeeTheme.colors
    var selected by remember { mutableStateOf(-1) }
    data class GrindLevel(val nameRes: Int, val descRes: Int, val forRes: Int)
    val grinds = listOf(
        GrindLevel(R.string.grind_vis_name_extra_coarse, R.string.grind_vis_desc_extra_coarse, R.string.grind_vis_for_extra_coarse),
        GrindLevel(R.string.grind_vis_name_coarse, R.string.grind_vis_desc_coarse, R.string.grind_vis_for_coarse),
        GrindLevel(R.string.grind_vis_name_med_coarse, R.string.grind_vis_desc_med_coarse, R.string.grind_vis_for_med_coarse),
        GrindLevel(R.string.grind_vis_name_medium, R.string.grind_vis_desc_medium, R.string.grind_vis_for_medium),
        GrindLevel(R.string.grind_vis_name_med_fine, R.string.grind_vis_desc_med_fine, R.string.grind_vis_for_med_fine),
        GrindLevel(R.string.grind_vis_name_fine, R.string.grind_vis_desc_fine, R.string.grind_vis_for_fine),
        GrindLevel(R.string.grind_vis_name_extra_fine, R.string.grind_vis_desc_extra_fine, R.string.grind_vis_for_extra_fine),
    )
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.grind_vis_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(8.dp))
        grinds.forEachIndexed { index, grind ->
            val fraction = index / (grinds.size - 1).toFloat()
            val barColor = lerp(colors.cremaLight, colors.cremaDark, fraction)
            val isSel = selected == index
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { selected = if (isSel) -1 else index },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(barColor)
                            .then(
                                if (isSel) Modifier.border(2.dp, colors.accent, RoundedCornerShape(6.dp))
                                else Modifier
                            ),
                    )
                    Spacer(Modifier.width(10.dp))
                    AppText(stringResource(grind.nameRes), style = CoffeeTheme.type.label)
                }
                if (isSel) {
                    Spacer(Modifier.height(4.dp))
                    AppText(stringResource(grind.descRes), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                    AppText(stringResource(grind.forRes), style = CoffeeTheme.type.caption, color = colors.accent)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun BrewTroubleshooterCard() {
    val colors = CoffeeTheme.colors
    var selected by remember { mutableStateOf(-1) }
    val issues = listOf(
        R.string.brew_issue_sour to R.string.brew_issue_sour_advice,
        R.string.brew_issue_bitter to R.string.brew_issue_bitter_advice,
        R.string.brew_issue_weak to R.string.brew_issue_weak_advice,
        R.string.brew_issue_dry to R.string.brew_issue_dry_advice,
    )
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.brew_troubleshoot_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(4.dp))
        AppText(stringResource(R.string.brew_troubleshoot_question), style = CoffeeTheme.type.caption, color = colors.textSecondary)
        Spacer(Modifier.height(12.dp))
        issues.withIndex().chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                rowItems.forEach { (index, issue) ->
                    val isSel = selected == index
                    Chip(
                        text = stringResource(issue.first),
                        background = if (isSel) colors.accent else colors.accentSoft,
                        textColor = if (isSel) colors.onAccent else colors.accent,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { selected = if (isSel) -1 else index },
                    )
                }
            }
        }
        val sel = selected
        if (sel != null) {
            AppText(stringResource(issues[sel].second), style = CoffeeTheme.type.body)
        }
    }
}

private data class GlossaryTerm(val termRes: Int, val defRes: Int)

private val GlossaryTerms = listOf(
    GlossaryTerm(R.string.glossary_term_1, R.string.glossary_def_1),
    GlossaryTerm(R.string.glossary_term_2, R.string.glossary_def_2),
    GlossaryTerm(R.string.glossary_term_3, R.string.glossary_def_3),
    GlossaryTerm(R.string.glossary_term_4, R.string.glossary_def_4),
    GlossaryTerm(R.string.glossary_term_5, R.string.glossary_def_5),
    GlossaryTerm(R.string.glossary_term_6, R.string.glossary_def_6),
    GlossaryTerm(R.string.glossary_term_7, R.string.glossary_def_7),
    GlossaryTerm(R.string.glossary_term_8, R.string.glossary_def_8),
    GlossaryTerm(R.string.glossary_term_9, R.string.glossary_def_9),
    GlossaryTerm(R.string.glossary_term_10, R.string.glossary_def_10),
    GlossaryTerm(R.string.glossary_term_11, R.string.glossary_def_11),
    GlossaryTerm(R.string.glossary_term_12, R.string.glossary_def_12),
    GlossaryTerm(R.string.glossary_term_13, R.string.glossary_def_13),
    GlossaryTerm(R.string.glossary_term_14, R.string.glossary_def_14),
    GlossaryTerm(R.string.glossary_term_15, R.string.glossary_def_15),
    GlossaryTerm(R.string.glossary_term_16, R.string.glossary_def_16),
    GlossaryTerm(R.string.glossary_term_17, R.string.glossary_def_17),
    GlossaryTerm(R.string.glossary_term_18, R.string.glossary_def_18),
    GlossaryTerm(R.string.glossary_term_19, R.string.glossary_def_19),
    GlossaryTerm(R.string.glossary_term_20, R.string.glossary_def_20),
    GlossaryTerm(R.string.glossary_term_21, R.string.glossary_def_21),
    GlossaryTerm(R.string.glossary_term_22, R.string.glossary_def_22),
    GlossaryTerm(R.string.glossary_term_23, R.string.glossary_def_23),
    GlossaryTerm(R.string.glossary_term_24, R.string.glossary_def_24),
    GlossaryTerm(R.string.glossary_term_25, R.string.glossary_def_25),
)

private data class FlavorCategory(val labelRes: Int, val notes: List<Int>)

private val FlavorWheelData = listOf(
    FlavorCategory(R.string.flavor_fruity, listOf(R.string.flavor_berry, R.string.flavor_citrus, R.string.flavor_stone_fruit, R.string.flavor_tropical)),
    FlavorCategory(R.string.flavor_floral, listOf(R.string.flavor_jasmine, R.string.flavor_rose, R.string.flavor_chamomile, R.string.flavor_lavender)),
    FlavorCategory(R.string.flavor_sweet, listOf(R.string.flavor_chocolate, R.string.flavor_caramel, R.string.flavor_honey, R.string.flavor_brown_sugar)),
    FlavorCategory(R.string.flavor_nutty_spice, listOf(R.string.flavor_almond, R.string.flavor_cinnamon, R.string.flavor_clove, R.string.flavor_nutmeg)),
    FlavorCategory(R.string.flavor_earthy, listOf(R.string.flavor_woody, R.string.flavor_tobacco, R.string.flavor_leather, R.string.flavor_mushroom)),
)

@Composable
private fun FlavorWheelCard() {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.flavor_wheel_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(8.dp))
        FlavorWheelData.forEach { category ->
            Spacer(Modifier.height(8.dp))
            AppText(stringResource(category.labelRes), style = CoffeeTheme.type.headline, color = colors.accent)
            Spacer(Modifier.height(4.dp))
            category.notes.chunked(4).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                    rowItems.forEach { noteRes ->
                        Chip(
                            text = stringResource(noteRes),
                            background = colors.accentSoft,
                            textColor = colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlossaryCard() {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        AppText(stringResource(R.string.glossary_title), style = CoffeeTheme.type.title)
        Spacer(Modifier.height(8.dp))
        GlossaryTerms.forEach { term ->
            AppText(stringResource(term.termRes), style = CoffeeTheme.type.headline)
            Spacer(Modifier.height(2.dp))
            AppText(
                stringResource(term.defRes),
                style = CoffeeTheme.type.caption,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}
