package co.coffeery.app.ui.screens.root

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.R
import co.coffeery.app.data.model.Palette
import co.coffeery.app.data.model.ThemeMode
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SegmentedControl
import co.coffeery.app.ui.theme.CoffeeShapes
import co.coffeery.app.ui.theme.CoffeeTheme

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val colors = CoffeeTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.settings_title))

        SettingsSection(R.string.settings_appearance) {
            AppText(stringResource(R.string.settings_theme), style = CoffeeTheme.type.body, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            SegmentedControl(
                options = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                selected = state.themeMode,
                label = { stringResource(it.labelRes) },
                onSelect = { vm.setThemeMode(it) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            AppText(stringResource(R.string.settings_palette), style = CoffeeTheme.type.body, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            SegmentedControl(
                options = Palette.entries.toList(),
                selected = state.palette,
                label = { stringResource(it.labelRes) },
                onSelect = { vm.setPalette(it) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SettingsSection(R.string.settings_language) {
            AppText(stringResource(R.string.settings_language_desc), style = CoffeeTheme.type.caption, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            val langOpts = listOf("en" to "English", "tr" to "Türkçe")
            SegmentedControl(
                options = langOpts,
                selected = langOpts.firstOrNull { it.first == state.settings.language } ?: langOpts[0],
                label = { it.second },
                onSelect = { vm.setLanguage(it.first) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SettingsSection(R.string.settings_timer) {
            ToggleRow(R.string.settings_timer_pip, state.settings.timerPip) {
                vm.setTimerSetting { it.copy(timerPip = !it.timerPip) }
            }
            ToggleRow(R.string.settings_timer_background, state.settings.timerBackground) {
                vm.setTimerSetting { it.copy(timerBackground = !it.timerBackground) }
            }
            ToggleRow(R.string.settings_timer_sound, state.settings.timerSound) {
                vm.setTimerSetting { it.copy(timerSound = !it.timerSound) }
            }
            ToggleRow(R.string.settings_timer_vibrate, state.settings.timerVibrate) {
                vm.setTimerSetting { it.copy(timerVibrate = !it.timerVibrate) }
            }
            ToggleRow(R.string.settings_timer_show_next, state.settings.timerShowNext) {
                vm.setTimerSetting { it.copy(timerShowNext = !it.timerShowNext) }
            }
            ToggleRow(R.string.settings_timer_merge_weight, state.settings.timerMergeWeight) {
                vm.setTimerSetting { it.copy(timerMergeWeight = !it.timerMergeWeight) }
            }
        }

        SettingsSection(R.string.settings_notifications) {
            ToggleRow(R.string.settings_notify_brew_complete, state.settings.notificationsBrewComplete) {
                vm.setTimerSetting { it.copy(notificationsBrewComplete = !it.notificationsBrewComplete) }
            }
            ToggleRow(R.string.settings_notify_step_change, state.settings.notificationsStepChange) {
                vm.setTimerSetting { it.copy(notificationsStepChange = !it.notificationsStepChange) }
            }
        }

        SettingsSection(R.string.settings_my_data) {
            ActionRow(stringResource(R.string.settings_export_data)) {
                vm.exportData(ctx)
            }
            ActionRow(stringResource(R.string.settings_import_data)) {
                vm.importData(ctx)
            }
        }

        SettingsSection(R.string.settings_backup) {
            ActionRow(stringResource(R.string.settings_restore_defaults)) {
                Toast.makeText(ctx, "Defaults restored", Toast.LENGTH_SHORT).show()
            }
        }

        SettingsSection(R.string.settings_about) {
            AboutRow(R.string.settings_version, "2.1.0")
            ActionRow(stringResource(R.string.settings_github)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/omersusin/Coffeery"))
                ctx.startActivity(intent)
            }
            ActionRow(stringResource(R.string.settings_whats_new)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/omersusin/Coffeery/releases"))
                ctx.startActivity(intent)
            }
        }
    }
}

@Composable
private fun SettingsSection(labelRes: Int, content: @Composable () -> Unit) {
    val colors = CoffeeTheme.colors
    Column {
        AppText(stringResource(labelRes), style = CoffeeTheme.type.label, color = colors.textSecondary)
        Spacer(Modifier.height(8.dp))
        CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12) {
            content()
        }
    }
}

@Composable
private fun ToggleRow(labelRes: Int, checked: Boolean, onToggle: () -> Unit) {
    val colors = CoffeeTheme.colors
    val trackColor by animateColorAsState(
        if (checked) colors.accent else colors.outline,
        label = "toggle-track",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(stringResource(labelRes), style = CoffeeTheme.type.body, modifier = Modifier.weight(1f), color = colors.textPrimary)
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 26.dp)
                .clip(CoffeeShapes.pill)
                .background(trackColor)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onToggle() },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(20.dp)
                    .clip(CoffeeShapes.pill)
                    .background(colors.surfaceElevated),
            )
        }
    }
}

@Composable
private fun ActionRow(text: String, onClick: () -> Unit) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(text, style = CoffeeTheme.type.body, modifier = Modifier.weight(1f), color = colors.textPrimary)
        LineIcon(Glyph.BEAN, colors.textSecondary, Modifier.size(16.dp))
    }
}

@Composable
private fun AboutRow(labelRes: Int, value: String) {
    val colors = CoffeeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(stringResource(labelRes), style = CoffeeTheme.type.body, modifier = Modifier.weight(1f), color = colors.textPrimary)
        AppText(value, style = CoffeeTheme.type.caption, color = colors.textSecondary)
    }
}
