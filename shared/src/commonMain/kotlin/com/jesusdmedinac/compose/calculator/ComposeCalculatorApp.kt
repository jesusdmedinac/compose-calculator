package com.jesusdmedinac.compose.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.compose.calculator.ui.ComposeCalculatorTheme
import com.jesusdmedinac.compose.calculator.ui.compose.MorphingKeyShape
import com.jesusdmedinac.compose.calculator.ui.compose.ResponsiveText
import com.jesusdmedinac.compose.calculator.ui.compose.RollingText
import com.jesusdmedinac.compose.calculator.ui.model.CalculatorEngine
import com.jesusdmedinac.compose.calculator.ui.model.CalculatorState
import com.jesusdmedinac.compose.calculator.ui.model.Keypad
import com.jesusdmedinac.compose.calculator.ui.model.KeypadType
import com.jesusdmedinac.compose.calculator.ui.model.Operation
import kotlin.math.hypot
import kotlinx.coroutines.launch

private val KeyGap = 12.dp
private val NeighborPush = 6.dp

private const val CircleExponent = 2f
private const val SquircleExponent = 3.6f
private const val PressedExponent = 5f
private const val PressedScale = 0.9f

@Composable
fun ComposeCalculatorApp() {
    ComposeCalculatorTheme {
        val calculatorEngine by remember { mutableStateOf(CalculatorEngine()) }
        val state by calculatorEngine.state.collectAsState()
        val scope = rememberCoroutineScope()
        val sweepColor = MaterialTheme.colorScheme.tertiaryContainer
        val sweepProgress = remember { Animatable(0f) }
        val sweepAlpha = remember { Animatable(0f) }
        var sweepOrigin by remember { mutableStateOf(Offset.Zero) }
        var contentOrigin by remember { mutableStateOf(Offset.Zero) }
        var clearKeyCenter by remember { mutableStateOf(Offset.Zero) }

        val onKeyClick: (Keypad) -> Unit = { keypad ->
            if (keypad == Keypad.CLEAR) {
                sweepOrigin = clearKeyCenter - contentOrigin
                scope.launch {
                    sweepAlpha.snapTo(1f)
                    sweepProgress.snapTo(0f)
                    sweepProgress.animateTo(1f, tween(durationMillis = 450, easing = FastOutSlowInEasing))
                    sweepAlpha.animateTo(0f, tween(durationMillis = 250))
                }
            }
            calculatorEngine.onKeypadClicked(keypad)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onGloballyPositioned { contentOrigin = it.positionInRoot() }
                    // AC sweep: a wave grows from the AC key behind the keypad sheet.
                    .drawBehind {
                        if (sweepAlpha.value > 0f) {
                            drawCircle(
                                color = sweepColor,
                                radius = sweepProgress.value * hypot(size.width, size.height),
                                center = sweepOrigin,
                                alpha = sweepAlpha.value,
                            )
                        }
                    },
            ) {
                Display(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = state,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(KeyGap),
                ) {
                    ColumnOfButtons(
                        keypads = Keypad.values().toList(),
                        operation = state.operation,
                        onKeyClick = onKeyClick,
                        onClearKeyPositioned = { clearKeyCenter = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun Display(
    modifier: Modifier = Modifier,
    state: CalculatorState,
) {
    val hasResult = state.displayedResult.isNotEmpty()
    // "=" pop: when a calculation finishes the result bounces into place.
    val resultScale = remember { Animatable(1f) }
    LaunchedEffect(state.lastCalculation) {
        if (state.lastCalculation.isNotEmpty()) {
            resultScale.snapTo(0.8f)
            resultScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow))
        }
    }
    Column(
        modifier = modifier.padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
    ) {
        // The finished expression flies up and shrinks into the history line.
        AnimatedContent(
            targetState = state.lastCalculation,
            transitionSpec = {
                (
                    slideInVertically(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)) { it * 2 } +
                        scaleIn(initialScale = 1.8f, transformOrigin = TransformOrigin(1f, 1f)) +
                        fadeIn()
                    ) togetherWith (slideOutVertically { -it } + fadeOut())
            },
        ) { lastCalculation ->
            Text(
                text = lastCalculation,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        AnimatedContent(
            targetState = state.displayedCalculation.takeIf { hasResult },
            contentKey = { it != null },
            transitionSpec = {
                fadeIn() togetherWith (slideOutVertically { -it } + fadeOut()) using SizeTransform(clip = false)
            },
        ) { expression ->
            if (expression != null) {
                ResponsiveText(
                    text = expression,
                    textStyle = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Row(
            modifier = Modifier.graphicsLayer {
                scaleX = resultScale.value
                scaleY = resultScale.value
                transformOrigin = TransformOrigin(1f, 0.5f)
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = hasResult,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                Text(
                    text = "=",
                    modifier = Modifier.padding(end = 10.dp),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RollingText(
                text = state.displayedResult
                    .ifEmpty { state.displayedCalculation }
                    .ifEmpty { "0" },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun ColumnOfButtons(
    keypads: List<Keypad>,
    operation: Operation,
    onKeyClick: (Keypad) -> Unit,
    onClearKeyPositioned: (Offset) -> Unit,
) {
    keypads
        .chunked(4)
        .forEach { rowOfButtons ->
            RowOfButtons(
                buttonValues = rowOfButtons,
                operation = operation,
                onKeyClick = onKeyClick,
                onClearKeyPositioned = onClearKeyPositioned,
            )
        }
}

/**
 * Keys in a row react to each other: while one is pressed, its neighbors are
 * pushed aside on a spring, like a Material 3 Expressive button group.
 */
@Composable
fun RowOfButtons(
    modifier: Modifier = Modifier,
    buttonValues: List<Keypad>,
    operation: Operation,
    onKeyClick: (Keypad) -> Unit,
    onClearKeyPositioned: (Offset) -> Unit,
) {
    var pressedIndex by remember { mutableStateOf(-1) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KeyGap),
    ) {
        buttonValues.forEachIndexed { index, keypad ->
            val distance = if (pressedIndex < 0) 0 else index - pressedIndex
            val push by animateDpAsState(
                targetValue = when (distance) {
                    0 -> 0.dp
                    1, -1 -> NeighborPush * distance
                    else -> NeighborPush / 2 * if (distance > 0) 1 else -1
                },
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
            )
            KeypadButton(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .graphicsLayer { translationX = push.toPx() }
                    .then(
                        if (keypad == Keypad.CLEAR) {
                            Modifier.onGloballyPositioned { onClearKeyPositioned(it.boundsInRoot().center) }
                        } else {
                            Modifier
                        },
                    ),
                keypad = keypad,
                isActive = operation != Operation.NONE && keypad.operation == operation,
                onPressedChange = { isPressed ->
                    if (isPressed) {
                        pressedIndex = index
                    } else if (pressedIndex == index) {
                        pressedIndex = -1
                    }
                },
                onClick = { onKeyClick(keypad) },
            )
        }
    }
}

/**
 * Expressive key: numbers are circles and everything else is a squircle. A
 * pressed key squishes and squares off on a bouncy spring, and the active
 * operator morphs into a slowly turning cookie.
 */
@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    keypad: Keypad,
    isActive: Boolean,
    onPressedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(isPressed) { onPressedChange(isPressed) }

    val (containerColor, contentColor) = keypadColors(keypad, isActive)
    val isNumberKey = keypad.type in setOf(KeypadType.NUMBER, KeypadType.DECIMAL, KeypadType.UNDO)
    val exponent by animateFloatAsState(
        targetValue = when {
            isPressed -> PressedExponent
            isActive || isNumberKey -> CircleExponent
            else -> SquircleExponent
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
    )
    val cookie by animateFloatAsState(
        targetValue = if (isActive && !isPressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow),
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
    )
    val rotation = if (isActive) {
        rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 12_000, easing = LinearEasing)),
        ).value
    } else {
        0f
    }
    val animatedContainerColor by animateColorAsState(containerColor)
    val animatedContentColor by animateColorAsState(contentColor)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = MorphingKeyShape(exponent = exponent, cookie = cookie, rotationDegrees = rotation)
                clip = true
            }
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
