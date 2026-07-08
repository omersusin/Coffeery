package co.coffeery.app.data.local

import android.content.Context
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
            result.add(parse(context, arr.getJSONObject(i)))
        }
        return result
    }

    private fun parse(context: Context, o: JSONObject): Equipment {
        val steps = ArrayList<BrewStepDef>()
        val stepArr = o.getJSONArray("steps")
        for (j in 0 until stepArr.length()) {
            val s = stepArr.getJSONObject(j)
            steps.add(
                BrewStepDef(
                    titleRes = stringRes(context, s.getString("titleKey")),
                    kind = StepKind.fromKey(s.optString("kind")),
                    durationSec = s.getInt("durationSec"),
                    waterTargetPct = s.getDouble("waterTargetPct").toFloat(),
                )
            )
        }
        return Equipment(
            id = o.getString("id"),
            nameRes = stringRes(context, o.getString("nameKey")),
            tagRes = stringRes(context, o.getString("tagKey")),
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
        )
    }

    private fun stringRes(context: Context, key: String): Int {
        val id = context.resources.getIdentifier(key, "string", context.packageName)
        require(id != 0) { "Missing string resource for key '$key'" }
        return id
    }
}
