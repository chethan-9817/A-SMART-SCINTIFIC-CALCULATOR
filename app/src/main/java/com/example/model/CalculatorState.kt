package com.example.model

enum class ActiveDialog {
    NONE,
    AI_AGENT,
    HTML_FORMULAS,
    HISTORY,
    FORMULA_VAULT
}

data class CalculatorUiState(
    val expression: String = "",
    val cursorPosition: Int = 0,
    val displayResult: String = "0",
    val previewResult: String = "",
    val isDegreeMode: Boolean = true,
    val isSecondMode: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val isStealthHudVisible: Boolean = false,
    val stealthPeekAnswer: String = "",
    val errorMessage: String? = null,
    val activeDialog: ActiveDialog = ActiveDialog.NONE
)

data class AiSolverUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val result: com.example.ai.SolverResult? = null,
    val errorMessage: String? = null,
    val isCamouflageMode: Boolean = false,
    val isSaved: Boolean = false
)
