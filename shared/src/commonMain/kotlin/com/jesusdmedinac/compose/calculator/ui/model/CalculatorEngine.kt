package com.jesusdmedinac.compose.calculator.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.keelar.exprk.ExpressionException
import com.github.keelar.exprk.Expressions
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode

class CalculatorEngine {
    var state: CalculatorState by mutableStateOf(CalculatorState())
        private set

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
        state.update {
            println("it.displayedCalculation: ${it.displayedCalculation}")
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
            println("displayedCalculation: $displayedCalculation")
            val result = calculateResult(displayedCalculation)
            it.copy(
                displayedCalculation = displayedCalculation,
                displayedResult = result.toStringExpanded(),
            )
        }
    }

    private fun String.hasOpenedParenthesis() = count { it == '(' } > count { it == ')' }

    private fun String.lastCharIsNumber() = lastOrNull()?.isDigit() == true

    private fun clearClicked() {
        state.update { CalculatorState() }
    }

    private fun calculateResult(expression: String): BigDecimal = run {
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
        state.update {
            val result = calculateResult(it.displayedCalculation)

            it.copy(
                displayedCalculation = result.toStringExpanded(),
                displayedResult = "",
            )
        }
    }

    private fun undoClicked() {
        state.update {
            if (it.displayedCalculation.isEmpty()) {
                return@update it
            }
            val displayedCalculation = it.displayedCalculation.dropLast(1)
            val result = calculateResult(displayedCalculation)
            it.copy(
                displayedCalculation = displayedCalculation,
                displayedResult = result.toStringExpanded(),
            )
        }
    }

    private fun CalculatorState.update(
        reducer: (CalculatorState) -> CalculatorState,
    ) {
        state = reducer(this)
    }
}

data class CalculatorState(
    val displayedCalculation: String = "",
    val displayedResult: String = "",
    val operation: Operation = Operation.NONE,
)

enum class Operation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    EQUALS,
    NONE,
}
