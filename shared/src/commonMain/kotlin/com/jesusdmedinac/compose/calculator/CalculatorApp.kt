package com.jesusdmedinac.compose.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.jesusdmedinac.compose.calculator.ui.model.CalculatorEngine
import com.jesusdmedinac.compose.calculator.ui.model.Keypad
import com.jesusdmedinac.compose.calculator.ui.model.KeypadSize
import com.jesusdmedinac.compose.calculator.ui.model.KeypadType
import com.jesusdmedinac.compose.calculator.ui.model.Operation
import kotlin.math.roundToInt

@Composable
fun ComposeCalculatorApp() {
    val calculatorEngine by remember { mutableStateOf(CalculatorEngine()) }
    val state = calculatorEngine.state
    val displayedValue = state.displayedValue
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(2.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                val displayedValueAsString = with(displayedValue) {
                    when {
                        this % 1 == 0.0 -> {
                            roundToInt()
                        }

                        else -> this
                    }
                }.toString()
                Text(
                    text = displayedValueAsString,
                    modifier = Modifier,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.End,
                    fontSize = MaterialTheme.typography.displayLarge.fontSize.run {
                        this * 1.5f
                    },
                )
            }
            ColumnOfButtons(
                keypads = Keypad.values().toList(),
                calculatorEngine = calculatorEngine,
            )
        }
    }
}

@Composable
fun ColumnScope.ColumnOfButtons(
    keypads: List<Keypad>,
    calculatorEngine: CalculatorEngine,
) {
    keypads
        .chunked(4)
        .forEach { rowOfButtons ->
            RowOfButtons(
                modifier = Modifier.weight(1f),
                buttonValues = rowOfButtons,
                calculatorEngine = calculatorEngine,
            )
        }
}

@Composable
fun RowOfButtons(
    modifier: Modifier = Modifier,
    buttonValues: List<Keypad>,
    calculatorEngine: CalculatorEngine,
) {
    val state = calculatorEngine.state
    val operation = state.operation
    Row(
        modifier = modifier,
    ) {
        buttonValues.forEach { keypad ->
            IconButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(
                        when (keypad.size) {
                            KeypadSize.NORMAL -> 1f
                            KeypadSize.DOUBLE_WIDE -> 2f
                            KeypadSize.DOUBLE_TALL -> 2f
                            KeypadSize.DOUBLE_WIDE_TALL -> 4f
                        },
                    ),
                onClick = {
                    calculatorEngine.onKeypadClicked(keypad)
                },
            ) {
                Box(
                    modifier = Modifier
                        .then(
                            when (keypad.size) {
                                KeypadSize.NORMAL ->
                                    Modifier
                                        .fillMaxSize(0.8f)
                                        .aspectRatio(1f)

                                KeypadSize.DOUBLE_WIDE ->
                                    Modifier
                                        .fillMaxWidth(0.9f)
                                        .fillMaxHeight(0.8f)

                                KeypadSize.DOUBLE_TALL ->
                                    Modifier
                                        .aspectRatio(1f)

                                KeypadSize.DOUBLE_WIDE_TALL ->
                                    Modifier
                                        .fillMaxWidth(0.8f)
                                        .aspectRatio(1f)
                            },
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(
                            MaterialTheme.colorScheme.run {
                                if (
                                    operation != Operation.NONE &&
                                    keypad.operation == operation
                                ) {
                                    outlineVariant
                                } else {
                                    when (keypad.type) {
                                        KeypadType.NUMBER -> primary
                                        KeypadType.OPERATOR -> secondary
                                        KeypadType.CLEAR -> tertiary
                                        KeypadType.EQUALS -> secondary
                                        KeypadType.DECIMAL -> primary
                                        KeypadType.NEGATE -> tertiary
                                        KeypadType.PERCENT -> tertiary
                                    }
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (keypad == Keypad.CLEAR && (
                                state.displayedValue != 0.0 ||
                                    state.previousValue != 0.0
                                )
                        ) {
                            "C"
                        } else {
                            keypad.label
                        },
                        color = MaterialTheme.colorScheme.run {
                            if (operation != Operation.NONE && keypad.operation == operation) {
                                outline
                            } else {
                                when (keypad.type) {
                                    KeypadType.NUMBER -> onPrimary
                                    KeypadType.OPERATOR -> onSecondary
                                    KeypadType.CLEAR -> onTertiary
                                    KeypadType.EQUALS -> onSecondary
                                    KeypadType.DECIMAL -> onPrimary
                                    KeypadType.NEGATE -> onTertiary
                                    KeypadType.PERCENT -> onTertiary
                                }
                            }
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    )
                }
            }
        }
    }
}
