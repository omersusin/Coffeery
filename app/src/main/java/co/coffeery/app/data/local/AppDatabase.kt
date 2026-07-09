package co.coffeery.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecipeEntity::class, CustomEquipmentEntity::class, SettingsEntity::class, BrewLogEntity::class, BeanEntity::class],
    version = 6,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun customEquipmentDao(): CustomEquipmentDao
    abstract fun settingsDao(): SettingsDao
    abstract fun brewLogDao(): BrewLogDao
    abstract fun beanDao(): BeanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN completedChapters TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE settings ADD COLUMN ratioMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN manualRatio REAL NOT NULL DEFAULT 16.0")
                db.execSQL("ALTER TABLE brew_logs ADD COLUMN beanName TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN bloomDurationSec INTEGER NOT NULL DEFAULT 40")
                db.execSQL("ALTER TABLE settings ADD COLUMN pourDurationSec INTEGER NOT NULL DEFAULT 45")
                db.execSQL("ALTER TABLE settings ADD COLUMN steepDurationSec INTEGER NOT NULL DEFAULT 240")
                db.execSQL("ALTER TABLE settings ADD COLUMN drawdownDurationSec INTEGER NOT NULL DEFAULT 55")
                db.execSQL("ALTER TABLE settings ADD COLUMN timerAutoAdvance INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN timerDisplayMode TEXT NOT NULL DEFAULT 'countdown'")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coffeery.db",
                ).addMigrations(MIGRATION_4_5, MIGRATION_5_6).build().also { INSTANCE = it }
            }
    }
}
