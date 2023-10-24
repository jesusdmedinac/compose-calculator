package com.jesusdmedinac.compose.calculator.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CalculatorEngine {
    var state: CalculatorState by mutableStateOf(CalculatorState())
        private set

    fun onKeypadClicked(keypad: Keypad) {
        when (keypad.type) {
            KeypadType.NUMBER -> numberClicked(keypad)
            KeypadType.OPERATOR -> operatorClicked(keypad)
            KeypadType.CLEAR -> clearClicked()
            KeypadType.EQUALS -> equalsClicked()
            KeypadType.DECIMAL -> TODO()
            KeypadType.NEGATE -> TODO()
            KeypadType.PERCENT -> TODO()
        }
    }

    private fun numberClicked(keypad: Keypad) {
        if (!state.displayingNextValue && state.previousValue != 0.0) {
            state.update {
                it.copy(
                    displayedValue = 0.0,
                    displayingNextValue = true,
                )
            }
        }
        state.update {
            it.copy(
                displayedValue = (it.displayedValue * 10) + keypad.label.toDouble(),
            )
        }
    }

    private fun operatorClicked(keypad: Keypad) {
        if (state.operation != Operation.NONE) {
            equalsClicked()
        }
        state.update {
            it.copy(
                previousValue = it.displayedValue,
                operation = when (keypad) {
                    Keypad.ADD -> Operation.ADD
                    Keypad.SUBTRACT -> Operation.SUBTRACT
                    Keypad.MULTIPLY -> Operation.MULTIPLY
                    Keypad.DIVIDE -> Operation.DIVIDE
                    else -> Operation.NONE
                },
            )
        }
    }

    private fun clearClicked() {
        if (state.displayedValue != 0.0) {
            state.update {
                it.copy(
                    displayedValue = 0.0,
                )
            }
        } else {
            state.update {
                it.copy(
                    displayedValue = 0.0,
                    previousValue = 0.0,
                    displayingNextValue = false,
                    operation = Operation.NONE,
                )
            }
        }
    }

    private fun equalsClicked() {
        state.update {
            it.copy(
                displayedValue = when (it.operation) {
                    Operation.ADD -> it.previousValue + it.displayedValue
                    Operation.SUBTRACT -> it.previousValue - it.displayedValue
                    Operation.MULTIPLY -> it.previousValue * it.displayedValue
                    Operation.DIVIDE -> it.previousValue / it.displayedValue
                    else -> it.displayedValue
                },
                previousValue = 0.0,
                displayingNextValue = false,
                operation = Operation.NONE,
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
    val displayedValue: Double = 0.0,
    val previousValue: Double = 0.0,
    val displayingNextValue: Boolean = false,
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
