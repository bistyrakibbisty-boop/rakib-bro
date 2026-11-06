package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calculation: CalculationEntity): Long

    @Update
    suspend fun update(calculation: CalculationEntity)

    @Delete
    suspend fun delete(calculation: CalculationEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()

    @Query("UPDATE calculation_history SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
