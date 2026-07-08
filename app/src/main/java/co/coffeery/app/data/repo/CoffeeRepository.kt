package co.coffeery.app.data.repo

import android.content.Context
import co.coffeery.app.R
import co.coffeery.app.data.local.AppDatabase
import co.coffeery.app.data.local.CustomEquipmentEntity
import co.coffeery.app.data.local.RecipeEntity
import co.coffeery.app.data.local.PresetLoader
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.model.BrewStepDef
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.data.model.Grind
import co.coffeery.app.data.model.StepKind
import co.coffeery.app.data.model.TempMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Suggested tuning defaults for a category when creating custom gear. */
data class CategoryDefaults(
    val ratioMin: Double,
    val ratioMax: Double,
    val ratioDefault: Double,
    val tempMode: TempMode,
    val tempMin: Int,
    val tempMax: Int,
    val grind: Grind,
    val cupMl: Int,
    val hasBloom: Boolean,
)

class CoffeeRepository(context: Context, private val db: AppDatabase) {

    private val builtIns: List<Equipment> = PresetLoader.loadBuiltIns(context)

    val recipes: Flow<List<RecipeEntity>> = db.recipeDao().observeAll()

    private val customEquipment: Flow<List<Equipment>> =
        db.customEquipmentDao().observeAll().map { list -> list.map { it.toEquipment() } }

    /** Built-ins first, then user gear. */
    val allEquipment: Flow<List<Equipment>> =
        customEquipment.map { custom -> builtIns + custom }

    fun builtInEquipment(): List<Equipment> = builtIns

    suspend fun saveRecipe(recipe: RecipeEntity) = db.recipeDao().insert(recipe)

    suspend fun deleteRecipe(id: Long) = db.recipeDao().deleteById(id)

    suspend fun addCustomEquipment(entity: CustomEquipmentEntity) =
        db.customEquipmentDao().insert(entity)

    suspend fun deleteCustomEquipment(id: String) =
        db.customEquipmentDao().deleteById(id)

    private fun CustomEquipmentEntity.toEquipment(): Equipment {
        val cat = BrewCategory.fromKey(category)
        return Equipment(
            id = id,
            nameRes = 0,
            tagRes = 0,
            category = cat,
            ratioMin = ratioMin,
            ratioMax = ratioMax,
            ratioDefault = ratioDefault,
            tempMode = TempMode.fromKey(tempMode),
            tempMin = tempMin,
            tempMax = tempMax,
            grind = Grind.fromKey(grind),
            cupMl = cupMl,
            timeLabel = "",
            hasBloom = hasBloom,
            steps = defaultStepsFor(cat, hasBloom),
            isCustom = true,
            customName = name,
        )
    }

    companion object {
        fun defaultsFor(category: BrewCategory): CategoryDefaults = when (category) {
            BrewCategory.POUR_OVER -> CategoryDefaults(
                15.0, 17.0, 16.5, TempMode.RANGE, 90, 96, Grind.MEDIUM, 250, true,
            )
            BrewCategory.IMMERSION -> CategoryDefaults(
                14.0, 17.0, 15.0, TempMode.RANGE, 92, 96, Grind.COARSE, 250, false,
            )
            BrewCategory.PRESSURE -> CategoryDefaults(
                8.0, 12.0, 10.0, TempMode.RANGE, 88, 95, Grind.MED_FINE, 60, false,
            )
            BrewCategory.OTHER -> CategoryDefaults(
                10.0, 16.0, 13.0, TempMode.RANGE, 90, 96, Grind.MEDIUM, 120, false,
            )
        }

        /** Generic timed routine synthesized for user-created gear. */
        fun defaultStepsFor(category: BrewCategory, hasBloom: Boolean): List<BrewStepDef> {
            val steps = ArrayList<BrewStepDef>()
            when (category) {
                BrewCategory.POUR_OVER -> {
                    if (hasBloom) steps += BrewStepDef(R.string.step_bloom, StepKind.BLOOM, 40, 0.18f)
                    steps += BrewStepDef(R.string.step_pour, StepKind.POUR, 45, 0.6f)
                    steps += BrewStepDef(R.string.step_pour, StepKind.POUR, 45, 1.0f)
                    steps += BrewStepDef(R.string.step_drawdown, StepKind.DRAWDOWN, 55, -1f)
                }
                BrewCategory.IMMERSION -> {
                    if (hasBloom) steps += BrewStepDef(R.string.step_bloom, StepKind.BLOOM, 30, 0.13f)
                    steps += BrewStepDef(R.string.step_add_coffee, StepKind.POUR, 15, 1.0f)
                    steps += BrewStepDef(R.string.step_steep, StepKind.STEEP, 240, -1f)
                    steps += BrewStepDef(R.string.step_serve, StepKind.SERVE, 10, -1f)
                }
                BrewCategory.PRESSURE -> {
                    steps += BrewStepDef(R.string.step_add_coffee, StepKind.POUR, 15, 1.0f)
                    steps += BrewStepDef(R.string.step_steep, StepKind.STEEP, 120, -1f)
                    steps += BrewStepDef(R.string.step_press, StepKind.PRESS, 30, -1f)
                }
                BrewCategory.OTHER -> {
                    steps += BrewStepDef(R.string.step_add_coffee, StepKind.STIR, 20, 1.0f)
                    steps += BrewStepDef(R.string.step_steep, StepKind.STEEP, 120, -1f)
                    steps += BrewStepDef(R.string.step_serve, StepKind.SERVE, 20, -1f)
                }
            }
            return steps
        }
    }
}
