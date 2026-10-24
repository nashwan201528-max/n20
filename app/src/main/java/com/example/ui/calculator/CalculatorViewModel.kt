package com.example.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.HistoryRepository
import com.example.domain.calculator.EvaluationResult
import com.example.domain.calculator.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val livePreview: String? = null,
    val committedResult: String? = null,
    val errorMessage: String? = null,
    val isScientificExpanded: Boolean = false,
    val isDegreeMode: Boolean = true,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false
)

class CalculatorViewModel(
    private val historyRepository: HistoryRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun toggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleAngleMode() {
        _uiState.update {
            val nextDeg = !it.isDegreeMode
            val nextLive = ExpressionEvaluator.evaluateLive(it.expression, nextDeg)
            it.copy(isDegreeMode = nextDeg, livePreview = nextLive)
        }
    }

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val newExpr = if (state.committedResult != null && state.expression == state.committedResult) {
                // If user just pressed = and starts typing a new number, reset expression
                digit
            } else {
                state.expression + digit
            }
            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onOperator(op: String) {
        _uiState.update { state ->
            val expr = state.expression.trim()
            val newExpr = when {
                expr.isEmpty() -> {
                    if (op == "-") "-" else ""
                }
                expr.last() in listOf('+', '-', '×', '÷', '*', '/', '^') -> {
                    // Replace previous operator
                    expr.dropLast(1) + op
                }
                else -> {
                    expr + op
                }
            }
            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            val expr = state.expression
            val tokens = expr.split('+', '-', '×', '÷', '*', '/', '(', ')', '^')
            val currentChunk = tokens.lastOrNull() ?: ""

            val newExpr = if (currentChunk.contains('.')) {
                expr // Ignore second decimal in same number
            } else if (currentChunk.isEmpty() || currentChunk.endsWith('(')) {
                expr + "0."
            } else {
                expr + "."
            }

            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onParentheses() {
        _uiState.update { state ->
            val expr = state.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val nextChar = if (expr.isEmpty() || expr.last() in listOf('+', '-', '×', '÷', '*', '/', '^', '(')) {
                "("
            } else if (openCount > closeCount) {
                ")"
            } else {
                "×("
            }

            val newExpr = expr + nextChar
            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onFunction(func: String) {
        _uiState.update { state ->
            val expr = state.expression
            val prefix = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) "×" else ""
            val newExpr = "$expr$prefix$func("
            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onConstant(constant: String) {
        _uiState.update { state ->
            val expr = state.expression
            val prefix = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) "×" else ""
            val newExpr = "$expr$prefix$constant"
            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.expression.isEmpty()) return@update state
            val newExpr = state.expression.dropLast(1)
            val live = ExpressionEvaluator.evaluateLive(newExpr, state.isDegreeMode)
            state.copy(
                expression = newExpr,
                livePreview = live,
                errorMessage = null,
                committedResult = null
            )
        }
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                livePreview = null,
                committedResult = null,
                errorMessage = null
            )
        }
    }

    fun onEquals() {
        val state = _uiState.value
        if (state.expression.isBlank()) return

        when (val res = ExpressionEvaluator.evaluate(state.expression, state.isDegreeMode)) {
            is EvaluationResult.Success -> {
                val formatted = res.formatted
                _uiState.update {
                    it.copy(
                        committedResult = formatted,
                        expression = formatted,
                        livePreview = null,
                        errorMessage = null
                    )
                }

                // Persist calculation in Room history
                viewModelScope.launch {
                    val modeTag = if (state.isScientificExpanded) "SCIENTIFIC" else "STANDARD"
                    historyRepository?.saveRecord(
                        CalculationRecord(
                            type = modeTag,
                            title = if (state.isScientificExpanded) "Scientific Calc" else "Standard Calc",
                            expression = state.expression,
                            result = formatted,
                            secondaryDetails = if (state.isDegreeMode) "Angle: DEG" else "Angle: RAD"
                        )
                    )
                }
            }
            is EvaluationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = res.message,
                        livePreview = null
                    )
                }
            }
        }
    }

    fun onMemoryAdd() {
        val state = _uiState.value
        val num = state.committedResult?.replace(",", "")?.toDoubleOrNull()
            ?: state.expression.replace(",", "").toDoubleOrNull()
            ?: return

        val newMem = state.memoryValue + num
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
    }

    fun onMemorySubtract() {
        val state = _uiState.value
        val num = state.committedResult?.replace(",", "")?.toDoubleOrNull()
            ?: state.expression.replace(",", "").toDoubleOrNull()
            ?: return

        val newMem = state.memoryValue - num
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
    }

    fun onMemoryRecall() {
        val state = _uiState.value
        if (!state.hasMemory) return
        val memFormatted = ExpressionEvaluator.formatNumber(state.memoryValue)
        _uiState.update {
            val newExpr = if (it.expression.isEmpty() || it.expression.last() in listOf('+', '-', '×', '÷', '*', '/', '^', '(')) {
                it.expression + memFormatted
            } else {
                it.expression + "×" + memFormatted
            }
            val live = ExpressionEvaluator.evaluateLive(newExpr, it.isDegreeMode)
            it.copy(expression = newExpr, livePreview = live, committedResult = null)
        }
    }

    fun onMemoryClear() {
        _uiState.update { it.copy(memoryValue = 0.0, hasMemory = false) }
    }

    /**
     * Re-uses a past calculation formula or result into the active calculator display
     */
    fun onRecallPastCalculation(record: CalculationRecord, useResultOnly: Boolean = false) {
        val valueToLoad = if (useResultOnly || record.expression.isBlank()) record.result else record.expression
        _uiState.update {
            val live = ExpressionEvaluator.evaluateLive(valueToLoad, it.isDegreeMode)
            it.copy(
                expression = valueToLoad,
                livePreview = live,
                committedResult = if (useResultOnly) record.result else null,
                errorMessage = null
            )
        }
    }
}

class CalculatorViewModelFactory(
    private val historyRepository: HistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
