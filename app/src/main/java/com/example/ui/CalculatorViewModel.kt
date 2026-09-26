package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.AppDatabase
import com.example.data.CalculationEntity
import com.example.data.CalculatorRepository
import com.example.data.SavedSolutionEntity
import com.example.math.EvaluationResult
import com.example.math.MathEvaluator
import com.example.model.ActiveDialog
import com.example.model.AiSolverUiState
import com.example.model.CalculatorUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculatorRepository
    private val aiService = GeminiAiService()

    init {
        val db = AppDatabase.getInstance(application)
        repository = CalculatorRepository(db.calculatorDao())
    }

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _aiState = MutableStateFlow(AiSolverUiState())
    val aiState: StateFlow<AiSolverUiState> = _aiState.asStateFlow()

    val history: StateFlow<List<CalculationEntity>> = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSolutions: StateFlow<List<SavedSolutionEntity>> = repository.getSavedSolutions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onInput(token: String) {
        val current = _uiState.value.expression
        val updated = current + token
        _uiState.update {
            it.copy(
                expression = updated,
                errorMessage = null
            )
        }
        updatePreview(updated, _uiState.value.isDegreeMode)
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                previewResult = "",
                errorMessage = null
            )
        }
    }

    fun onBackspace() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            // If deleting a multi-char function e.g. "sin(", delete whole word
            val updated = when {
                current.endsWith("asin(") || current.endsWith("acos(") || current.endsWith("atan(") -> current.dropLast(5)
                current.endsWith("sinh(") || current.endsWith("cosh(") || current.endsWith("tanh(") -> current.dropLast(5)
                current.endsWith("sin(") || current.endsWith("cos(") || current.endsWith("tan(") ||
                current.endsWith("log(") || current.endsWith("log2(") -> current.dropLast(4)
                current.endsWith("sqrt(") || current.endsWith("cbrt(") -> current.dropLast(5)
                current.endsWith("ln(") -> current.dropLast(3)
                else -> current.dropLast(1)
            }
            _uiState.update { it.copy(expression = updated, errorMessage = null) }
            updatePreview(updated, _uiState.value.isDegreeMode)
        }
    }

    fun onEvaluate() {
        val expr = _uiState.value.expression.trim()
        if (expr.isEmpty()) return

        // Easter egg / covert cheat trigger: typing 8888 or 007
        if (expr == "8888" || expr == "7777" || expr == "007") {
            _uiState.update {
                it.copy(
                    isStealthHudVisible = true,
                    activeDialog = ActiveDialog.AI_AGENT,
                    displayResult = "0"
                )
            }
            return
        }

        when (val eval = MathEvaluator.evaluate(expr, _uiState.value.isDegreeMode)) {
            is EvaluationResult.Success -> {
                val formatted = eval.formatted
                _uiState.update {
                    it.copy(
                        displayResult = formatted,
                        previewResult = "",
                        stealthPeekAnswer = formatted,
                        errorMessage = null
                    )
                }
                viewModelScope.launch {
                    repository.saveCalculation(expr, formatted)
                }
            }
            is EvaluationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = eval.message
                    )
                }
            }
        }
    }

    private fun updatePreview(expr: String, isDegree: Boolean) {
        if (expr.isBlank()) {
            _uiState.update { it.copy(previewResult = "") }
            return
        }
        val eval = MathEvaluator.evaluate(expr, isDegree)
        if (eval is EvaluationResult.Success) {
            _uiState.update { it.copy(previewResult = eval.formatted) }
        } else {
            _uiState.update { it.copy(previewResult = "") }
        }
    }

    fun toggleAngleMode() {
        val newMode = !_uiState.value.isDegreeMode
        _uiState.update { it.copy(isDegreeMode = newMode) }
        updatePreview(_uiState.value.expression, newMode)
    }

    fun toggleSecondMode() {
        _uiState.update { it.copy(isSecondMode = !it.isSecondMode) }
    }

    fun toggleStealthHud() {
        _uiState.update { it.copy(isStealthHudVisible = !it.isStealthHudVisible) }
    }

    // Memory Functions
    fun memoryAdd() {
        val currentVal = getCurrentNumericValue()
        val newMem = _uiState.value.memoryValue + currentVal
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
    }

    fun memorySubtract() {
        val currentVal = getCurrentNumericValue()
        val newMem = _uiState.value.memoryValue - currentVal
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
    }

    fun memoryRecall() {
        if (_uiState.value.hasMemory) {
            val formatted = MathEvaluator.formatResult(_uiState.value.memoryValue)
            onInput(formatted)
        }
    }

    fun memoryClear() {
        _uiState.update { it.copy(memoryValue = 0.0, hasMemory = false) }
    }

    private fun getCurrentNumericValue(): Double {
        val res = _uiState.value.displayResult.toDoubleOrNull()
        if (res != null) return res
        val eval = MathEvaluator.evaluate(_uiState.value.expression, _uiState.value.isDegreeMode)
        return if (eval is EvaluationResult.Success) eval.value else 0.0
    }

    fun openDialog(dialog: ActiveDialog) {
        if (dialog == ActiveDialog.AI_AGENT && _aiState.value.query.isBlank()) {
            // If calculator has an expression or result, prepopulate it for convenience!
            val expr = _uiState.value.expression
            if (expr.isNotBlank()) {
                _aiState.update { it.copy(query = expr) }
            }
        }
        _uiState.update { it.copy(activeDialog = dialog) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.NONE) }
    }

    fun useHistoryItem(item: CalculationEntity) {
        _uiState.update {
            it.copy(
                expression = item.expression,
                displayResult = item.result,
                previewResult = "",
                activeDialog = ActiveDialog.NONE
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // AI Solver
    fun updateAiQuery(query: String) {
        _aiState.update { it.copy(query = query, errorMessage = null) }
    }

    fun solveWithAi() {
        val query = _aiState.value.query.trim()
        if (query.isBlank()) return

        _aiState.update { it.copy(isLoading = true, errorMessage = null, isSaved = false) }

        viewModelScope.launch {
            val result = aiService.solveProblem(query)
            result.onSuccess { solverResult ->
                _aiState.update {
                    it.copy(
                        isLoading = false,
                        result = solverResult,
                        errorMessage = null
                    )
                }
                // Also update stealth peek answer so user can discreetly check it!
                _uiState.update {
                    it.copy(
                        stealthPeekAnswer = solverResult.stealthHint
                    )
                }
            }.onFailure { error ->
                _aiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to solve problem"
                    )
                }
            }
        }
    }

    fun toggleAiCamouflage() {
        _aiState.update { it.copy(isCamouflageMode = !it.isCamouflageMode) }
    }

    fun saveCurrentSolution() {
        val res = _aiState.value.result ?: return
        viewModelScope.launch {
            repository.saveSolution(
                SavedSolutionEntity(
                    problem = _aiState.value.query,
                    quickAnswer = res.quickAnswer,
                    stepByStep = res.steps,
                    formulasUsed = res.formulasUsed,
                    category = "Exam/Study Solution"
                )
            )
            _aiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteSavedSolution(id: Long) {
        viewModelScope.launch {
            repository.deleteSolution(id)
        }
    }
}
