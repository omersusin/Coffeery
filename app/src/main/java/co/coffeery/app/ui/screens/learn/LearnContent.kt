package co.coffeery.app.ui.screens.learn

import androidx.annotation.StringRes
import co.coffeery.app.R

/** A knowledge lesson distilled from the coffee research report, grouped by chapter. */
data class LearnCard(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    @StringRes val chapterRes: Int,
)

/** A tasting-feedback option mapped to a corrective suggestion. */
data class TasteOption(@StringRes val labelRes: Int, @StringRes val adviceRes: Int)

object LearnContent {
    /** Ordered so cards sharing a chapter are contiguous (drives section headers). */
    val cards: List<LearnCard> = listOf(
        LearnCard(R.string.learn_c8_title, R.string.learn_c8_body, R.string.learn_ch_basics),
        LearnCard(R.string.learn_c9_title, R.string.learn_c9_body, R.string.learn_ch_basics),
        LearnCard(R.string.learn_c6_title, R.string.learn_c6_body, R.string.learn_ch_basics),
        LearnCard(R.string.learn_l1_title, R.string.learn_l1_body, R.string.learn_ch_basics),
        LearnCard(R.string.learn_l13_title, R.string.learn_l13_body, R.string.learn_ch_basics),
        LearnCard(R.string.learn_c2_title, R.string.learn_c2_body, R.string.learn_ch_grinding),
        LearnCard(R.string.learn_l2_title, R.string.learn_l2_body, R.string.learn_ch_grinding),
        LearnCard(R.string.learn_c3_title, R.string.learn_c3_body, R.string.learn_ch_water),
        LearnCard(R.string.learn_c4_title, R.string.learn_c4_body, R.string.learn_ch_water),
        LearnCard(R.string.learn_c1_title, R.string.learn_c1_body, R.string.learn_ch_extraction),
        LearnCard(R.string.learn_c5_title, R.string.learn_c5_body, R.string.learn_ch_extraction),
        LearnCard(R.string.learn_l3_title, R.string.learn_l3_body, R.string.learn_ch_extraction),
        LearnCard(R.string.learn_l4_title, R.string.learn_l4_body, R.string.learn_ch_extraction),
        LearnCard(R.string.learn_l14_title, R.string.learn_l14_body, R.string.learn_ch_extraction),
        LearnCard(R.string.learn_l5_title, R.string.learn_l5_body, R.string.learn_ch_methods),
        LearnCard(R.string.learn_l6_title, R.string.learn_l6_body, R.string.learn_ch_methods),
        LearnCard(R.string.learn_l7_title, R.string.learn_l7_body, R.string.learn_ch_milk),
        LearnCard(R.string.learn_c7_title, R.string.learn_c7_body, R.string.learn_ch_tasting),
        LearnCard(R.string.learn_l8_title, R.string.learn_l8_body, R.string.learn_ch_tasting),
        LearnCard(R.string.learn_l9_title, R.string.learn_l9_body, R.string.learn_ch_tasting),
        LearnCard(R.string.learn_l10_title, R.string.learn_l10_body, R.string.learn_ch_caffeine),
        LearnCard(R.string.learn_l11_title, R.string.learn_l11_body, R.string.learn_ch_caffeine),
        LearnCard(R.string.learn_l15_title, R.string.learn_l15_body, R.string.learn_ch_caffeine),
        LearnCard(R.string.learn_c10_title, R.string.learn_c10_body, R.string.learn_ch_equipment),
        LearnCard(R.string.learn_l12_title, R.string.learn_l12_body, R.string.learn_ch_equipment),
        LearnCard(R.string.learn_l16_title, R.string.learn_l16_body, R.string.learn_ch_equipment),
    )

    val tasteOptions: List<TasteOption> = listOf(
        TasteOption(R.string.taste_sour, R.string.taste_sour_advice),
        TasteOption(R.string.taste_bitter, R.string.taste_bitter_advice),
        TasteOption(R.string.taste_astringent, R.string.taste_astringent_advice),
        TasteOption(R.string.taste_weak, R.string.taste_weak_advice),
        TasteOption(R.string.taste_strong, R.string.taste_strong_advice),
        TasteOption(R.string.taste_balanced, R.string.taste_balanced_advice),
        TasteOption(R.string.taste_dry, R.string.taste_dry_advice),
        TasteOption(R.string.taste_hollow, R.string.taste_hollow_advice),
    )
}
