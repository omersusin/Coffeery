package co.coffeery.app.ui.screens.root

import androidx.annotation.StringRes
import co.coffeery.app.R
import co.coffeery.app.ui.components.Glyph

/** Bottom navigation destinations. */
enum class NavTab(@StringRes val labelRes: Int, val glyph: Glyph) {
    BREW(R.string.nav_brew, Glyph.DROP),
    GEAR(R.string.nav_equipment, Glyph.CONE),
    RECIPES(R.string.nav_recipes, Glyph.BOOKMARK),
    LEARN(R.string.nav_learn, Glyph.BOOK),
}

/** Full-screen routes layered above the tab scaffold. */
sealed interface Route {
    data object Tabs : Route
    data object AddEquipment : Route
    data object Timer : Route
    data class LearnDetail(val cardIndex: Int) : Route
}
