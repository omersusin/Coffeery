package co.coffeery.app.ui.screens.brew

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.FileProvider
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.coffeery.app.R
import co.coffeery.app.MainActivity
import co.coffeery.app.data.local.BeanEntity
import co.coffeery.app.data.local.BrewLogEntity
import co.coffeery.app.service.TimerService
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.components.displayName
import co.coffeery.app.ui.components.glyph
import co.coffeery.app.ui.screens.root.AppUiState
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.data.model.StepKind
import co.coffeery.app.util.BrewMath
import co.coffeery.app.util.Format
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import kotlinx.coroutines.delay

@Composable
fun BrewTimerScreen(state: AppUiState, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    val eq = state.selectedEquipment ?: return
    val rawSteps = eq.steps
    if (rawSteps.isEmpty()) return
    val steps = (if (state.settings.timerMergeWeight) mergePours(rawSteps) else rawSteps).map { step ->
        val override = when (step.kind) {
            StepKind.BLOOM -> state.settings.bloomDurationSec.takeIf { it > 0 }
            StepKind.POUR -> state.settings.pourDurationSec.takeIf { it > 0 }
            StepKind.STEEP -> state.settings.steepDurationSec.takeIf { it > 0 }
            StepKind.DRAWDOWN -> state.settings.drawdownDurationSec.takeIf { it > 0 }
            else -> null
        }
        if (override != null) step.copy(durationSec = override) else step
    }
    val result = BrewMath.compute(eq, state.strength, state.roast, state.byCups, state.cups, state.waterMl)
    val totalWater = result.waterMl
    val plannedTotal = steps.sumOf { it.durationSec }

    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var remaining by rememberSaveable { mutableIntStateOf(steps[0].durationSec) }
    var running by rememberSaveable { mutableStateOf(false) }
    var everStarted by rememberSaveable { mutableStateOf(false) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var handsFree by remember { mutableStateOf(false) }
    var elapsedTotal by rememberSaveable { mutableIntStateOf(0) }
    var stepEndTime by rememberSaveable { mutableLongStateOf(0L) }

    val equipmentName = eq.displayName()
    val stepTitles = steps.map { stringResource(it.titleRes) }

    // Keep the screen awake for the whole brew session.
    val view = LocalView.current
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    DisposableEffect(handsFree) {
        if (!handsFree) return@DisposableEffect onDispose { }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (proximitySensor == null) return@DisposableEffect onDispose { }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val win = (context as? Activity)?.window ?: return
                val attrs = win.attributes
                if (event.values[0] < proximitySensor.maximumRange) {
                    attrs.screenBrightness = 0.01f
                } else {
                    attrs.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
                win.attributes = attrs
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose {
            sensorManager.unregisterListener(listener)
            (context as? Activity)?.window?.attributes?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    LaunchedEffect(running, finished) {
        if (running && state.settings.timerBackground && !finished) {
            val intent = Intent(context, TimerService::class.java)
            intent.putExtra("equipment", equipmentName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }

    LaunchedEffect(stepIndex, running) {
        if (running && stepEndTime == 0L) {
            stepEndTime = System.currentTimeMillis() + remaining * 1000L
        }
    }

    LaunchedEffect(running, stepIndex) {
        if (running) {
            stepEndTime = System.currentTimeMillis() + remaining * 1000L
        }
        while (running && !finished) {
            val now = System.currentTimeMillis()
            val newRemaining = ((stepEndTime - now + 999) / 1000).toInt().coerceAtLeast(0)
            if (newRemaining != remaining) {
                if (newRemaining < remaining) {
                    elapsedTotal += remaining - newRemaining
                }
                remaining = newRemaining
            }
            if (remaining <= 0 && stepIndex < steps.lastIndex) {
                stepIndex++
                remaining = steps[stepIndex].durationSec
                stepEndTime = System.currentTimeMillis() + remaining * 1000L
                if (!state.settings.timerAutoAdvance) {
                    running = false
                }
                if (state.settings.timerVibrate) {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(longArrayOf(0, 30, 50, 30), -1)
                    }
                }
                if (state.settings.timerSound) {
                    playChime(0)
                }
                if (state.settings.notificationsStepChange) {
                    val currentStep = steps[stepIndex]
                    val stepTitle = context.getString(currentStep.titleRes)
                    sendNotification(context, "Next step", "Step ${stepIndex+1}/${steps.size}: $stepTitle")
                }
            } else if (remaining <= 0) {
                remaining = 0
                running = false
                finished = true
            }
            if (state.settings.timerBackground) {
                if (!TimerService.isRunning) {
                    val intent = Intent(context, TimerService::class.java)
                    intent.putExtra("equipment", equipmentName)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } else {
                    val title = stepTitles.getOrElse(stepIndex) { "" }
                    TimerService.update(context, equipmentName, title, Format.clock(remaining), stepIndex, steps.size)
                }
            }
            delay(((stepEndTime - now) % 1000L).coerceAtLeast(16L))
        }
        if (finished) {
            if (state.settings.timerVibrate) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50, 200, 100), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 50, 100, 50, 200, 100), -1)
                }
            }
            if (state.settings.timerSound) {
                playChime(2)
            }
            if (state.settings.notificationsBrewComplete) {
                sendNotification(context, "Brew complete!", "Your $equipmentName is ready. Total time: ${Format.clock(elapsedTotal)}")
            }
        }
    }

    LaunchedEffect(stepIndex, everStarted) {
        if (stepIndex == 0 && !everStarted) {
            vm.clearStepWaterOverrides()
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
        val stepDur = step.durationSec.coerceAtLeast(1)
        val targetProgress = ((stepDur - remaining).toFloat() / stepDur).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f,
            ),
            label = "ringProgress",
        )

        AppText(
            stringResource(R.string.brew_step_counter, stepIndex + 1, steps.size),
            style = CoffeeTheme.type.label,
            color = colors.textSecondary,
            modifier = Modifier.fillMaxWidth(),
            align = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            steps.forEachIndexed { index, _ ->
                val isCurrent = index == stepIndex
                val isDone = index < stepIndex
                val color = when {
                    isDone -> CoffeeTheme.colors.accent
                    isCurrent -> CoffeeTheme.colors.accent
                    else -> CoffeeTheme.colors.outline
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (isCurrent) 32.dp else 8.dp)
                        .height(4.dp)
                        .clip(CoffeeShapes.pill)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        val pulseScale by rememberInfiniteTransition(label = "timerPulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(250),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseScale",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            ProgressRing(progress = animatedProgress, color = colors.coffeeFor(animatedProgress))
            AppText(
                Format.clock(remaining),
                style = CoffeeTheme.type.display.copy(
                    fontSize = 72.sp,
                    lineHeight = 76.sp,
                ),
                color = colors.textPrimary,
                modifier = Modifier.scale(if (running && remaining in 1..10) pulseScale else 1f),
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
        val effectivePct = state.stepWaterOverrides[stepIndex] ?: step.waterTargetPct
        val pourGrams = BrewMath.stepWaterGrams(effectivePct, totalWater)

        if (step.waterTargetPct >= 0f) {
            Spacer(Modifier.height(6.dp))
            AppText(
                stringResource(R.string.brew_pour_to, pourGrams),
                style = CoffeeTheme.type.body,
                color = colors.accent,
                modifier = Modifier.fillMaxWidth(),
                align = TextAlign.Center,
            )
        }

        if (step.waterTargetPct >= 0f && (running || everStarted)) {
            Spacer(Modifier.height(8.dp))
            AppText(
                stringResource(R.string.brew_adjust_pour),
                style = CoffeeTheme.type.caption,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                align = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SecondaryButton(stringResource(R.string.brew_adjust_less), Modifier) {
                    val newPct = (effectivePct - 0.05f).coerceAtLeast(0f)
                    vm.setStepWaterOverride(stepIndex, newPct)
                }
                Spacer(Modifier.width(16.dp))
                AppText(
                    "${pourGrams}g",
                    style = CoffeeTheme.type.body,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.width(16.dp))
                SecondaryButton(stringResource(R.string.brew_adjust_more), Modifier) {
                    val newPct = (effectivePct + 0.05f).coerceAtMost(1f)
                    vm.setStepWaterOverride(stepIndex, newPct)
                }
            }
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                stringResource(R.string.timer_hands_free),
                style = CoffeeTheme.type.caption,
                color = if (handsFree) colors.accent else colors.textSecondary,
                modifier = Modifier.clickable { handsFree = !handsFree },
            )
            val boxColor = if (handsFree) colors.accent else colors.outline
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp)
                    .clip(CoffeeShapes.small)
                    .background(boxColor)
                    .clickable { handsFree = !handsFree },
                contentAlignment = Alignment.Center,
            ) {
                if (handsFree) {
                    AppText("\u2713", style = CoffeeTheme.type.caption.copy(fontSize = 10.sp), color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

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
    val pulse = 1f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LineIcon(
                co.coffeery.app.ui.components.Glyph.CUP,
                colors.accent,
                Modifier.size(64.dp),
            )
            Spacer(Modifier.height(20.dp))
            AppText(
                stringResource(R.string.brew_complete),
                style = CoffeeTheme.type.display,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                )
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
@OptIn(ExperimentalLayoutApi::class)
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
    var selectedBean by remember { mutableStateOf<BeanEntity?>(null) }
    var flavorTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) photoUri = null
    }
    val name = eq.displayName()
    val activeBeans = state.beans.filter { !it.isArchived }

    CoffeeDialog(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AppText(stringResource(R.string.save_brew_log), style = CoffeeTheme.type.title)
            Spacer(Modifier.height(8.dp))
            AppText(name + " · " + stringResource(R.string.calc_grams, Format.grams(result.coffeeGrams)) + " : " + result.waterMl + " ml", style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(14.dp))

            AppText(stringResource(R.string.log_bean_label), style = CoffeeTheme.type.label, color = colors.textSecondary)
            Spacer(Modifier.height(6.dp))
            if (activeBeans.isNotEmpty()) {
                val rows = activeBeans.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedBean == null) colors.accent else colors.accentSoft)
                                .clickable { selectedBean = null }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            AppText(stringResource(R.string.log_bean_none), style = CoffeeTheme.type.caption, color = if (selectedBean == null) Color.White else colors.textPrimary)
                        }
                    }
                    for (row in rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (bean in row) {
                                val isSelected = selectedBean?.id == bean.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.accent else colors.accentSoft)
                                        .clickable { selectedBean = bean }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    AppText(
                                        if (bean.name.length > 8) bean.name.take(8) + "…" else bean.name,
                                        style = CoffeeTheme.type.caption,
                                        color = if (isSelected) Color.White else colors.textPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                AppText(stringResource(R.string.log_bean_empty), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            }
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
            Spacer(Modifier.height(12.dp))

            AppText(stringResource(R.string.brew_flavor_tags), style = CoffeeTheme.type.label, color = colors.textSecondary)
            Spacer(Modifier.height(6.dp))
            val flavorKeys = listOf(
                "fruity" to R.string.flavor_fruity,
                "berry" to R.string.flavor_berry,
                "citrus" to R.string.flavor_citrus,
                "stone_fruit" to R.string.flavor_stone_fruit,
                "tropical" to R.string.flavor_tropical,
                "floral" to R.string.flavor_floral,
                "chocolate" to R.string.flavor_chocolate,
                "caramel" to R.string.flavor_caramel,
                "nutty" to R.string.flavor_nutty,
                "almond" to R.string.flavor_almond,
                "honey" to R.string.flavor_honey,
                "brown_sugar" to R.string.flavor_brown_sugar,
                "bright" to R.string.flavor_bright,
                "smooth" to R.string.flavor_smooth,
                "bold" to R.string.flavor_bold,
                "earthy" to R.string.flavor_earthy,
                "woody" to R.string.flavor_woody,
                "spicy" to R.string.flavor_spicy,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                flavorKeys.forEach { (key, labelRes) ->
                    val selected = key in flavorTags
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) colors.accent else colors.accentSoft)
                            .clickable {
                                flavorTags = if (selected) flavorTags - key else flavorTags + key
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        AppText(
                            stringResource(labelRes),
                            style = CoffeeTheme.type.caption,
                            color = if (selected) Color.White else colors.textPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIcon(Glyph.BEAN, colors.accent, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                if (photoUri == null) {
                    SecondaryButton(stringResource(R.string.brew_add_photo), Modifier) {
                        val file = createTempImageFile(context)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                    }
                } else {
                    AppText(stringResource(R.string.brew_photo_attached), style = CoffeeTheme.type.caption, color = colors.accent)
                    Spacer(Modifier.width(8.dp))
                    AppText(stringResource(R.string.brew_photo_remove), style = CoffeeTheme.type.label, color = colors.textSecondary, modifier = Modifier.clickable { photoUri = null })
                }
            }

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
                            flavorTags = flavorTags.joinToString(","),
                            beanId = selectedBean?.id,
                            beanName = selectedBean?.name ?: "",
                            photoUri = photoUri?.toString(),
                        )
                    )
                }
            }
        }
    }
}

private fun sendNotification(context: Context, title: String, body: String) {
    try {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "coffeery_alerts", "Brew Alerts", NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, "coffeery_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notification)
    } catch (e: Exception) {
        android.util.Log.e("Coffeery", "Notification failed", e)
    }
}

private fun playChime(type: Int) {
    try {
        val sampleRate = 44100
        val duration = when (type) {
            0 -> 0.12f
            1 -> 0.08f
            else -> 0.30f
        }
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        when (type) {
            0 -> {
                val freq1 = 523.25
                val freq2 = 659.25
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val envelope = 1f - (t / duration)
                    samples[i] = ((envelope * 0.3 * (kotlin.math.sin(2.0 * Math.PI * freq1 * t) + kotlin.math.sin(2.0 * Math.PI * freq2 * t)).toFloat()) * Short.MAX_VALUE).toInt().toShort()
                }
            }
            2 -> {
                val notes = floatArrayOf(523.25f, 659.25f, 783.99f)
                val noteLen = numSamples / 3
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val noteIdx = (i / noteLen).coerceAtMost(2)
                    val localT = t - (noteIdx * noteLen.toFloat() / sampleRate)
                    val envelope = (1f - localT / (noteLen.toFloat() / sampleRate)).coerceAtLeast(0f)
                    val freq = notes[noteIdx]
                    samples[i] = ((envelope * 0.4 * kotlin.math.sin(2.0 * Math.PI * freq * localT)).toFloat() * Short.MAX_VALUE).toInt().toShort()
                }
            }
            else -> {
                val freq = 783.99f
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val envelope = 1f - (t / duration)
                    samples[i] = ((envelope * 0.3 * kotlin.math.sin(2.0 * Math.PI * freq * t)).toFloat() * Short.MAX_VALUE).toInt().toShort()
                }
            }
        }

        val audioTrack = android.media.AudioTrack(
            android.media.AudioManager.STREAM_NOTIFICATION,
            sampleRate,
            android.media.AudioFormat.CHANNEL_OUT_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT,
            numSamples * 2,
            android.media.AudioTrack.MODE_STATIC
        )
        audioTrack.write(samples, 0, numSamples)
        audioTrack.play()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            audioTrack.release()
        }, (duration * 1000 + 100).toLong())
    } catch (_: Exception) {
    }
}

private fun mergePours(stepsInput: List<co.coffeery.app.data.model.BrewStepDef>): List<co.coffeery.app.data.model.BrewStepDef> {
    val result = mutableListOf<co.coffeery.app.data.model.BrewStepDef>()
    var i = 0
    while (i < stepsInput.size) {
        val current = stepsInput[i]
        if (current.kind == co.coffeery.app.data.model.StepKind.POUR && i + 1 < stepsInput.size && stepsInput[i + 1].kind == co.coffeery.app.data.model.StepKind.POUR) {
            val next = stepsInput[i + 1]
            result.add(current.copy(durationSec = current.durationSec + next.durationSec, waterTargetPct = next.waterTargetPct))
            i += 2
        } else {
            result.add(current)
            i++
        }
    }
    return result
}

private fun createTempImageFile(context: Context): java.io.File {
    val dir = context.externalCacheDir ?: context.cacheDir
    return java.io.File(dir, "brew_photo_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
}
