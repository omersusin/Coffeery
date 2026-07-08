package co.coffeery.app.util

import java.util.Locale
import kotlin.math.roundToInt

object Format {

    /** "16.7" or "16" or "2.5" — one decimal, trailing ".0" trimmed. */
    fun ratio(denominator: Double): String {
        val rounded = (denominator * 10).roundToInt() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
        else String.format(Locale.US, "%.1f", rounded)
    }

    /** Coffee mass with a single decimal, e.g. "15.0" or "17.6". */
    fun grams(value: Double): String =
        String.format(Locale.US, "%.1f", value)

    fun temp(celsius: Int): String = celsius.toString()

    /** Seconds -> "m:ss" (or "h:mm:ss" for long steeps like cold brew). */
    fun clock(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, sec)
        else String.format(Locale.US, "%d:%02d", m, sec)
    }
}
