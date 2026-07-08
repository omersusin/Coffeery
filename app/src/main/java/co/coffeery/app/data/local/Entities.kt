package co.coffeery.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A saved brew recipe (favorite). Local-first: no account, no network. */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val equipmentId: String,
    val strength: Float,
    val roast: String,          // RoastLevel.name
    val inputByCups: Boolean,
    val cups: Int,
    val waterMl: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * User-created custom gear. Steps are synthesized from [category] at load time,
 * so only the tuning parameters are persisted.
 */
@Entity(tableName = "custom_equipment")
data class CustomEquipmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,       // BrewCategory.name
    val ratioMin: Double,
    val ratioMax: Double,
    val ratioDefault: Double,
    val tempMode: String,       // TempMode.name
    val tempMin: Int,
    val tempMax: Int,
    val grind: String,          // Grind.name
    val cupMl: Int,
    val hasBloom: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)
