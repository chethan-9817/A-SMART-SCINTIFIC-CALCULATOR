package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculatorDao {

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalculationEntity): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    // Saved AI Solutions
    @Query("SELECT * FROM saved_solutions ORDER BY timestamp DESC")
    fun getAllSavedSolutions(): Flow<List<SavedSolutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSolution(solution: SavedSolutionEntity): Long

    @Delete
    suspend fun deleteSavedSolution(solution: SavedSolutionEntity)

    @Query("DELETE FROM saved_solutions WHERE id = :id")
    suspend fun deleteSavedSolutionById(id: Long)
}
