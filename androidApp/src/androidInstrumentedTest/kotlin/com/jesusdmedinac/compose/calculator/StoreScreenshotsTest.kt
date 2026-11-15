package com.jesusdmedinac.compose.calculator

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.AfterClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.DecorViewScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * Store screenshots for Google Play, captured by `fastlane screenshots`.
 * Each test sets the system theme, types a calculation and takes one screenshot.
 * Screenshots draw only the app window, so the device's status bar icons and
 * overlays such as Samsung's Edge panel handle stay out of them.
 */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotsTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Test
    fun screenshot1_division() = capture("1_division", darkTheme = false, "1280÷16+4=")

    @Test
    fun screenshot2_activeOperator() = capture("2_active_operator", darkTheme = false, "2450×12−")

    @Test
    fun screenshot3_exactDecimals() = capture("3_exact_decimals", darkTheme = true, "0.1+0.2=")

    @Test
    fun screenshot4_largeNumbers() = capture("4_large_numbers", darkTheme = true, "888888881201868+4")

    @After
    fun tearDown() {
        scenario?.close()
    }

    private fun capture(name: String, darkTheme: Boolean, keys: String) {
        shell("cmd uimode night ${if (darkTheme) "yes" else "no"}")
        scenario = ActivityScenario.launch(MainActivity::class.java)
        keys.forEach { key ->
            composeRule.onNode(hasText(key.toString()) and hasClickAction()).performClick()
        }
        composeRule.waitForIdle()
        // Let the springs and the rolling digits settle; the test clock does not drive them.
        Thread.sleep(SETTLE_MILLIS)
        lateinit var activity: MainActivity
        scenario?.onActivity { activity = it }
        Screengrab.screenshot(name, DecorViewScreenshotStrategy(activity))
    }

    companion object {
        private const val SETTLE_MILLIS = 1_500L

        @get:ClassRule
        @JvmStatic
        val localeTestRule = LocaleTestRule()

        private fun shell(command: String) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand(command)
                .close()
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            shell("cmd uimode night no")
        }
    }
}
