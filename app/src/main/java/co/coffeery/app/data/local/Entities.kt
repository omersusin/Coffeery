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

/** Single-row app settings. Primary key is always "app". */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: String = "app",
    val themeMode: String = "system",
    val paletteKey: String = "TERRACOTTA",
    val hasCompletedOnboarding: Boolean = false,
    val language: String = "en",
    val timerPip: Boolean = true,
    val timerBackground: Boolean = true,
    val timerSound: Boolean = true,
    val timerVibrate: Boolean = true,
    val timerShowNext: Boolean = true,
    val timerMergeWeight: Boolean = false,
    val notificationsBrewComplete: Boolean = true,
    val notificationsStepChange: Boolean = false,
    val completedChapters: String = "",
    val ratioMode: Boolean = false,
    val manualRatio: Double = 16.0,
    val bloomDurationSec: Int = 40,
    val pourDurationSec: Int = 45,
    val steepDurationSec: Int = 240,
    val drawdownDurationSec: Int = 55,
    val timerAutoAdvance: Boolean = false,
    val timerDisplayMode: String = "countdown",
    val temperatureUnit: String = "C",
)

/** A completed brew session. Immutable after creation — history, not a preset. */
@Entity(tableName = "brew_logs")
data class BrewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val equipmentId: String,
    val equipmentName: String,
    val strength: Float,
    val roast: String,
    val ratioDenominator: Double,
    val coffeeGrams: Double,
    val waterMl: Int,
    val grind: String,
    val customGrindSize: String = "",
    val tempCelsius: Int,
    val totalDurationSec: Int,
    val rating: Int = 0,
    val tastingNotes: String = "",
    val flavorTags: String = "", // comma-separated flavor tags
    val beanId: Long? = null,
    val beanName: String = "",
    val photoUri: String? = null,
)

/** A bag of coffee beans. Local-first, no account needed. */
@Entity(tableName = "beans")
data class BeanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val origin: String = "",
    val roaster: String = "",
    val roastDate: Long? = null,
    val roastLevel: String = "MEDIUM",
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val processMethod: String = "",
    val varietal: String = "",
    val altitude: String = "",
    val flavorNotes: String = "",
    val scaScore: Float? = null,
    val purchaseDate: Long? = null,
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
