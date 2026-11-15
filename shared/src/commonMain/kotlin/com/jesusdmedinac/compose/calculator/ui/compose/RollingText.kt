package com.jesusdmedinac.compose.calculator.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp

private const val CHAR_WIDTH_RATIO = 0.62f
private const val LINE_HEIGHT_RATIO = 1.25f

private val RollSpring = spring(
    dampingRatio = 0.6f,
    stiffness = Spring.StiffnessMediumLow,
    visibilityThreshold = IntOffset.VisibilityThreshold,
)

/**
 * Shows [text] one character at a time so each digit rolls in on a spring when
 * it changes, like a mechanical counter: higher digits roll up, lower ones down.
 * The font shrinks to keep long numbers on a single line and to fit short screens.
 */
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        val (widthFontSize, heightFontSize) = with(LocalDensity.current) {
            (maxWidth / (text.length.coerceAtLeast(1) * CHAR_WIDTH_RATIO)).toSp() to
                (maxHeight / LINE_HEIGHT_RATIO).toSp()
        }
        val fontSize = minOf(widthFontSize.value, heightFontSize.value, style.fontSize.value).sp
        Row {
            text.forEachIndexed { index, char ->
                key(index) {
                    RollingChar(char = char, style = style.copy(fontSize = fontSize), color = color)
                }
            }
        }
    }
}

@Composable
private fun RollingChar(char: Char, style: TextStyle, color: Color) {
    // Starts empty so a newly typed character rolls in instead of popping up.
    val target = remember { mutableStateOf<Char?>(null) }
    LaunchedEffect(char) { target.value = char }
    AnimatedContent(
        targetState = target.value,
        transitionSpec = {
            val rollsUp = (targetState?.digitToIntOrNull() ?: 0) >= (initialState?.digitToIntOrNull() ?: 0)
            val direction = if (rollsUp) 1 else -1
            (slideInVertically(RollSpring) { it * direction } + fadeIn()) togetherWith
                (slideOutVertically(RollSpring) { -it * direction } + fadeOut()) using
                SizeTransform(clip = false)
        },
    ) { shownChar ->
        if (shownChar != null) {
            Text(text = shownChar.toString(), style = style, color = color, maxLines = 1, softWrap = false)
        }
    }
}
