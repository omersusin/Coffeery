package co.coffeery.app.util

import co.coffeery.app.R
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.data.model.Grind
import co.coffeery.app.data.model.RoastLevel
import co.coffeery.app.data.model.TempMode
import kotlin.math.roundToInt

/** Fully resolved recipe output for the calculator + timer screens. */
data class BrewResult(
    val ratioDenominator: Double,
    val coffeeGrams: Double,
    val waterMl: Int,
    val grind: Grind,
    val tempMode: TempMode,
    val tempCelsius: Int,        // meaningful only when tempMode == RANGE
    val strengthBandRes: Int,
)

object BrewMath {

    /** Strength axis: 0f = mildest (ratioMax), 1f = strongest (ratioMin). */
    fun ratioFor(e: Equipment, strength: Float): Double {
        val t = strength.coerceIn(0f, 1f)
        return e.ratioMax - t * (e.ratioMax - e.ratioMin)
    }

    fun totalWater(e: Equipment, byCups: Boolean, cups: Int, waterMl: Int): Int =
        if (byCups) (cups.coerceAtLeast(1) * e.cupMl) else waterMl.coerceAtLeast(1)

    fun compute(
        e: Equipment,
        strength: Float,
        roast: RoastLevel,
        byCups: Boolean,
        cups: Int,
        waterMl: Int,
    ): BrewResult {
        val ratio = ratioFor(e, strength)
        val water = totalWater(e, byCups, cups, waterMl)
        val coffee = water / ratio
        val grind = e.grind.shifted(roast.grindShift)
        val temp = if (e.tempMode == TempMode.RANGE) {
            val mid = (e.tempMin + e.tempMax) / 2
            (mid + roast.tempOffset).coerceIn(e.tempMin - 2, e.tempMax)
        } else 0
        val band = when {
            strength < 0.34f -> R.string.strength_light_label
            strength < 0.67f -> R.string.strength_balanced_label
            else -> R.string.strength_strong_label
        }
        return BrewResult(
            ratioDenominator = ratio,
            coffeeGrams = coffee,
            waterMl = water,
            grind = grind,
            tempMode = e.tempMode,
            tempCelsius = temp,
            strengthBandRes = band,
        )
    }

    /** Water (g) that should be in the vessel by the end of a pour step. */
    fun stepWaterGrams(pct: Float, totalWater: Int): Int =
        (pct * totalWater).roundToInt()
}
