package co.coffeery.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.coffeery.app.data.model.Equipment

/** Resolves a localized name for built-in gear, or the raw name for custom gear. */
@Composable
fun Equipment.displayName(): String =
    customName ?: if (nameRes != 0) stringResource(nameRes) else "—"

@Composable
fun Equipment.displayTag(): String? =
    if (isCustom || tagRes == 0) null else stringResource(tagRes)
