package co.coffeery.app.ui.screens.brew

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.ui.components.AppText
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
            BrewComplete(eq.displayName(), plannedTotal) { vm.back() }
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
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            ProgressRing(progress = progress)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LineIcon(step.kind.glyph(), colors.accent, Modifier.size(40.dp))
                Spacer(Modifier.height(10.dp))
                AppText(Format.clock(remaining), style = CoffeeTheme.type.display, color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                AppText(stringResource(step.titleRes), style = CoffeeTheme.type.title, color = colors.textPrimary)
                if (step.waterTargetPct >= 0f) {
                    Spacer(Modifier.height(4.dp))
                    AppText(
                        stringResource(R.string.brew_pour_to, BrewMath.stepWaterGrams(step.waterTargetPct, totalWater)),
                        style = CoffeeTheme.type.body,
                        color = colors.accent,
                    )
                }
            }
        }

        if (stepIndex < steps.lastIndex) {
            AppText(
                stringResource(R.string.brew_next_up, stringResource(steps[stepIndex + 1].titleRes)),
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
private fun ProgressRing(progress: Float) {
    val colors = CoffeeTheme.colors
    Canvas(modifier = Modifier.size(240.dp)) {
        val sw = 16.dp.toPx()
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
            color = colors.accent,
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
private fun BrewComplete(equipmentName: String, plannedTotal: Int, onDone: () -> Unit) {
    val colors = CoffeeTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        LineIcon(co.coffeery.app.ui.components.Glyph.CUP, colors.accent, Modifier.size(64.dp))
        Spacer(Modifier.height(20.dp))
        AppText(stringResource(R.string.brew_complete), style = CoffeeTheme.type.display, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
            AppText(Format.clock(plannedTotal), style = CoffeeTheme.type.caption, color = colors.textPrimary)
        }
        Spacer(Modifier.height(32.dp))
        PrimaryButton(stringResource(R.string.action_done), Modifier.fillMaxWidth()) { onDone() }
    }
}
