package co.coffeery.app.ui.screens.brew

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var remaining by rememberSaveable { mutableIntStateOf(steps[0].durationSec) }
    var running by rememberSaveable { mutableStateOf(false) }
    var everStarted by rememberSaveable { mutableStateOf(false) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var elapsedTotal by rememberSaveable { mutableIntStateOf(0) }

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

    LaunchedEffect(running) {
        while (running && !finished) {
            delay(1000)
            elapsedTotal++
            val next = remaining - 1
            if (next <= 0) {
                if (stepIndex < steps.lastIndex) {
                    stepIndex++
                    remaining = steps[stepIndex].durationSec
                    if (state.settings.timerVibrate) {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(50)
                        }
                    }
                    if (state.settings.timerSound) {
                        try {
                            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
                            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                        } catch (_: Exception) {
                        }
                    }
                    if (state.settings.notificationsStepChange) {
                        val currentStep = steps[stepIndex]
                        val stepTitle = context.getString(currentStep.titleRes)
                        sendNotification(context, "Next step", "Step ${stepIndex+1}/${steps.size}: $stepTitle")
                    }
                } else {
                    remaining = 0
                    running = false
                    finished = true
                }
            } else {
                remaining = next
            }
            if (state.settings.timerBackground) {
                if (!TimerService.isRunning) {
                    running = false
                } else {
                    val title = stepTitles.getOrElse(stepIndex) { "" }
                    TimerService.update(context, equipmentName, title, Format.clock(remaining), stepIndex, steps.size)
                }
            }
        }
        if (finished) {
            if (state.settings.timerVibrate) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
            if (state.settings.timerSound) {
                try {
                    val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                } catch (_: Exception) {
                }
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
    var selectedBean by remember { mutableStateOf<BeanEntity?>(null) }
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
                            beanId = selectedBean?.id,
                            beanName = selectedBean?.name ?: "",
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
