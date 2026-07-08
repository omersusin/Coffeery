package co.coffeery.app.ui.screens.root

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.coffeery.app.data.local.AppDatabase
import co.coffeery.app.data.local.BrewLogEntity
import co.coffeery.app.data.local.CustomEquipmentEntity
import co.coffeery.app.data.local.RecipeEntity
import co.coffeery.app.data.local.SettingsEntity
import co.coffeery.app.data.model.BrewCategory
import co.coffeery.app.data.model.Equipment
import co.coffeery.app.data.model.RoastLevel
import co.coffeery.app.data.model.ThemeMode
import co.coffeery.app.data.repo.CoffeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** Immutable UI state for the whole app (unidirectional data flow). */
data class AppUiState(
    val tab: NavTab = NavTab.BREW,
    val route: Route = Route.Tabs,
    val equipment: List<Equipment> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
    val selectedEquipmentId: String? = null,
    val strength: Float = 0.5f,
    val roast: RoastLevel = RoastLevel.MEDIUM,
    val byCups: Boolean = true,
    val cups: Int = 2,
    val waterMl: Int = 500,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val settings: SettingsEntity = SettingsEntity(),
    val brewLogs: List<BrewLogEntity> = emptyList(),
) {
    val selectedEquipment: Equipment?
        get() = equipment.firstOrNull { it.id == selectedEquipmentId }
            ?: equipment.firstOrNull()
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CoffeeRepository(app, AppDatabase.get(app))

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.allEquipment.collect { list ->
                _state.update { s ->
                    val selected = s.selectedEquipmentId
                        ?: list.firstOrNull()?.id
                    val strength = if (s.selectedEquipmentId == null) {
                        list.firstOrNull()?.defaultStrength ?: 0.5f
                    } else s.strength
                    s.copy(equipment = list, selectedEquipmentId = selected, strength = strength)
                }
            }
        }
        viewModelScope.launch {
            repo.recipes.collect { list -> _state.update { it.copy(recipes = list) } }
        }
        viewModelScope.launch {
            repo.brewLogs.collect { list -> _state.update { it.copy(brewLogs = list) } }
        }
        viewModelScope.launch {
            repo.settings.collect { entity ->
                val s = entity ?: SettingsEntity()
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(s.language))
                _state.update {
                    it.copy(themeMode = ThemeMode.fromKey(s.themeMode), settings = s)
                }
            }
        }
    }

    // --- Settings ---
    fun setThemeMode(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
        viewModelScope.launch {
            val cur = (repo.settings.first() ?: SettingsEntity())
                .copy(themeMode = mode.name)
            repo.upsertSettings(cur)
        }
    }

    fun setLanguage(lang: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        viewModelScope.launch {
            val cur = (repo.settings.first() ?: SettingsEntity())
                .copy(language = lang)
            repo.upsertSettings(cur)
        }
    }

    fun setTimerSetting(update: (SettingsEntity) -> SettingsEntity) {
        viewModelScope.launch {
            val cur = repo.settings.first() ?: SettingsEntity()
            repo.upsertSettings(update(cur))
        }
    }

    // --- Navigation ---
    fun selectTab(tab: NavTab) = _state.update { it.copy(tab = tab, route = Route.Tabs) }
    fun openRoute(route: Route) = _state.update { it.copy(route = route) }
    fun back() = _state.update { it.copy(route = Route.Tabs) }

    // --- Brew parameters ---
    fun selectEquipment(id: String) = _state.update { s ->
        val eq = s.equipment.firstOrNull { it.id == id }
        s.copy(
            selectedEquipmentId = id,
            strength = eq?.defaultStrength ?: s.strength,
            tab = NavTab.BREW,
            route = Route.Tabs,
        )
    }

    fun setStrength(v: Float) = _state.update { it.copy(strength = v) }
    fun setRoast(r: RoastLevel) = _state.update { it.copy(roast = r) }
    fun setByCups(byCups: Boolean) = _state.update { it.copy(byCups = byCups) }
    fun setCups(c: Int) = _state.update { it.copy(cups = c.coerceIn(1, 12)) }
    fun setWater(ml: Int) = _state.update { it.copy(waterMl = ml.coerceIn(0, 4000)) }

    // --- Recipes ---
    fun saveRecipe(name: String) {
        val s = _state.value
        val eq = s.selectedEquipment ?: return
        viewModelScope.launch {
            repo.saveRecipe(
                RecipeEntity(
                    name = name.trim().ifBlank { "—" },
                    equipmentId = eq.id,
                    strength = s.strength,
                    roast = s.roast.name,
                    inputByCups = s.byCups,
                    cups = s.cups,
                    waterMl = s.waterMl,
                )
            )
        }
    }

    fun deleteRecipe(id: Long) = viewModelScope.launch { repo.deleteRecipe(id) }

    fun applyRecipe(r: RecipeEntity) = _state.update {
        it.copy(
            selectedEquipmentId = r.equipmentId,
            strength = r.strength,
            roast = runCatching { RoastLevel.valueOf(r.roast) }.getOrDefault(RoastLevel.MEDIUM),
            byCups = r.inputByCups,
            cups = r.cups,
            waterMl = r.waterMl,
            tab = NavTab.BREW,
            route = Route.Tabs,
        )
    }

    // --- Custom equipment ---
    fun addCustomEquipment(name: String, category: BrewCategory) {
        val defaults = CoffeeRepository.defaultsFor(category)
        val id = "custom_" + UUID.randomUUID().toString().take(8)
        viewModelScope.launch {
            repo.addCustomEquipment(
                CustomEquipmentEntity(
                    id = id,
                    name = name.trim(),
                    category = category.name,
                    ratioMin = defaults.ratioMin,
                    ratioMax = defaults.ratioMax,
                    ratioDefault = defaults.ratioDefault,
                    tempMode = defaults.tempMode.name,
                    tempMin = defaults.tempMin,
                    tempMax = defaults.tempMax,
                    grind = defaults.grind.name,
                    cupMl = defaults.cupMl,
                    hasBloom = defaults.hasBloom,
                )
            )
            _state.update { it.copy(route = Route.Tabs, tab = NavTab.GEAR) }
        }
    }

    fun deleteCustomEquipment(id: String) = viewModelScope.launch { repo.deleteCustomEquipment(id) }

    // --- Brew logs ---
    fun saveBrewLog(log: BrewLogEntity) = viewModelScope.launch { repo.saveBrewLog(log) }

    fun deleteBrewLog(id: Long) = viewModelScope.launch { repo.deleteBrewLog(id) }

    fun applyBrewLog(log: BrewLogEntity) = _state.update {
        it.copy(
            selectedEquipmentId = log.equipmentId,
            strength = log.strength,
            roast = runCatching { RoastLevel.valueOf(log.roast) }.getOrDefault(RoastLevel.MEDIUM),
            byCups = false,
            waterMl = log.waterMl,
            tab = NavTab.BREW,
            route = Route.Tabs,
        )
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    AppViewModel(app) as T
            }
    }
}
