package co.coffeery.app.ui.screens.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.Glyph
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
            .padding(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.learn_title),
            subtitle = stringResource(R.string.learn_intro),
        )

        TodaysLessonCard(vm)

        QuickQuizCard()

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

        FoodPairingCard()

        CultureFactsCard()

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
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val checkPath = Path().apply {
                                moveTo(size.width * 0.22f, size.height * 0.52f)
                                lineTo(size.width * 0.4f, size.height * 0.72f)
                                lineTo(size.width * 0.78f, size.height * 0.28f)
                            }
                            drawPath(
                                checkPath,
                                colors.onAccent,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = size.minDimension * 0.13f,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                ),
                            )
                        }
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
        R.string.pro_tip_9, R.string.pro_tip_10, R.string.pro_tip_11, R.string.pro_tip_12,
        R.string.pro_tip_13, R.string.pro_tip_14, R.string.pro_tip_15, R.string.pro_tip_16,
        R.string.pro_tip_17, R.string.pro_tip_18, R.string.pro_tip_19, R.string.pro_tip_20,
        R.string.pro_tip_21, R.string.pro_tip_22, R.string.pro_tip_23, R.string.pro_tip_24,
        R.string.pro_tip_25, R.string.pro_tip_26, R.string.pro_tip_27, R.string.pro_tip_28,
        R.string.pro_tip_29, R.string.pro_tip_30, R.string.pro_tip_31, R.string.pro_tip_32,
        R.string.pro_tip_33, R.string.pro_tip_34, R.string.pro_tip_35, R.string.pro_tip_36,
        R.string.pro_tip_37, R.string.pro_tip_38, R.string.pro_tip_39, R.string.pro_tip_40,
        R.string.pro_tip_41, R.string.pro_tip_42, R.string.pro_tip_43, R.string.pro_tip_44,
        R.string.pro_tip_45, R.string.pro_tip_46, R.string.pro_tip_47, R.string.pro_tip_48,
        R.string.pro_tip_49, R.string.pro_tip_50,
    )
    var current by remember { mutableStateOf(kotlin.random.Random.nextInt(tips.size)) }
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.CUP, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.pro_tips_title), style = CoffeeTheme.type.title)
        }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.CUP, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.ratio_ref_title), style = CoffeeTheme.type.title)
        }
        Spacer(Modifier.height(8.dp))
        RatioRow(R.string.ratio_1_15)
        Spacer(Modifier.height(4.dp))
        RatioRow(R.string.ratio_1_16)
        Spacer(Modifier.height(4.dp))
        RatioRow(R.string.ratio_1_17)
        Spacer(Modifier.height(4.dp))
        RatioRow(R.string.ratio_1_18)
    }
}

@Composable
private fun RatioRow(textRes: Int) {
    val colors = CoffeeTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        LineIcon(Glyph.CUP, colors.accent, Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        AppText(stringResource(textRes), style = CoffeeTheme.type.body, color = colors.textSecondary)
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
    var selected by remember { mutableStateOf<Int?>(null) }
    val issues = listOf(
        R.string.brew_issue_sour to R.string.brew_issue_sour_advice,
        R.string.brew_issue_bitter to R.string.brew_issue_bitter_advice,
        R.string.brew_issue_weak to R.string.brew_issue_weak_advice,
        R.string.brew_issue_dry to R.string.brew_issue_dry_advice,
        R.string.brew_issue_stalling to R.string.brew_issue_stalling_advice,
        R.string.brew_issue_channeling to R.string.brew_issue_channeling_advice,
        R.string.brew_issue_muddy to R.string.brew_issue_muddy_advice,
        R.string.brew_issue_flat to R.string.brew_issue_flat_advice,
        R.string.brew_issue_burnt to R.string.brew_issue_burnt_advice,
        R.string.brew_issue_metallic to R.string.brew_issue_metallic_advice,
        R.string.brew_issue_grassy to R.string.brew_issue_grassy_advice,
        R.string.brew_issue_salty to R.string.brew_issue_salty_advice,
        R.string.brew_issue_no_crema to R.string.brew_issue_no_crema_advice,
        R.string.brew_issue_gusher to R.string.brew_issue_gusher_advice,
        R.string.brew_issue_fines_mud to R.string.brew_issue_fines_mud_advice,
        R.string.brew_issue_clogged_filter to R.string.brew_issue_clogged_filter_advice,
        R.string.brew_issue_temp_loss to R.string.brew_issue_temp_loss_advice,
        R.string.brew_issue_grind_inconsistent to R.string.brew_issue_grind_inconsistent_advice,
    )
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.GEAR, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.brew_troubleshoot_title), style = CoffeeTheme.type.title)
        }
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
                        ) { selected = if (isSel) null else index },
                    )
                }
            }
        }
        selected?.let { sel ->
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
    GlossaryTerm(R.string.glossary_term_26, R.string.glossary_def_26),
    GlossaryTerm(R.string.glossary_term_27, R.string.glossary_def_27),
    GlossaryTerm(R.string.glossary_term_28, R.string.glossary_def_28),
    GlossaryTerm(R.string.glossary_term_29, R.string.glossary_def_29),
    GlossaryTerm(R.string.glossary_term_30, R.string.glossary_def_30),
    GlossaryTerm(R.string.glossary_term_31, R.string.glossary_def_31),
    GlossaryTerm(R.string.glossary_term_32, R.string.glossary_def_32),
    GlossaryTerm(R.string.glossary_term_33, R.string.glossary_def_33),
    GlossaryTerm(R.string.glossary_term_34, R.string.glossary_def_34),
    GlossaryTerm(R.string.glossary_term_35, R.string.glossary_def_35),
    GlossaryTerm(R.string.glossary_term_36, R.string.glossary_def_36),
    GlossaryTerm(R.string.glossary_term_37, R.string.glossary_def_37),
    GlossaryTerm(R.string.glossary_term_38, R.string.glossary_def_38),
    GlossaryTerm(R.string.glossary_term_39, R.string.glossary_def_39),
    GlossaryTerm(R.string.glossary_term_40, R.string.glossary_def_40),
    GlossaryTerm(R.string.glossary_term_41, R.string.glossary_def_41),
    GlossaryTerm(R.string.glossary_term_42, R.string.glossary_def_42),
    GlossaryTerm(R.string.glossary_term_43, R.string.glossary_def_43),
    GlossaryTerm(R.string.glossary_term_44, R.string.glossary_def_44),
    GlossaryTerm(R.string.glossary_term_45, R.string.glossary_def_45),
    GlossaryTerm(R.string.glossary_term_46, R.string.glossary_def_46),
    GlossaryTerm(R.string.glossary_term_47, R.string.glossary_def_47),
    GlossaryTerm(R.string.glossary_term_48, R.string.glossary_def_48),
    GlossaryTerm(R.string.glossary_term_49, R.string.glossary_def_49),
    GlossaryTerm(R.string.glossary_term_50, R.string.glossary_def_50),
    GlossaryTerm(R.string.glossary_term_51, R.string.glossary_def_51),
    GlossaryTerm(R.string.glossary_term_52, R.string.glossary_def_52),
    GlossaryTerm(R.string.glossary_term_53, R.string.glossary_def_53),
    GlossaryTerm(R.string.glossary_term_54, R.string.glossary_def_54),
    GlossaryTerm(R.string.glossary_term_55, R.string.glossary_def_55),
    GlossaryTerm(R.string.glossary_term_56, R.string.glossary_def_56),
    GlossaryTerm(R.string.glossary_term_57, R.string.glossary_def_57),
    GlossaryTerm(R.string.glossary_term_58, R.string.glossary_def_58),
    GlossaryTerm(R.string.glossary_term_59, R.string.glossary_def_59),
    GlossaryTerm(R.string.glossary_term_60, R.string.glossary_def_60),
    GlossaryTerm(R.string.glossary_term_61, R.string.glossary_def_61),
    GlossaryTerm(R.string.glossary_term_62, R.string.glossary_def_62),
    GlossaryTerm(R.string.glossary_term_63, R.string.glossary_def_63),
    GlossaryTerm(R.string.glossary_term_64, R.string.glossary_def_64),
    GlossaryTerm(R.string.glossary_term_65, R.string.glossary_def_65),
)

private data class FlavorCategory(val labelRes: Int, val notes: List<Int>)

private val FlavorWheelData = listOf(
    FlavorCategory(R.string.flavor_fruity, listOf(R.string.flavor_berry, R.string.flavor_citrus, R.string.flavor_stone_fruit, R.string.flavor_tropical, R.string.flavor_tropical_fruit, R.string.flavor_red_berry)),
    FlavorCategory(R.string.flavor_floral, listOf(R.string.flavor_jasmine, R.string.flavor_rose, R.string.flavor_chamomile, R.string.flavor_lavender)),
    FlavorCategory(R.string.flavor_sweet, listOf(R.string.flavor_chocolate, R.string.flavor_caramel, R.string.flavor_honey, R.string.flavor_brown_sugar, R.string.flavor_caramelized, R.string.flavor_maple)),
    FlavorCategory(R.string.flavor_nutty_spice, listOf(R.string.flavor_almond, R.string.flavor_cinnamon, R.string.flavor_clove, R.string.flavor_nutmeg, R.string.flavor_hazelnut, R.string.flavor_milk_chocolate, R.string.flavor_dark_cocoa)),
    FlavorCategory(R.string.flavor_earthy, listOf(R.string.flavor_woody, R.string.flavor_tobacco, R.string.flavor_leather, R.string.flavor_mushroom)),
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlavorWheelCard() {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.PALETTE, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.flavor_wheel_title), style = CoffeeTheme.type.title)
        }
        Spacer(Modifier.height(8.dp))
        FlavorWheelData.forEach { category ->
            Spacer(Modifier.height(8.dp))
            AppText(stringResource(category.labelRes), style = CoffeeTheme.type.headline, color = colors.accent)
            Spacer(Modifier.height(4.dp))
            category.notes.chunked(4).forEach { rowItems ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.BOOK, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.glossary_title), style = CoffeeTheme.type.title)
        }
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

@Composable
private fun FoodPairingCard() {
    val colors = CoffeeTheme.colors
    val pairings = listOf(
        Triple(R.string.pairing_1_coffee, R.string.pairing_1_food, R.string.pairing_1_why),
        Triple(R.string.pairing_2_coffee, R.string.pairing_2_food, R.string.pairing_2_why),
        Triple(R.string.pairing_3_coffee, R.string.pairing_3_food, R.string.pairing_3_why),
        Triple(R.string.pairing_4_coffee, R.string.pairing_4_food, R.string.pairing_4_why),
        Triple(R.string.pairing_5_coffee, R.string.pairing_5_food, R.string.pairing_5_why),
    )
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.BEAN, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.pairing_title), style = CoffeeTheme.type.title)
        }
        Spacer(Modifier.height(8.dp))
        pairings.forEach { (coffee, food, why) ->
            AppText(stringResource(coffee), style = CoffeeTheme.type.headline)
            AppText("+ ${stringResource(food)}", style = CoffeeTheme.type.body, color = colors.accent)
            AppText(stringResource(why), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CultureFactsCard() {
    val colors = CoffeeTheme.colors
    val facts = listOf(
        R.string.culture_fact_1,
        R.string.culture_fact_2,
        R.string.culture_fact_3,
        R.string.culture_fact_4,
    )
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.BOOK, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.culture_title), style = CoffeeTheme.type.title)
        }
        Spacer(Modifier.height(8.dp))
        facts.forEach { fact ->
            AppText(stringResource(fact), style = CoffeeTheme.type.body, color = colors.textSecondary)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun TodaysLessonCard(vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val daySeed = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    val card = LearnContent.cards[daySeed % LearnContent.cards.size]
    CoffeeCard(modifier = Modifier.fillMaxWidth().clickable { vm.openRoute(Route.LearnDetail(LearnContent.cards.indexOf(card))) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.BOOK, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.learn_today), style = CoffeeTheme.type.label, color = colors.accent)
        }
        Spacer(Modifier.height(6.dp))
        AppText(stringResource(card.titleRes), style = CoffeeTheme.type.headline)
        Spacer(Modifier.height(2.dp))
        AppText(stringResource(card.bodyRes), style = CoffeeTheme.type.caption, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private data class QuizQuestion(
    val question: String,
    val answers: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

private val QuizQuestions = listOf(
    QuizQuestion(
        "What grind size for French Press?",
        listOf("Fine", "Medium", "Coarse", "Extra Fine"),
        2,
        "French press uses a coarse grind to prevent sludge and bitterness.",
    ),
    QuizQuestion(
        "What water temp for pour-over (SCA standard)?",
        listOf("80-85°C", "90-96°C", "70-75°C", "100°C"),
        1,
        "SCA recommends 90-96°C for optimal pour-over extraction.",
    ),
    QuizQuestion(
        "What causes sour coffee?",
        listOf("Over-extraction", "Under-extraction", "Old beans", "Hard water"),
        1,
        "Sour coffee is a classic sign of under-extraction.",
    ),
    QuizQuestion(
        "Bloom time for fresh coffee?",
        listOf("5-10 sec", "30-45 sec", "60-90 sec", "No bloom needed"),
        1,
        "Bloom for 30-45 seconds to allow CO₂ to escape for even extraction.",
    ),
    QuizQuestion(
        "Which has more caffeine?",
        listOf("Arabica", "Robusta", "Both equal", "Decaf"),
        1,
        "Robusta beans contain roughly twice the caffeine of Arabica.",
    ),
    QuizQuestion(
        "What is the ideal water temperature for pour-over (SCA standard)?",
        listOf("80-85°C", "90-96°C", "70-75°C", "100°C"),
        1,
        "SCA specifies 195-205°F (90-96°C). Below 90°C under-extracts; above 96°C risks bitterness.",
    ),
    QuizQuestion(
        "Which coffee species has more caffeine?",
        listOf("Arabica (~1.2%)", "Robusta (~2.2%)", "Liberica", "All equal"),
        1,
        "Robusta has nearly double the caffeine of Arabica, giving it a more bitter taste and stronger pest resistance.",
    ),
    QuizQuestion(
        "What is 'crema'?",
        listOf("Coffee foam from steamed milk", "The tan foam on top of espresso", "A type of bean", "A roasting level"),
        1,
        "Crema forms when CO2 and coffee oils emulsify under 9 bars of pressure. Fresh beans produce more crema.",
    ),
    QuizQuestion(
        "Which grind for French Press?",
        listOf("Extra fine", "Fine", "Coarse", "Medium"),
        2,
        "Coarse grind prevents sludge and over-extraction during the 4-minute immersion. Think breadcrumbs, not powder.",
    ),
    QuizQuestion(
        "What does 'blooming' mean?",
        listOf("Adding milk", "Pre-wetting grounds to release CO2", "Roasting beans", "Grinding coffee"),
        1,
        "Fresh coffee releases CO2 when hot water first hits it. Blooming for 30-45 seconds ensures even extraction.",
    ),
    QuizQuestion(
        "What is a 'ristretto'?",
        listOf("Espresso with more water", "Espresso with less water", "A type of bean", "A brewing device"),
        1,
        "Ristretto uses the same dose but half the water — more concentrated, sweeter, less bitter than regular espresso.",
    ),
    QuizQuestion(
        "Which country is the birthplace of coffee?",
        listOf("Brazil", "Colombia", "Ethiopia", "Vietnam"),
        2,
        "Coffee originated in Ethiopia's Kaffa region. Legend says a goatherd named Kaldi discovered it when his goats ate red berries and became energetic.",
    ),
)

@Composable
private fun QuickQuizCard() {
    val colors = CoffeeTheme.colors
    var questionIndex by remember { mutableStateOf(kotlin.random.Random.nextInt(QuizQuestions.size)) }
    var selectedAnswer by remember { mutableStateOf(-1) }
    var correctCount by remember { mutableIntStateOf(0) }
    var counted by remember { mutableStateOf(false) }
    val q = QuizQuestions[questionIndex]
    val wrongColor = Color(0xFFE53935)
    CoffeeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineIcon(Glyph.CHECK, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            AppText(stringResource(R.string.learn_quiz_title), style = CoffeeTheme.type.title)
        }
        Spacer(Modifier.height(8.dp))
        AppText(q.question, style = CoffeeTheme.type.headline)
        Spacer(Modifier.height(8.dp))
        q.answers.forEachIndexed { index, answer ->
            val isSelected = selectedAnswer == index
            val isCorrect = index == q.correctIndex
            val background = when {
                selectedAnswer != -1 && isCorrect -> colors.accent
                selectedAnswer != -1 && isSelected && !isCorrect -> wrongColor
                else -> colors.accentSoft
            }
            val textColor = when {
                selectedAnswer != -1 && (isCorrect || (isSelected && !isCorrect)) -> colors.onAccent
                else -> colors.textPrimary
            }
            val prefix = when {
                selectedAnswer != -1 && isCorrect -> "✓  "
                selectedAnswer != -1 && isSelected && !isCorrect -> "✕  "
                else -> ""
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(background)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { if (selectedAnswer == -1) selectedAnswer = index }
                    .padding(10.dp),
            ) {
                AppText("$prefix$answer", style = CoffeeTheme.type.body, color = textColor)
            }
            Spacer(Modifier.height(6.dp))
        }
        if (selectedAnswer != -1) {
            Spacer(Modifier.height(4.dp))
            val isCorrect = selectedAnswer == q.correctIndex
            if (isCorrect && !counted) {
                correctCount++
                counted = true
            }
            AppText(
                if (isCorrect) stringResource(R.string.learn_quiz_correct) else stringResource(R.string.learn_quiz_wrong),
                style = CoffeeTheme.type.label,
                color = if (isCorrect) colors.accent else wrongColor,
            )
            AppText(q.explanation, style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(4.dp))
            AppText(
                stringResource(R.string.learn_quiz_score, correctCount),
                style = CoffeeTheme.type.label,
                color = colors.accent,
            )
            Spacer(Modifier.height(4.dp))
            AppText(
                "Next question →",
                style = CoffeeTheme.type.label,
                color = colors.accent,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    questionIndex = kotlin.random.Random.nextInt(QuizQuestions.size)
                    selectedAnswer = -1
                    counted = false
                },
            )
        }
    }
}
