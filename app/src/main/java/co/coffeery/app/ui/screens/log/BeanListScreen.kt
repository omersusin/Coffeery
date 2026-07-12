package co.coffeery.app.ui.screens.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.R
import co.coffeery.app.data.local.BeanEntity
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.AppTextField
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.CoffeeDialog
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.Route
import co.coffeery.app.ui.theme.CoffeeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BeanListScreen(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = CoffeeTheme.colors
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 96.dp)) {
        ScreenHeader(title = stringResource(R.string.beans_title))

        if (state.beans.isEmpty()) {
            Spacer(Modifier.height(60.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LineIcon(Glyph.BEAN, colors.textSecondary.copy(alpha = 0.4f), Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                AppText(stringResource(R.string.empty_beans_title), style = CoffeeTheme.type.title, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppText(stringResource(R.string.empty_beans_sub), style = CoffeeTheme.type.body, color = colors.textSecondary, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                PrimaryButton(stringResource(R.string.empty_beans_action), modifier = Modifier.fillMaxWidth()) { showAdd = true }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            ) {
                state.beans.forEach { bean ->
                    BeanCard(bean, vm)
                }
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton(stringResource(R.string.beans_add), Modifier.fillMaxWidth()) { showAdd = true }
        }
    }

    if (showAdd) {
        AddBeanDialog(onDismiss = { showAdd = false }) { name, origin, roaster, roastDate, notes, processMethod, varietal, altitude, scaScore, purchaseDate ->
            vm.addBean(name, origin, roaster, roastDate, notes, processMethod, varietal, altitude, scaScore, purchaseDate)
            showAdd = false
        }
    }
}

@Composable
private fun BeanCard(bean: BeanEntity, vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14, onClick = { vm.openRoute(Route.BeanDetail(bean.id)) }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                AppText(bean.name, style = CoffeeTheme.type.headline, color = colors.textPrimary)
                if (bean.origin.isNotBlank() || bean.roaster.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    AppText(listOfNotNull(bean.roaster.takeIf { it.isNotBlank() }, bean.origin.takeIf { it.isNotBlank() }).joinToString(" · "), style = CoffeeTheme.type.caption, color = colors.textSecondary)
                }
                if (bean.roastDate != null) {
                    Spacer(Modifier.height(2.dp))
                    val days = (System.currentTimeMillis() - bean.roastDate) / 86400000
                    AppText("${days}d since roast · ${bean.roastLevel}", style = CoffeeTheme.type.caption, color = colors.accent)
                }
                if (bean.processMethod.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    AppText(bean.processMethod + if (bean.varietal.isNotBlank()) " · ${bean.varietal}" else "", style = CoffeeTheme.type.caption, color = colors.textSecondary)
                }
                if (bean.altitude.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    AppText(bean.altitude, style = CoffeeTheme.type.caption, color = colors.textSecondary)
                }
                if (bean.flavorNotes.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    AppText(bean.flavorNotes.replace(",", " · "), style = CoffeeTheme.type.caption, color = colors.accent)
                }
                if (bean.scaScore != null) {
                    Spacer(Modifier.height(2.dp))
                    AppText("SCA: ${String.format(Locale.US, "%.1f", bean.scaScore)}", style = CoffeeTheme.type.caption, color = colors.accent)
                }
                if (bean.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    AppText(bean.notes, style = CoffeeTheme.type.body, color = colors.textPrimary, maxLines = 2)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AddBeanDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, origin: String, roaster: String, roastDate: Long?, notes: String, processMethod: String, varietal: String, altitude: String, scaScore: Float?, purchaseDate: Long?) -> Unit,
) {
    val colors = CoffeeTheme.colors
    var name by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var roaster by remember { mutableStateOf("") }
    var roastDateStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var processMethod by remember { mutableStateOf("") }
    var varietal by remember { mutableStateOf("") }
    var altitude by remember { mutableStateOf("") }
    var scaScoreStr by remember { mutableStateOf("") }
    var purchaseDateStr by remember { mutableStateOf("") }
    val processOptions = listOf("Washed", "Natural", "Honey", "Anaerobic")

    CoffeeDialog(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            AppText(stringResource(R.string.beans_add), style = CoffeeTheme.type.title)
            Spacer(Modifier.height(10.dp))
            AppTextField(value = name, onValueChange = { name = it }, hint = stringResource(R.string.beans_name_hint), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            AppTextField(value = origin, onValueChange = { origin = it }, hint = stringResource(R.string.beans_origin_hint), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            AppTextField(value = roaster, onValueChange = { roaster = it }, hint = stringResource(R.string.beans_roaster_hint), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            AppTextField(value = roastDateStr, onValueChange = { roastDateStr = it }, hint = stringResource(R.string.beans_date_hint), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))

            AppText(stringResource(R.string.bean_process), style = CoffeeTheme.type.label, color = colors.textSecondary)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                processOptions.forEach { opt ->
                    val selected = processMethod == opt
                    PrimaryButton(
                        text = opt,
                        enabled = selected,
                        shape = co.coffeery.app.ui.theme.CoffeeShapes.pill,
                    ) { processMethod = opt }
                }
            }

            Spacer(Modifier.height(6.dp))
            AppTextField(value = varietal, onValueChange = { varietal = it }, hint = stringResource(R.string.bean_varietal), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            AppTextField(value = altitude, onValueChange = { altitude = it }, hint = stringResource(R.string.bean_altitude), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            AppTextField(value = scaScoreStr, onValueChange = { scaScoreStr = it }, hint = stringResource(R.string.bean_sca_score), modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
            Spacer(Modifier.height(6.dp))
            AppTextField(value = purchaseDateStr, onValueChange = { purchaseDateStr = it }, hint = stringResource(R.string.bean_purchase_date), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            AppTextField(value = notes, onValueChange = { notes = it }, hint = stringResource(R.string.beans_notes_hint), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { onDismiss() }
                PrimaryButton(stringResource(R.string.action_add), Modifier.weight(1f), enabled = name.isNotBlank()) {
                    val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(roastDateStr)?.time } catch (_: Exception) { null }
                    val purchaseDate = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(purchaseDateStr)?.time } catch (_: Exception) { null }
                    val scaScore = try { scaScoreStr.toFloat().coerceIn(0f, 100f).takeIf { scaScoreStr.isNotBlank() } } catch (_: Exception) { null }
                    onAdd(name.trim(), origin.trim(), roaster.trim(), date, notes.trim(), processMethod.trim(), varietal.trim(), altitude.trim(), scaScore, purchaseDate)
                }
            }
        }
    }
}
