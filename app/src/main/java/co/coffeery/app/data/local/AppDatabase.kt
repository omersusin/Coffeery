package co.coffeery.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecipeEntity::class, CustomEquipmentEntity::class, SettingsEntity::class, BrewLogEntity::class, BeanEntity::class],
    version = 9,
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

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN temperatureUnit TEXT NOT NULL DEFAULT 'C'")
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

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE beans ADD COLUMN processMethod TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE beans ADD COLUMN varietal TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE beans ADD COLUMN altitude TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE beans ADD COLUMN flavorNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE beans ADD COLUMN scaScore REAL")
                db.execSQL("ALTER TABLE beans ADD COLUMN purchaseDate INTEGER")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE brew_logs ADD COLUMN photoUri TEXT")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coffeery.db",
                ).addMigrations(
                    MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                ).build().also { INSTANCE = it }
            }
    }
}
