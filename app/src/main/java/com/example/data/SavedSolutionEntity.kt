package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_solutions")
data class SavedSolutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val problem: String,
    val quickAnswer: String,
    val stepByStep: String,
    val formulasUsed: String,
    val category: String, // Algebra, Calculus, Physics, General, etc.
    val timestamp: Long = System.currentTimeMillis()
)
