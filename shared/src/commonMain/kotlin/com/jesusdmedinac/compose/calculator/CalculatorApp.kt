package com.jesusdmedinac.compose.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CalculatorApp() {
    MaterialTheme {
        Column {
            TextField(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                value = "0",
                onValueChange = { /*TODO*/ },
                textStyle = MaterialTheme.typography.h1.copy(
                    textAlign = TextAlign.End,
                ),
            )
            ColumnOfButtons(
                buttonValues = listOf(
                    listOf("C", "±", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=", ""),
                ),
            )
        }
    }
}

@Composable
fun ColumnScope.ColumnOfButtons(
    buttonValues: List<List<String>>,
) {
    buttonValues.forEach { rowOfButtons ->
        RowOfButtons(
            modifier = Modifier.weight(1f),
            buttonValues = rowOfButtons,
        )
    }
}

@Composable
fun RowOfButtons(
    modifier: Modifier = Modifier,
    buttonValues: List<String>,
) {
    Row(
        modifier = modifier,
    ) {
        buttonValues.forEach { buttonValue ->
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = { /*TODO*/ },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = buttonValue,
                        color = MaterialTheme.colors.onPrimary,
                    )
                }
            }
        }
    }
}
