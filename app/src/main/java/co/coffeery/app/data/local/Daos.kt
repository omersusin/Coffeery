package co.coffeery.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface CustomEquipmentDao {
    @Query("SELECT * FROM custom_equipment ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CustomEquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: CustomEquipmentEntity)

    @Query("DELETE FROM custom_equipment WHERE id = :id")
    suspend fun deleteById(id: String)
}
