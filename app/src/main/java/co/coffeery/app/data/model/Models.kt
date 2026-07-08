package co.coffeery.app.data.model

import androidx.annotation.StringRes
import co.coffeery.app.R

/** The four families a brewer can belong to. */
enum class BrewCategory(
    @StringRes val labelRes: Int,
    @StringRes val descRes: Int,
) {
    POUR_OVER(R.string.cat_pour_over, R.string.cat_pour_over_desc),
    IMMERSION(R.string.cat_immersion, R.string.cat_immersion_desc),
    PRESSURE(R.string.cat_pressure, R.string.cat_pressure_desc),
    OTHER(R.string.cat_other, R.string.cat_other_desc);

    companion object {
        fun fromKey(key: String?): BrewCategory =
            entries.firstOrNull { it.name == key } ?: OTHER
    }
}

/**
 * Grind sizes ordered fine -> coarse. Ordinal is used to nudge grind by roast.
 */
enum class Grind(
    @StringRes val labelRes: Int,
    @StringRes val refRes: Int,
) {
    EXTRA_FINE(R.string.grind_extra_fine, R.string.grindref_extra_fine),
    FINE(R.string.grind_fine, R.string.grindref_fine),
    MED_FINE(R.string.grind_med_fine, R.string.grindref_med_fine),
    MEDIUM(R.string.grind_medium, R.string.grindref_medium),
    MED_COARSE(R.string.grind_med_coarse, R.string.grindref_med_coarse),
    COARSE(R.string.grind_coarse, R.string.grindref_coarse),
    EXTRA_COARSE(R.string.grind_extra_coarse, R.string.grindref_extra_coarse);

    /** Shift by [steps]; positive = coarser, negative = finer. Clamped. */
    fun shifted(steps: Int): Grind {
        val idx = (ordinal + steps).coerceIn(0, entries.size - 1)
        return entries[idx]
    }

    companion object {
        fun fromKey(key: String?): Grind =
            entries.firstOrNull { it.name == key } ?: MEDIUM
    }
}

/** How the target water temperature should be presented for a method. */
enum class TempMode { RANGE, SLOW, COLD;
    companion object {
        fun fromKey(key: String?): TempMode =
            entries.firstOrNull { it.name == key } ?: RANGE
    }
}

/**
 * Roast level fine-tunes the recipe. Faithful to the research report's water
 * table (light roasts brew hotter, dark roasts cooler) and extraction theory
 * (denser light roasts grind finer, brittle dark roasts grind coarser).
 */
enum class RoastLevel(
    @StringRes val labelRes: Int,
    @StringRes val descRes: Int,
    val tempOffset: Int,
    val grindShift: Int,
) {
    LIGHT(R.string.roast_light, R.string.roast_light_desc, 2, -1),
    MEDIUM(R.string.roast_medium, R.string.roast_medium_desc, 0, 0),
    DARK(R.string.roast_dark, R.string.roast_dark_desc, -3, 1);
}

/** Kind of a brew step — drives the illustration/icon shown in the timer. */
enum class StepKind { RINSE, BLOOM, POUR, SWIRL, STEEP, STIR, PRESS, PLUNGE, DRAWDOWN, SKIM, HEAT, REMOVE_HEAT, SERVE, WAIT;
    companion object {
        fun fromKey(key: String?): StepKind =
            entries.firstOrNull { it.name == key } ?: POUR
    }
}

/**
 * A single timed step in a brew routine.
 * @param titleKey resolved to a localized string at display time.
 * @param waterTargetPct cumulative fraction (0..1) of total water reached by
 *   the END of this step, or -1 if the step adds no water.
 */
data class BrewStepDef(
    val titleRes: Int,
    val kind: StepKind,
    val durationSec: Int,
    val waterTargetPct: Float,
)

/** A brewing method (built-in preset or user-created). */
data class Equipment(
    val id: String,
    val nameRes: Int,
    val tagRes: Int,
    val category: BrewCategory,
    val ratioMin: Double,      // strongest (smallest water-per-gram denominator)
    val ratioMax: Double,      // mildest
    val ratioDefault: Double,
    val tempMode: TempMode,
    val tempMin: Int,
    val tempMax: Int,
    val grind: Grind,
    val cupMl: Int,
    val timeLabel: String,
    val hasBloom: Boolean,
    val steps: List<BrewStepDef>,
    val isCustom: Boolean = false,
    /** Non-null only for user-created gear; takes precedence over [nameRes]. */
    val customName: String? = null,
) {
    /** Position of [ratioDefault] on the 0..1 strength axis (1 = strongest). */
    val defaultStrength: Float
        get() {
            if (ratioMax == ratioMin) return 0.5f
            return ((ratioMax - ratioDefault) / (ratioMax - ratioMin))
                .toFloat().coerceIn(0f, 1f)
        }
}
