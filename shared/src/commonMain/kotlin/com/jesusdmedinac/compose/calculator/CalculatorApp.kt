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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.jesusdmedinac.compose.calculator.ui.model.Keypad
import com.jesusdmedinac.compose.calculator.ui.model.KeypadSize
import com.jesusdmedinac.compose.calculator.ui.model.KeypadType

@Composable
fun ComposeCalculatorApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        var displayValue by remember { mutableStateOf("0") }
        var savedOperatorKeypad by remember { mutableStateOf<Keypad?>(null) }
        Column {
            Box(
                modifier = Modifier
                    .weight(2.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Text(
                    text = displayValue,
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
                onKeypadClick = { keypad ->
                    when (keypad.type) {
                        KeypadType.NUMBER -> {
                            if (savedOperatorKeypad != null) {
                                when (savedOperatorKeypad) {
                                    Keypad.DIVIDE -> {
                                        displayValue =
                                            (displayValue.toDouble() / keypad.label.toDouble()).toString()
                                    }

                                    Keypad.MULTIPLY -> {
                                        displayValue =
                                            (displayValue.toDouble() * keypad.label.toDouble()).toString()
                                    }

                                    Keypad.SUBTRACT -> {
                                        displayValue =
                                            (displayValue.toDouble() - keypad.label.toDouble()).toString()
                                    }

                                    Keypad.ADD -> {
                                        displayValue =
                                            (displayValue.toDouble() + keypad.label.toDouble()).toString()
                                    }

                                    else -> Unit
                                }
                            } else {
                                displayValue = when (displayValue) {
                                    "0" -> keypad.label
                                    else -> displayValue + keypad.label
                                }
                            }
                        }

                        KeypadType.DECIMAL -> {
                            if (!displayValue.contains(".")) {
                                displayValue += keypad.label
                            }
                        }

                        KeypadType.OPERATOR -> {
                            if (displayValue == "0") return@ColumnOfButtons
                            savedOperatorKeypad = keypad
                        }

                        KeypadType.PERCENT -> {
                            displayValue = (displayValue.toDouble() / 100).toString()
                        }

                        KeypadType.EQUALS -> {
                            when (savedOperatorKeypad) {
                                Keypad.DIVIDE -> {
                                    displayValue =
                                        (displayValue.toDouble() / displayValue.toDouble()).toString()
                                }

                                Keypad.MULTIPLY -> {
                                    displayValue =
                                        (displayValue.toDouble() * displayValue.toDouble()).toString()
                                }

                                Keypad.SUBTRACT -> {
                                    displayValue =
                                        (displayValue.toDouble() - displayValue.toDouble()).toString()
                                }

                                Keypad.ADD -> {
                                    displayValue =
                                        (displayValue.toDouble() + displayValue.toDouble()).toString()
                                }

                                else -> Unit
                            }
                            savedOperatorKeypad = null
                        }

                        KeypadType.NEGATE -> {
                            displayValue = (displayValue.toDouble() * -1).toString()
                        }

                        KeypadType.CLEAR -> {
                            displayValue = "0"
                            savedOperatorKeypad = null
                        }
                    }
                    if (displayValue.endsWith(".0")) {
                        displayValue = displayValue.substringBefore(".0")
                    }
                },
            )
        }
    }
}

@Composable
fun ColumnScope.ColumnOfButtons(
    keypads: List<Keypad>,
    onKeypadClick: (Keypad) -> Unit,
) {
    keypads
        .chunked(4)
        .forEach { rowOfButtons ->
            RowOfButtons(
                modifier = Modifier.weight(1f),
                buttonValues = rowOfButtons,
                onKeypadClick = onKeypadClick,
            )
        }
}

@Composable
fun RowOfButtons(
    modifier: Modifier = Modifier,
    buttonValues: List<Keypad>,
    onKeypadClick: (Keypad) -> Unit,
) {
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
                    onKeypadClick(keypad)
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
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.run {
                                when (keypad.type) {
                                    KeypadType.NUMBER -> primary
                                    KeypadType.OPERATOR -> secondary
                                    KeypadType.CLEAR -> tertiary
                                    KeypadType.EQUALS -> secondary
                                    KeypadType.DECIMAL -> primary
                                    KeypadType.NEGATE -> tertiary
                                    KeypadType.PERCENT -> tertiary
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = keypad.label,
                        color = MaterialTheme.colorScheme.run {
                            when (keypad.type) {
                                KeypadType.NUMBER -> onPrimary
                                KeypadType.OPERATOR -> onSecondary
                                KeypadType.CLEAR -> onTertiary
                                KeypadType.EQUALS -> onSecondary
                                KeypadType.DECIMAL -> onPrimary
                                KeypadType.NEGATE -> onTertiary
                                KeypadType.PERCENT -> onTertiary
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
