package co.coffeery.app.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.coffeery.app.R
import co.coffeery.app.data.local.BeanEntity
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.CoffeeCard
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.ScreenHeader
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.NavTab
import co.coffeery.app.ui.theme.CoffeeTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BeanDetailScreen(beanId: Long, vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val bean = vm.getBean(beanId)
    val colors = CoffeeTheme.colors

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 96.dp),
    ) {
        ScreenHeader(
            title = bean?.name ?: stringResource(R.string.bean_detail_title),
            onBack = { vm.back() },
        )

        if (bean == null) {
            Spacer(Modifier.height(40.dp))
            AppText(
                stringResource(R.string.bean_detail_title),
                style = CoffeeTheme.type.body,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            )
            return
        }

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            InfoCard(bean)

            if (bean.notes.isNotBlank()) {
                NotesCard(bean.notes)
            }

            if (bean.flavorNotes.isNotBlank()) {
                FlavorTagsSection(bean.flavorNotes)
            }

            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = stringResource(R.string.bean_detail_brewing),
            modifier = Modifier.fillMaxWidth(),
        ) {
            vm.selectTab(NavTab.BREW)
        }
    }
}

@Composable
private fun InfoCard(bean: BeanEntity) {
    val colors = CoffeeTheme.colors

    CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LineIcon(Glyph.BEAN, colors.accent, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                val originRoaster = listOfNotNull(
                    bean.roaster.takeIf { it.isNotBlank() },
                    bean.origin.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                AppText(originRoaster.ifBlank { bean.name }, style = CoffeeTheme.type.headline, color = colors.textPrimary)
            }
        }

        if (bean.roastDate != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val freshness = freshnessColor(bean.roastDate)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(freshness),
                )
                Spacer(Modifier.width(8.dp))
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date(bean.roastDate))
                AppText(
                    "${stringResource(R.string.bean_detail_roast_date)}: $dateStr",
                    style = CoffeeTheme.type.body,
                    color = colors.textSecondary,
                )
            }
        }

        val detailRows = buildList {
            if (bean.processMethod.isNotBlank()) add(Pair(R.string.bean_detail_process, bean.processMethod))
            if (bean.varietal.isNotBlank()) add(Pair(R.string.bean_detail_varietal, bean.varietal))
            if (bean.altitude.isNotBlank()) add(Pair(R.string.bean_detail_altitude, bean.altitude))
            if (bean.scaScore != null) add(Pair(R.string.bean_detail_sca, String.format(Locale.US, "%.1f", bean.scaScore)))
            if (bean.purchaseDate != null) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date(bean.purchaseDate))
                add(Pair(R.string.bean_detail_purchase, dateStr))
            }
        }

        if (detailRows.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            detailRows.forEach { (labelRes, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    AppText(
                        "${stringResource(labelRes)}: ",
                        style = CoffeeTheme.type.label,
                        color = colors.textSecondary,
                    )
                    AppText(value, style = CoffeeTheme.type.label, color = colors.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    val colors = CoffeeTheme.colors

    CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        AppText(notes, style = CoffeeTheme.type.body, color = colors.textPrimary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlavorTagsSection(flavorNotes: String) {
    val colors = CoffeeTheme.colors
    val tags = flavorNotes.split(",").map { it.trim() }.filter { it.isNotBlank() }

    CoffeeCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(co.coffeery.app.ui.theme.CoffeeShapes.pill)
                        .background(colors.surfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    AppText(tag, style = CoffeeTheme.type.label, color = colors.textPrimary)
                }
            }
        }
    }
}

private fun freshnessColor(roastDate: Long): Color {
    val days = (System.currentTimeMillis() - roastDate) / (1000 * 60 * 60 * 24)
    return when {
        days < 14 -> Color(0xFF4CAF50)
        days < 30 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}
