package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val dao: CalculatorDao) {

    fun getHistory(): Flow<List<CalculationEntity>> = dao.getAllHistory()

    suspend fun saveCalculation(expression: String, result: String): Long {
        return dao.insertHistory(CalculationEntity(expression = expression, result = result))
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }

    suspend fun deleteHistory(id: Long) {
        dao.deleteHistoryById(id)
    }

    fun getSavedSolutions(): Flow<List<SavedSolutionEntity>> = dao.getAllSavedSolutions()

    suspend fun saveSolution(solution: SavedSolutionEntity): Long {
        return dao.insertSavedSolution(solution)
    }

    suspend fun deleteSolution(id: Long) {
        dao.deleteSavedSolutionById(id)
    }
}
