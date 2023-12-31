package com.jesusdmedinac.compose.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.jesusdmedinac.compose.calculator.ui.compose.ResponsiveText
import com.jesusdmedinac.compose.calculator.ui.model.CalculatorEngine
import com.jesusdmedinac.compose.calculator.ui.model.Keypad
import com.jesusdmedinac.compose.calculator.ui.model.KeypadType
import com.jesusdmedinac.compose.calculator.ui.model.Operation

@Composable
fun ComposeCalculatorApp() {
    val calculatorEngine by remember { mutableStateOf(CalculatorEngine()) }
    val state = calculatorEngine.state
    val displayedCalculation = state.displayedCalculation
    val displayedResult = state.displayedResult
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ResponsiveText(
                        text = displayedCalculation,
                        modifier = Modifier,
                        textStyle = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.End,
                    )
                    ResponsiveText(
                        text = displayedResult,
                        modifier = Modifier,
                        textStyle = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
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
                    .weight(1f),
                onClick = {
                    calculatorEngine.onKeypadClicked(keypad)
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .aspectRatio(1f)
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
                                        KeypadType.CLEAR -> tertiary
                                        KeypadType.OPERATOR -> secondary
                                        KeypadType.NUMBER -> primary
                                        KeypadType.EQUALS -> secondary
                                        KeypadType.DECIMAL -> primary
                                        KeypadType.UNDO -> primary
                                    }
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = keypad.label,
                        color = MaterialTheme.colorScheme.run {
                            if (operation != Operation.NONE && keypad.operation == operation) {
                                outline
                            } else {
                                when (keypad.type) {
                                    KeypadType.CLEAR -> onTertiary
                                    KeypadType.OPERATOR -> onSecondary
                                    KeypadType.NUMBER -> onPrimary
                                    KeypadType.EQUALS -> onSecondary
                                    KeypadType.DECIMAL -> onPrimary
                                    KeypadType.UNDO -> onPrimary
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
