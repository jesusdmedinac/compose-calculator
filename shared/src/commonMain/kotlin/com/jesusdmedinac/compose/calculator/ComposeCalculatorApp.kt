package com.jesusdmedinac.compose.calculator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.compose.calculator.ui.ComposeCalculatorTheme
import com.jesusdmedinac.compose.calculator.ui.compose.ResponsiveText
import com.jesusdmedinac.compose.calculator.ui.model.CalculatorEngine
import com.jesusdmedinac.compose.calculator.ui.model.Keypad
import com.jesusdmedinac.compose.calculator.ui.model.KeypadType
import com.jesusdmedinac.compose.calculator.ui.model.Operation

private val KeyGap = 12.dp
private val CircleRadius = 50.dp
private val SquircleRadius = 28.dp
private val ActiveRadius = 18.dp
private val PressedRadius = 14.dp

@Composable
fun ComposeCalculatorApp() {
    ComposeCalculatorTheme {
        val calculatorEngine by remember { mutableStateOf(CalculatorEngine()) }
        val state = calculatorEngine.state
        val hasResult = state.displayedResult.isNotEmpty()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
                ) {
                    Text(
                        text = state.lastCalculation,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    if (hasResult) {
                        ResponsiveText(
                            text = state.displayedCalculation,
                            textStyle = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasResult) {
                            Text(
                                text = "=",
                                modifier = Modifier.padding(end = 10.dp),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        ResponsiveText(
                            text = state.displayedResult
                                .ifEmpty { state.displayedCalculation }
                                .ifEmpty { "0" },
                            textStyle = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 96.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(KeyGap),
                ) {
                    ColumnOfButtons(
                        keypads = Keypad.values().toList(),
                        calculatorEngine = calculatorEngine,
                    )
                }
            }
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
    val operation = calculatorEngine.state.operation
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KeyGap),
    ) {
        buttonValues.forEach { keypad ->
            KeypadButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                keypad = keypad,
                isActive = operation != Operation.NONE && keypad.operation == operation,
                onClick = { calculatorEngine.onKeypadClicked(keypad) },
            )
        }
    }
}

/**
 * Expressive key: numbers are circles, everything else is a squircle, and the
 * active operator or a pressed key morphs into a tighter shape.
 */
@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    keypad: Keypad,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val (containerColor, contentColor) = keypadColors(keypad, isActive)
    val isNumberKey = keypad.type in setOf(KeypadType.NUMBER, KeypadType.DECIMAL, KeypadType.UNDO)
    val cornerRadius by animateDpAsState(
        when {
            isPressed -> PressedRadius
            isActive -> ActiveRadius
            isNumberKey -> CircleRadius
            else -> SquircleRadius
        },
    )
    val animatedContainerColor by animateColorAsState(containerColor)
    val animatedContentColor by animateColorAsState(contentColor)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(animatedContainerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = keypad.label,
            color = animatedContentColor,
            fontSize = if (keypad.label.length > 1) 26.sp else 32.sp,
            fontWeight = if (isNumberKey) FontWeight.Medium else FontWeight.SemiBold,
        )
    }
}

@Composable
private fun keypadColors(keypad: Keypad, isActive: Boolean): Pair<Color, Color> =
    MaterialTheme.colorScheme.run {
        when {
            isActive -> primary to onPrimary
            keypad.type == KeypadType.EQUALS -> primary to onPrimary
            keypad.type == KeypadType.CLEAR -> tertiaryContainer to onTertiaryContainer
            keypad.type == KeypadType.OPERATOR && keypad.operation == Operation.NONE ->
                tertiaryContainer to onTertiaryContainer
            keypad.type == KeypadType.OPERATOR -> secondaryContainer to onSecondaryContainer
            else -> surface to onSurface
        }
    }
