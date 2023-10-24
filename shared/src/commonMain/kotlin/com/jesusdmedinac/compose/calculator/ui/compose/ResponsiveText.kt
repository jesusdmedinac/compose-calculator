/**
 * @see https://medium.com/tech-takeaways/responsive-auto-resizing-text-with-jetpack-compose-b8238aaf0e09
 * @author [Yanneck Reiß](https://yanneck-reiss.medium.com/)
 */
package com.jesusdmedinac.compose.calculator.ui.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.9f
private const val TEXT_SCALE_INCREMENT_INTERVAL = 1.1f

@Composable
fun ResponsiveText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Center,
    textStyle: TextStyle,
    targetTextSizeHeight: TextUnit = textStyle.fontSize,
    maxLines: Int = 1,
) {
    var textSize by remember { mutableStateOf(targetTextSizeHeight) }
    var textLength by remember { mutableStateOf(text.length) }

    Text(
        modifier = modifier,
        text = text,
        color = color,
        textAlign = textAlign,
        fontSize = textSize,
        fontFamily = textStyle.fontFamily,
        fontStyle = textStyle.fontStyle,
        fontWeight = textStyle.fontWeight,
        lineHeight = textStyle.lineHeight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            val maxCurrentLineIndex: Int = textLayoutResult.lineCount - 1

            if (textLayoutResult.isLineEllipsized(maxCurrentLineIndex)) {
                textSize = textSize.times(TEXT_SCALE_REDUCTION_INTERVAL)
            } else if (textLength - text.length == 1) {
                textSize = kotlin.math.min(
                    textSize.times(TEXT_SCALE_INCREMENT_INTERVAL).value,
                    textStyle.fontSize.value,
                ).sp
            }
            textLength = text.length
        },
    )
}
