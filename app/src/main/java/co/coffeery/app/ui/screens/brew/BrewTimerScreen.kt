package co.coffeery.app.ui.screens.brew

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.coffeery.app.R
import co.coffeery.app.data.local.BrewLogEntity
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.components.displayName
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppUiState
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.util.BrewMath
import co.coffeery.app.util.Format
import kotlinx.coroutines.delay

@Composable
fun BrewTimerScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val eq = state.selectedEquipment ?: return
    val steps = eq.steps
    if (steps.isEmpty()) return
    val result = BrewMath.compute(eq, state.strength, state.roast, state.byCups, state.cups, state.waterMl)
    val totalWater = result.waterMl
    val plannedTotal = steps.sumOf { it.durationSec }

    var stepIndex by remember { mutableIntStateOf(0) }
    var remaining by remember { mutableIntStateOf(steps[0].durationSec) }
    var running by remember { mutableStateOf(false) }
    var everStarted by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var elapsedTotal by remember { mutableIntStateOf(0) }

    // Keep the screen awake for the whole brew session.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    LaunchedEffect(running) {
        while (running && !finished) {
            delay(1000)
            elapsedTotal++
            val next = remaining - 1
            if (next <= 0) {
                if (stepIndex < steps.lastIndex) {
                    stepIndex++
                    remaining = steps[stepIndex].durationSec
                } else {
                    remaining = 0
                    running = false
                    finished = true
                }
            } else {
                remaining = next
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.brew_title, eq.displayName()),
            onBack = { vm.back() },
        )
        Spacer(Modifier.height(24.dp))

        if (finished) {
            BrewComplete(eq, state, result, elapsedTotal, vm)
            return@Column
        }

        val step = steps[stepIndex]
        val dur = step.durationSec.coerceAtLeast(1)
        val progress by animateFloatAsState(
            targetValue = ((dur - remaining).toFloat() / dur).coerceIn(0f, 1f),
            label = "progress",
        )

        AppText(
            stringResource(R.string.brew_step_counter, stepIndex + 1, steps.size),
            style = CoffeeTheme.type.label,
            color = colors.textSecondary,
            modifier = Modifier.fillMaxWidth(),
            align = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            ProgressRing(progress = progress, color = colors.coffeeFor(progress))
            AppText(
                Format.clock(remaining),
                style = CoffeeTheme.type.display.copy(
                    fontSize = 72.sp,
                    lineHeight = 76.sp,
                ),
                color = colors.textPrimary,
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LineIcon(step.kind.glyph(), colors.accent, Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            AppText(
                stringResource(step.titleRes),
                style = CoffeeTheme.type.title,
                color = colors.textPrimary,
            )
        }
        if (step.waterTargetPct >= 0f) {
            Spacer(Modifier.height(6.dp))
            AppText(
                stringResource(R.string.brew_pour_to, BrewMath.stepWaterGrams(step.waterTargetPct, totalWater)),
                style = CoffeeTheme.type.body,
                color = colors.accent,
                modifier = Modifier.fillMaxWidth(),
                align = TextAlign.Center,
            )
        }

        if (stepIndex < steps.lastIndex) {
            val nextStep = steps[stepIndex + 1]
            Spacer(Modifier.height(12.dp))
            AppText(
                stringResource(R.string.brew_next_up, stringResource(nextStep.titleRes) + " · " + Format.clock(nextStep.durationSec)),
                style = CoffeeTheme.type.body,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                align = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (everStarted) {
                SecondaryButton(stringResource(R.string.brew_finish), Modifier.weight(1f)) {
                    running = false; finished = true
                }
            }
            PrimaryButton(
                text = when {
                    running -> stringResource(R.string.brew_pause)
                    everStarted -> stringResource(R.string.brew_resume)
                    else -> stringResource(R.string.action_start)
                },
                modifier = Modifier.weight(1f),
            ) {
                everStarted = true
                running = !running
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, color: Color) {
    val colors = CoffeeTheme.colors
    Canvas(modifier = Modifier.size(260.dp)) {
        val sw = 12.dp.toPx()
        val inset = sw / 2f
        val arcSize = Size(size.width - sw, size.height - sw)
        drawArc(
            color = colors.outline,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = sw),
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = sw, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun BrewComplete(
    eq: co.coffeery.app.data.model.Equipment,
    state: AppUiState,
    result: co.coffeery.app.util.BrewResult,
    elapsedTotal: Int,
    vm: AppViewModel,
) {
    val colors = CoffeeTheme.colors
    var showSave by remember { mutableStateOf(false) }
    val equipmentName = eq.displayName()

    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.6f) + fadeIn(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LineIcon(
                    co.coffeery.app.ui.components.Glyph.CUP,
                    colors.accent,
                    Modifier.scale(pulse).size(64.dp),
                )
                Spacer(Modifier.height(20.dp))
                AppText(
                    stringResource(R.string.brew_complete),
                    style = CoffeeTheme.type.display,
                    align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        AppText(
            stringResource(R.string.brew_complete_sub, equipmentName),
            style = CoffeeTheme.type.body,
            color = colors.textSecondary,
            align = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Row {
            AppText(stringResource(R.string.brew_elapsed) + ": ", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            AppText(Format.clock(elapsedTotal), style = CoffeeTheme.type.caption, color = colors.textPrimary)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(stringResource(R.string.save_brew_log), Modifier.fillMaxWidth()) { showSave = true }
        Spacer(Modifier.height(10.dp))
        SecondaryButton(stringResource(R.string.action_done), Modifier.fillMaxWidth()) { vm.back() }
    }

    if (showSave) {
        SaveBrewDialog(
            eq = eq,
            state = state,
            result = result,
            elapsedTotal = elapsedTotal,
            onDismiss = { showSave = false },
            onSave = { log ->
                vm.saveBrewLog(log)
                showSave = false
                vm.back()
            },
        )
    }
}

@Composable
private fun SaveBrewDialog(
    eq: co.coffeery.app.data.model.Equipment,
    state: AppUiState,
    result: co.coffeery.app.util.BrewResult,
    elapsedTotal: Int,
    onDismiss: () -> Unit,
    onSave: (BrewLogEntity) -> Unit,
) {
    val colors = CoffeeTheme.colors
    var rating by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var grindSize by remember { mutableStateOf("") }
    val name = eq.displayName()

    CoffeeDialog(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AppText(stringResource(R.string.save_brew_log), style = CoffeeTheme.type.title)
            Spacer(Modifier.height(8.dp))
            AppText(name + " · " + stringResource(R.string.calc_grams, Format.grams(result.coffeeGrams)) + " : " + result.waterMl + " ml", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(14.dp))

            AppText(stringResource(R.string.log_rating), style = CoffeeTheme.type.label, color = colors.textSecondary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..5) {
                    AppText(
                        if (i <= rating) "★" else "☆",
                        style = CoffeeTheme.type.title,
                        color = if (i <= rating) colors.accent else colors.outline,
                        modifier = Modifier.clickable { rating = i },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            AppText(stringResource(R.string.log_grind_size), style = CoffeeTheme.type.label, color = colors.textSecondary)
            Spacer(Modifier.height(4.dp))
            AppTextField(
                value = grindSize,
                onValueChange = { grindSize = it },
                hint = stringResource(R.string.log_grind_hint),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            AppText(stringResource(R.string.log_notes), style = CoffeeTheme.type.label, color = colors.textSecondary)
            Spacer(Modifier.height(4.dp))
            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                hint = stringResource(R.string.log_notes_hint),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { onDismiss() }
                PrimaryButton(stringResource(R.string.action_save), Modifier.weight(1f)) {
                    onSave(
                        BrewLogEntity(
                            equipmentId = eq.id,
                            equipmentName = name,
                            strength = state.strength,
                            roast = state.roast.name,
                            ratioDenominator = result.ratioDenominator,
                            coffeeGrams = result.coffeeGrams,
                            waterMl = result.waterMl,
                            grind = result.grind.name,
                            customGrindSize = grindSize.trim(),
                            tempCelsius = result.tempCelsius,
                            totalDurationSec = elapsedTotal,
                            rating = rating,
                            tastingNotes = notes.trim(),
                        )
                    )
                }
            }
        }
    }
}
