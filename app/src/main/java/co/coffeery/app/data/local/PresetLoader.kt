package co.coffeery.app.data.local

import android.content.Context
import co.coffeery.app.R
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.model.BrewStepDef
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.data.model.Grind
import co.coffeery.app.data.model.StepKind
import co.coffeery.app.data.model.TempMode
import org.json.JSONObject

/**
 * Loads the bundled research-data asset (equipment_presets.json) and maps it to
 * [Equipment] domain objects. String keys in the JSON are resolved to localized
 * string resources so all preset text respects the current locale.
 */
object PresetLoader {

    private const val ASSET = "equipment_presets.json"

    fun loadBuiltIns(context: Context): List<Equipment> {
        val json = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("equipment")
        val result = ArrayList<Equipment>(arr.length())
        for (i in 0 until arr.length()) {
            result.add(parse(arr.getJSONObject(i)))
        }
        return result
    }

    private fun parse(o: JSONObject): Equipment {
        val steps = ArrayList<BrewStepDef>()
        val stepArr = o.getJSONArray("steps")
        for (j in 0 until stepArr.length()) {
            val s = stepArr.getJSONObject(j)
            steps.add(
                BrewStepDef(
                    titleRes = stringRes(s.getString("titleKey")),
                    kind = StepKind.fromKey(s.optString("kind")),
                    durationSec = s.getInt("durationSec"),
                    waterTargetPct = s.getDouble("waterTargetPct").toFloat(),
                )
            )
        }
        val youtubeUrl = o.optString("youtubeUrl", "").takeIf { it.isNotEmpty() }
        return Equipment(
            id = o.getString("id"),
            nameRes = stringRes(o.getString("nameKey")),
            tagRes = stringRes(o.getString("tagKey")),
            category = BrewCategory.fromKey(o.getString("category")),
            ratioMin = o.getDouble("ratioMin"),
            ratioMax = o.getDouble("ratioMax"),
            ratioDefault = o.getDouble("ratioDefault"),
            tempMode = TempMode.fromKey(o.getString("tempMode")),
            tempMin = o.getInt("tempMin"),
            tempMax = o.getInt("tempMax"),
            grind = Grind.fromKey(o.getString("grind")),
            cupMl = o.getInt("cupMl"),
            timeLabel = o.getString("timeLabel"),
            hasBloom = o.getBoolean("hasBloom"),
            steps = steps,
            isCustom = false,
            youtubeUrl = youtubeUrl,
        )
    }

    private fun stringRes(key: String): Int = when (key) {
        "equip_v60_name" -> R.string.equip_v60_name
        "equip_v60_tag" -> R.string.equip_v60_tag
        "equip_chemex_name" -> R.string.equip_chemex_name
        "equip_chemex_tag" -> R.string.equip_chemex_tag
        "equip_kalita_name" -> R.string.equip_kalita_name
        "equip_kalita_tag" -> R.string.equip_kalita_tag
        "equip_frenchpress_name" -> R.string.equip_frenchpress_name
        "equip_frenchpress_tag" -> R.string.equip_frenchpress_tag
        "equip_aeropress_name" -> R.string.equip_aeropress_name
        "equip_aeropress_tag" -> R.string.equip_aeropress_tag
        "equip_moka_name" -> R.string.equip_moka_name
        "equip_moka_tag" -> R.string.equip_moka_tag
        "equip_turkish_name" -> R.string.equip_turkish_name
        "equip_turkish_tag" -> R.string.equip_turkish_tag
        "equip_coldbrew_name" -> R.string.equip_coldbrew_name
        "equip_coldbrew_tag" -> R.string.equip_coldbrew_tag
        "equip_siphon_name" -> R.string.equip_siphon_name
        "equip_siphon_tag" -> R.string.equip_siphon_tag
        "equip_espresso_name" -> R.string.equip_espresso_name
        "equip_espresso_tag" -> R.string.equip_espresso_tag
        "equip_clever_name" -> R.string.equip_clever_name
        "equip_clever_tag" -> R.string.equip_clever_tag
        "equip_switch_name" -> R.string.equip_switch_name
        "equip_switch_tag" -> R.string.equip_switch_tag
        "equip_origami_name" -> R.string.equip_origami_name
        "equip_origami_tag" -> R.string.equip_origami_tag
        "equip_april_name" -> R.string.equip_april_name
        "equip_april_tag" -> R.string.equip_april_tag
        "equip_stagg_name" -> R.string.equip_stagg_name
        "equip_stagg_tag" -> R.string.equip_stagg_tag
        "equip_timemore_name" -> R.string.equip_timemore_name
        "equip_timemore_tag" -> R.string.equip_timemore_tag
        "equip_beehouse_name" -> R.string.equip_beehouse_name
        "equip_beehouse_tag" -> R.string.equip_beehouse_tag
        "equip_phin_name" -> R.string.equip_phin_name
        "equip_phin_tag" -> R.string.equip_phin_tag
        "equip_colddrip_name" -> R.string.equip_colddrip_name
        "equip_colddrip_tag" -> R.string.equip_colddrip_tag
        "equip_percolator_name" -> R.string.equip_percolator_name
        "equip_percolator_tag" -> R.string.equip_percolator_tag
        "equip_batchbrew_name" -> R.string.equip_batchbrew_name
        "equip_batchbrew_tag" -> R.string.equip_batchbrew_tag
        "equip_napoletana_name" -> R.string.equip_napoletana_name
        "equip_napoletana_tag" -> R.string.equip_napoletana_tag
        "equip_cafec_name" -> R.string.equip_cafec_name
        "equip_cafec_tag" -> R.string.equip_cafec_tag
        "equip_ibrik_name" -> R.string.equip_ibrik_name
        "equip_ibrik_tag" -> R.string.equip_ibrik_tag
        "step_bloom" -> R.string.step_bloom
        "step_pour" -> R.string.step_pour
        "step_drawdown" -> R.string.step_drawdown
        "step_steep" -> R.string.step_steep
        "step_swirl" -> R.string.step_swirl
        "step_stir" -> R.string.step_stir
        "step_press" -> R.string.step_press
        "step_serve" -> R.string.step_serve
        "step_add_coffee" -> R.string.step_add_coffee
        "step_heat" -> R.string.step_heat
        "step_remove_heat" -> R.string.step_remove_heat
        "step_skim" -> R.string.step_skim
        "step_wait" -> R.string.step_wait
        "step_rinse" -> R.string.step_rinse
        "step_open_valve" -> R.string.step_open_valve
        "step_flip" -> R.string.step_flip
        else -> 0
    }
}
