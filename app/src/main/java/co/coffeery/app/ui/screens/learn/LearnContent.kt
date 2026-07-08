package co.coffeery.app.ui.screens.learn

import androidx.annotation.StringRes
import co.coffeery.app.R

/** A short knowledge card distilled from the coffee research report. */
data class LearnCard(@StringRes val titleRes: Int, @StringRes val bodyRes: Int)

/** A tasting-feedback option mapped to a corrective suggestion. */
data class TasteOption(@StringRes val labelRes: Int, @StringRes val adviceRes: Int)

object LearnContent {
    val cards: List<LearnCard> = listOf(
        LearnCard(R.string.learn_c1_title, R.string.learn_c1_body),
        LearnCard(R.string.learn_c2_title, R.string.learn_c2_body),
        LearnCard(R.string.learn_c3_title, R.string.learn_c3_body),
        LearnCard(R.string.learn_c4_title, R.string.learn_c4_body),
        LearnCard(R.string.learn_c5_title, R.string.learn_c5_body),
        LearnCard(R.string.learn_c6_title, R.string.learn_c6_body),
        LearnCard(R.string.learn_c7_title, R.string.learn_c7_body),
        LearnCard(R.string.learn_c8_title, R.string.learn_c8_body),
        LearnCard(R.string.learn_c9_title, R.string.learn_c9_body),
        LearnCard(R.string.learn_c10_title, R.string.learn_c10_body),
    )

    val tasteOptions: List<TasteOption> = listOf(
        TasteOption(R.string.taste_sour, R.string.taste_sour_advice),
        TasteOption(R.string.taste_bitter, R.string.taste_bitter_advice),
        TasteOption(R.string.taste_weak, R.string.taste_weak_advice),
        TasteOption(R.string.taste_strong, R.string.taste_strong_advice),
        TasteOption(R.string.taste_balanced, R.string.taste_balanced_advice),
    )
}
