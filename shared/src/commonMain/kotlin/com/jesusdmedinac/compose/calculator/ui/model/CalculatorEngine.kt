package com.jesusdmedinac.compose.calculator.ui.model

import com.github.keelar.exprk.ExpressionException
import com.github.keelar.exprk.Expressions
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorEngine {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onKeypadClicked(keypad: Keypad) {
        when (keypad.type) {
            KeypadType.NUMBER,
            KeypadType.OPERATOR,
            KeypadType.DECIMAL,
            -> numberOperatorOrDecimalClicked(keypad)

            KeypadType.CLEAR -> clearClicked()
            KeypadType.EQUALS -> equalsClicked()
            KeypadType.UNDO -> undoClicked()
        }
    }

    private fun numberOperatorOrDecimalClicked(keypad: Keypad) {
        _state.update {
            val displayedCalculation = with(it.displayedCalculation) {
                this + when {
                    keypad == Keypad.PARENTHESIS &&
                        hasOpenedParenthesis() &&
                        lastCharIsNumber() -> ")"

                    keypad == Keypad.PARENTHESIS &&
                        hasOpenedParenthesis() &&
                        !lastCharIsNumber() -> "("

                    keypad == Keypad.PARENTHESIS &&
                        !hasOpenedParenthesis() -> "("

                    else -> keypad.label
                }
            }
            val result = calculateResult(displayedCalculation)
            it.copy(
                displayedCalculation = displayedCalculation,
                displayedResult = result.toStringExpanded(),
                operation = keypad.operation,
            )
        }
    }

    private fun String.hasOpenedParenthesis() = count { it == '(' } > count { it == ')' }

    private fun String.lastCharIsNumber() = lastOrNull()?.isDigit() == true

    private fun clearClicked() {
        _state.update { CalculatorState(lastCalculation = it.lastCalculation) }
    }

    private fun calculateResult(displayedExpression: String): BigDecimal = run {
        val expression = Keypad.values().fold(displayedExpression) { acc, keypad ->
            if (keypad.operation == Operation.NONE) acc else acc.replace(keypad.label, keypad.value)
        }
        if (expression.isEmpty()) {
            return@run BigDecimal.ZERO
        }
        var useExpression = expression
        if (expression.endsWith(".")) {
            useExpression = expression.dropLast(1)
            useExpression += "0."
        }
        if (expression.endsWith("/") ||
            expression.endsWith("*") ||
            expression.endsWith("+") ||
            expression.endsWith("-") ||
            expression.endsWith("%")
        ) {
            useExpression = expression.dropLast(1)
        }
        try {
            Expressions()
                .setPrecision(10)
                .setRoundingMode(RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                .eval(useExpression)
        } catch (e: ExpressionException) {
            if (e.message?.startsWith("Expected end of expression") == true) {
                useExpression += ")"
                Expressions()
                    .setPrecision(10)
                    .setRoundingMode(RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                    .eval(useExpression)
            } else {
                BigDecimal.ZERO
            }
        }
    }

    private fun equalsClicked() {
        _state.update {
            if (it.displayedCalculation.isEmpty()) {
                return@update it
            }
            val result = calculateResult(it.displayedCalculation).toStringExpanded()

            it.copy(
                displayedCalculation = result,
                displayedResult = "",
                operation = Operation.NONE,
                lastCalculation = "${it.displayedCalculation} = $result",
            )
        }
    }

    private fun undoClicked() {
        _state.update {
            if (it.displayedCalculation.isEmpty()) {
                return@update it
            }
            val displayedCalculation = it.displayedCalculation.dropLast(1)
            val result = calculateResult(displayedCalculation)
            it.copy(
                displayedCalculation = displayedCalculation,
                displayedResult = result.toStringExpanded(),
                operation = Operation.NONE,
            )
        }
    }
}

data class CalculatorState(
    val displayedCalculation: String = "",
    val displayedResult: String = "",
    val operation: Operation = Operation.NONE,
    val lastCalculation: String = "",
)

enum class Operation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    EQUALS,
    NONE,
}
