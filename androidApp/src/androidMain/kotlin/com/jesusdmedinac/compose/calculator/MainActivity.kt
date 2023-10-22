package com.jesusdmedinac.compose.calculator

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.jesusdmedinac.compose.calculator.ui.ComposeCalculatorTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeCalculatorTheme {
                MainView()
            }
        }
    }
}
