package com.jesusdmedinac.compose.calculator.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.keelar.exprk.ExpressionException
import com.github.keelar.exprk.Expressions
import com.ionspin.kotlin.bignum.decimal.BigDecimal

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
            KeypadType.NEGATE -> TODO()
            KeypadType.PERCENT -> TODO()
        }
    }

    private fun numberOperatorOrDecimalClicked(keypad: Keypad) {
        state.update {
            val displayedCalculation = it.displayedCalculation + keypad.label
            val result = calculateResult(displayedCalculation)
            it.copy(
                displayedCalculation = displayedCalculation,
                displayedResult = when {
                    result % 1 == BigDecimal.fromInt(0) ->
                        result.toString().dropLast(2)

                    else -> result.toString()
                },
            )
        }
    }

    private fun clearClicked() {
        state.update { CalculatorState() }
    }

    private fun calculateResult(expression: String): BigDecimal =
        try {
            var useExpression = expression
            if (expression.endsWith(".")) {
                useExpression = expression.dropLast(1)
                useExpression += "0."
            }
            Expressions().eval(useExpression)
        } catch (e: ExpressionException) {
            Expressions().eval(expression.dropLast(1))
        }

    private fun equalsClicked() {
        state.update {
            val result = calculateResult(it.displayedCalculation)

            it.copy(
                displayedResult = when {
                    result % 1 == BigDecimal.fromInt(0) ->
                        result.toString().drop(1)

                    else -> result.toString()
                },
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
