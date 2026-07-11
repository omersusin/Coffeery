package co.coffeery.app.ui.screens.drinks

import androidx.annotation.StringRes
import co.coffeery.app.R

/** Which catalog section a drink belongs to. */
enum class DrinkGroup(@StringRes val labelRes: Int) {
    MILK(R.string.drink_group_milk),
    REGIONAL(R.string.drink_group_regional),
}

/** A built (not ratio-brewed) drink recipe: milk-based or regional. */
data class DrinkItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val summaryRes: Int,
    @StringRes val bodyRes: Int,
    val group: DrinkGroup,
)

object DrinkContent {
    /** Ordered so drinks sharing a group are contiguous (drives section headers). */
    val drinks: List<DrinkItem> = listOf(
        DrinkItem("latte", R.string.drink_latte_name, R.string.drink_latte_summary, R.string.drink_latte_body, DrinkGroup.MILK),
        DrinkItem("cappuccino", R.string.drink_cappuccino_name, R.string.drink_cappuccino_summary, R.string.drink_cappuccino_body, DrinkGroup.MILK),
        DrinkItem("flatwhite", R.string.drink_flatwhite_name, R.string.drink_flatwhite_summary, R.string.drink_flatwhite_body, DrinkGroup.MILK),
        DrinkItem("cortado", R.string.drink_cortado_name, R.string.drink_cortado_summary, R.string.drink_cortado_body, DrinkGroup.MILK),
        DrinkItem("macchiato", R.string.drink_macchiato_name, R.string.drink_macchiato_summary, R.string.drink_macchiato_body, DrinkGroup.MILK),
        DrinkItem("mocha", R.string.drink_mocha_name, R.string.drink_mocha_summary, R.string.drink_mocha_body, DrinkGroup.MILK),
        DrinkItem("affogato", R.string.drink_affogato_name, R.string.drink_affogato_summary, R.string.drink_affogato_body, DrinkGroup.MILK),
        DrinkItem("americano", R.string.drink_americano_name, R.string.drink_americano_summary, R.string.drink_americano_body, DrinkGroup.MILK),
        DrinkItem("redeye", R.string.drink_redeye_name, R.string.drink_redeye_summary, R.string.drink_redeye_body, DrinkGroup.MILK),
        DrinkItem("doppio", R.string.drink_doppio_name, R.string.drink_doppio_summary, R.string.drink_doppio_body, DrinkGroup.MILK),
        DrinkItem("ristretto", R.string.drink_ristretto_name, R.string.drink_ristretto_summary, R.string.drink_ristretto_body, DrinkGroup.MILK),
        DrinkItem("lungo", R.string.drink_lungo_name, R.string.drink_lungo_summary, R.string.drink_lungo_body, DrinkGroup.MILK),
        DrinkItem("marocchino", R.string.drink_marocchino_name, R.string.drink_marocchino_summary, R.string.drink_marocchino_body, DrinkGroup.MILK),
        DrinkItem("caphetrung", R.string.drink_caphetrung_name, R.string.drink_caphetrung_summary, R.string.drink_caphetrung_body, DrinkGroup.REGIONAL),
        DrinkItem("caphesuada", R.string.drink_caphesuada_name, R.string.drink_caphesuada_summary, R.string.drink_caphesuada_body, DrinkGroup.REGIONAL),
        DrinkItem("greekfrappe", R.string.drink_greekfrappe_name, R.string.drink_greekfrappe_summary, R.string.drink_greekfrappe_body, DrinkGroup.REGIONAL),
        DrinkItem("irishcoffee", R.string.drink_irishcoffee_name, R.string.drink_irishcoffee_summary, R.string.drink_irishcoffee_body, DrinkGroup.REGIONAL),
        DrinkItem("cafebombon", R.string.drink_cafebombon_name, R.string.drink_cafebombon_summary, R.string.drink_cafebombon_body, DrinkGroup.REGIONAL),
        DrinkItem("cortadito", R.string.drink_cortadito_name, R.string.drink_cortadito_summary, R.string.drink_cortadito_body, DrinkGroup.REGIONAL),
        DrinkItem("viennese", R.string.drink_viennese_name, R.string.drink_viennese_summary, R.string.drink_viennese_body, DrinkGroup.REGIONAL),
        DrinkItem("cubano", R.string.drink_cubano_name, R.string.drink_cubano_summary, R.string.drink_cubano_body, DrinkGroup.REGIONAL),
        DrinkItem("conmiel", R.string.drink_conmiel_name, R.string.drink_conmiel_summary, R.string.drink_conmiel_body, DrinkGroup.REGIONAL),
        DrinkItem("barraquito", R.string.drink_barraquito_name, R.string.drink_barraquito_summary, R.string.drink_barraquito_body, DrinkGroup.REGIONAL),
        DrinkItem("eiskaffee", R.string.drink_eiskaffee_name, R.string.drink_eiskaffee_summary, R.string.drink_eiskaffee_body, DrinkGroup.REGIONAL),
        DrinkItem("touba", R.string.drink_touba_name, R.string.drink_touba_summary, R.string.drink_touba_body, DrinkGroup.REGIONAL),
        DrinkItem("caramacchiato", R.string.drink_caramacchiato_name, R.string.drink_caramacchiato_summary, R.string.drink_caramacchiato_body, DrinkGroup.MILK),
        DrinkItem("espressino", R.string.drink_espressino_name, R.string.drink_espressino_summary, R.string.drink_espressino_body, DrinkGroup.MILK),
        DrinkItem("lagrima", R.string.drink_lagrima_name, R.string.drink_lagrima_summary, R.string.drink_lagrima_body, DrinkGroup.MILK),
        DrinkItem("galao", R.string.drink_galao_name, R.string.drink_galao_summary, R.string.drink_galao_body, DrinkGroup.MILK),
        DrinkItem("piccolo", R.string.drink_piccolo_name, R.string.drink_piccolo_summary, R.string.drink_piccolo_body, DrinkGroup.MILK),
        DrinkItem("breve", R.string.drink_breve_name, R.string.drink_breve_summary, R.string.drink_breve_body, DrinkGroup.MILK),
        DrinkItem("bulletproof", R.string.drink_bulletproof_name, R.string.drink_bulletproof_summary, R.string.drink_bulletproof_body, DrinkGroup.MILK),
        DrinkItem("bicerin", R.string.drink_bicerin_name, R.string.drink_bicerin_summary, R.string.drink_bicerin_body, DrinkGroup.REGIONAL),
        DrinkItem("espresso_romano", R.string.drink_espresso_romano_name, R.string.drink_espresso_romano_summary, R.string.drink_espresso_romano_body, DrinkGroup.REGIONAL),
        DrinkItem("mazagran", R.string.drink_mazagran_name, R.string.drink_mazagran_summary, R.string.drink_mazagran_body, DrinkGroup.REGIONAL),
        DrinkItem("carajillo", R.string.drink_carajas_name, R.string.drink_carajas_summary, R.string.drink_carajas_body, DrinkGroup.REGIONAL),
        DrinkItem("asiatico", R.string.drink_asiatico_name, R.string.drink_asiatico_summary, R.string.drink_asiatico_body, DrinkGroup.REGIONAL),
        DrinkItem("pharisaer", R.string.drink_pharisaer_name, R.string.drink_pharisaer_summary, R.string.drink_pharisaer_body, DrinkGroup.REGIONAL),
        DrinkItem("kaffeost", R.string.drink_kaffeost_name, R.string.drink_kaffeost_summary, R.string.drink_kaffeost_body, DrinkGroup.REGIONAL),
        DrinkItem("flatred", R.string.drink_flatred_name, R.string.drink_flatred_summary, R.string.drink_flatred_body, DrinkGroup.REGIONAL),
        DrinkItem("cascara", R.string.drink_cascara_name, R.string.drink_cascara_summary, R.string.drink_cascara_body, DrinkGroup.REGIONAL),
        DrinkItem("qishr", R.string.drink_qishr_name, R.string.drink_qishr_summary, R.string.drink_qishr_body, DrinkGroup.REGIONAL),
        DrinkItem("espresso_yen", R.string.drink_espresso_yen_name, R.string.drink_espresso_yen_summary, R.string.drink_espresso_yen_body, DrinkGroup.REGIONAL),
        DrinkItem("magic", R.string.drink_magic_name, R.string.drink_magic_summary, R.string.drink_magic_body, DrinkGroup.REGIONAL),
        DrinkItem("long_macchiato", R.string.drink_long_macchiato_name, R.string.drink_long_macchiato_summary, R.string.drink_long_macchiato_body, DrinkGroup.MILK),
        DrinkItem("cafe_bombon", R.string.drink_cafe_bombon_name, R.string.drink_cafe_bombon_summary, R.string.drink_cafe_bombon_body, DrinkGroup.REGIONAL),
        DrinkItem("einspanner", R.string.drink_einspanner_name, R.string.drink_einspanner_summary, R.string.drink_einspanner_body, DrinkGroup.REGIONAL),
        DrinkItem("dalgona", R.string.drink_dalgona_name, R.string.drink_dalgona_summary, R.string.drink_dalgona_body, DrinkGroup.REGIONAL),
        DrinkItem("yuanyang", R.string.drink_yuanyang_name, R.string.drink_yuanyang_summary, R.string.drink_yuanyang_body, DrinkGroup.REGIONAL),
        DrinkItem("cuban", R.string.drink_cuban_name, R.string.drink_cuban_summary, R.string.drink_cuban_body, DrinkGroup.REGIONAL),
        DrinkItem("cafe_de_olla", R.string.drink_cafe_de_olla_name, R.string.drink_cafe_de_olla_summary, R.string.drink_cafe_de_olla_body, DrinkGroup.REGIONAL),
        DrinkItem("freddo_espresso", R.string.drink_freddo_espresso_name, R.string.drink_freddo_espresso_summary, R.string.drink_freddo_espresso_body, DrinkGroup.REGIONAL),
        DrinkItem("freddo_cappuccino", R.string.drink_freddo_cappuccino_name, R.string.drink_freddo_cappuccino_summary, R.string.drink_freddo_cappuccino_body, DrinkGroup.REGIONAL),
        DrinkItem("scandi_egg", R.string.drink_scandi_egg_name, R.string.drink_scandi_egg_summary, R.string.drink_scandi_egg_body, DrinkGroup.REGIONAL),
        DrinkItem("kopi_joss", R.string.drink_kopi_joss_name, R.string.drink_kopi_joss_summary, R.string.drink_kopi_joss_body, DrinkGroup.REGIONAL),
        DrinkItem("spanish_latte", R.string.drink_spanish_latte_name, R.string.drink_spanish_latte_summary, R.string.drink_spanish_latte_body, DrinkGroup.MILK),
        DrinkItem("pumpkin_spice", R.string.drink_pumpkin_spice_name, R.string.drink_pumpkin_spice_summary, R.string.drink_pumpkin_spice_body, DrinkGroup.MILK),
        DrinkItem("affogato", R.string.drink_affogato_name, R.string.drink_affogato_summary, R.string.drink_affogato_body, DrinkGroup.MILK),
    )
}

data class CoffeeVariety(
    @StringRes val nameRes: Int,
    @StringRes val originRes: Int,
    @StringRes val flavorRes: Int,
    @StringRes val bestBrewRes: Int,
)

object VarietyContent {
    val varieties: List<CoffeeVariety> = listOf(
        CoffeeVariety(R.string.variety_typica_name, R.string.variety_typica_origin, R.string.variety_typica_flavor, R.string.variety_typica_brew),
        CoffeeVariety(R.string.variety_bourbon_name, R.string.variety_bourbon_origin, R.string.variety_bourbon_flavor, R.string.variety_bourbon_brew),
        CoffeeVariety(R.string.variety_gesha_name, R.string.variety_gesha_origin, R.string.variety_gesha_flavor, R.string.variety_gesha_brew),
        CoffeeVariety(R.string.variety_sl28_name, R.string.variety_sl28_origin, R.string.variety_sl28_flavor, R.string.variety_sl28_brew),
        CoffeeVariety(R.string.variety_caturra_name, R.string.variety_caturra_origin, R.string.variety_caturra_flavor, R.string.variety_caturra_brew),
        CoffeeVariety(R.string.variety_pacamara_name, R.string.variety_pacamara_origin, R.string.variety_pacamara_flavor, R.string.variety_pacamara_brew),
        CoffeeVariety(R.string.variety_robusta_name, R.string.variety_robusta_origin, R.string.variety_robusta_flavor, R.string.variety_robusta_brew),
        CoffeeVariety(R.string.variety_heirloom_name, R.string.variety_heirloom_origin, R.string.variety_heirloom_flavor, R.string.variety_heirloom_brew),
        CoffeeVariety(R.string.variety_mundonovo_name, R.string.variety_mundonovo_origin, R.string.variety_mundonovo_flavor, R.string.variety_mundonovo_brew),
        CoffeeVariety(R.string.variety_catuai_name, R.string.variety_catuai_origin, R.string.variety_catuai_flavor, R.string.variety_catuai_brew),
        CoffeeVariety(R.string.variety_maragogipe_name, R.string.variety_maragogipe_origin, R.string.variety_maragogipe_flavor, R.string.variety_maragogipe_brew),
        CoffeeVariety(R.string.variety_liberica_name, R.string.variety_liberica_origin, R.string.variety_liberica_flavor, R.string.variety_liberica_brew),
        CoffeeVariety(R.string.variety_sl34_name, R.string.variety_sl34_origin, R.string.variety_sl34_flavor, R.string.variety_sl34_brew),
        CoffeeVariety(R.string.variety_mundo_novo_name, R.string.variety_mundo_novo_origin, R.string.variety_mundo_novo_flavor, R.string.variety_mundo_novo_brew),
        CoffeeVariety(R.string.variety_jamaica_blue_name, R.string.variety_jamaica_blue_origin, R.string.variety_jamaica_blue_flavor, R.string.variety_jamaica_blue_brew),
        CoffeeVariety(R.string.variety_kona_name, R.string.variety_kona_origin, R.string.variety_kona_flavor, R.string.variety_kona_brew),
        CoffeeVariety(R.string.variety_mokka_name, R.string.variety_mokka_origin, R.string.variety_mokka_flavor, R.string.variety_mokka_brew),
        CoffeeVariety(R.string.variety_villa_sarchi_name, R.string.variety_villa_sarchi_origin, R.string.variety_villa_sarchi_flavor, R.string.variety_villa_sarchi_brew),
        CoffeeVariety(R.string.variety_ruiru_11_name, R.string.variety_ruiru_11_origin, R.string.variety_ruiru_11_flavor, R.string.variety_ruiru_11_brew),
        CoffeeVariety(R.string.variety_s795_name, R.string.variety_s795_origin, R.string.variety_s795_flavor, R.string.variety_s795_brew),
        CoffeeVariety(R.string.variety_catimor_name, R.string.variety_catimor_origin, R.string.variety_catimor_flavor, R.string.variety_catimor_brew),
        CoffeeVariety(R.string.variety_starmaya_name, R.string.variety_starmaya_origin, R.string.variety_starmaya_flavor, R.string.variety_starmaya_brew),
    )
}
