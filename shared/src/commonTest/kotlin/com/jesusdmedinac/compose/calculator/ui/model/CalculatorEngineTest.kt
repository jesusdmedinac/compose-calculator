package com.jesusdmedinac.compose.calculator.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculatorEngineTest {

    private fun CalculatorEngine.type(input: String): CalculatorEngine = apply {
        input.forEach { char ->
            val keypad = Keypad.values().first { it.label == char.toString() }
            onKeypadClicked(keypad)
        }
    }

    private fun CalculatorEngine.equals(): String {
        onKeypadClicked(Keypad.EQUALS)
        return state.value.displayedCalculation
    }

    @Test
    fun largeNumbersKeepEveryDigit() {
        assertEquals("888888881201872", CalculatorEngine().type("888888881201868+4").equals())
    }

    @Test
    fun divisionUsesTheDisplayedSymbol() {
        assertEquals("84", CalculatorEngine().type("1280÷16+4").equals())
    }

    @Test
    fun multiplicationAndSubtractionUseTheDisplayedSymbols() {
        assertEquals("-1", CalculatorEngine().type("2×3−7").equals())
    }

    @Test
    fun decimalsAreExact() {
        assertEquals("0.3", CalculatorEngine().type("0.1+0.2").equals())
    }

    @Test
    fun repeatingDecimalsAreRoundedToTheDisplayPrecision() {
        val result = CalculatorEngine().type("1÷3").equals()
        assertTrue(result.startsWith("0.33333333333333333"), result)
        assertTrue(result.length <= 22, result)
    }

    @Test
    fun trailingDecimalPointPreviewsTheNumberItself() {
        assertEquals("5", CalculatorEngine().type("5.").state.value.displayedResult)
    }

    @Test
    fun trailingDecimalPointAfterAnOperatorPreviewsAZero() {
        assertEquals("5", CalculatorEngine().type("5+.").state.value.displayedResult)
    }

    @Test
    fun trailingOperatorIsIgnoredInThePreview() {
        assertEquals("12", CalculatorEngine().type("12×").state.value.displayedResult)
    }

    @Test
    fun percentIsAHundredthOfTheNumber() {
        assertEquals("0.2", CalculatorEngine().type("20%").equals())
    }

    @Test
    fun multiplyingByAPercentTakesThatShare() {
        assertEquals("10", CalculatorEngine().type("50×20%").equals())
    }

    @Test
    fun addingOrSubtractingAPercentAppliesItToTheLeftSide() {
        assertEquals("60", CalculatorEngine().type("50+20%").equals())
        assertEquals("40", CalculatorEngine().type("50−20%").equals())
    }

    @Test
    fun percentIsPreviewedWhileTyping() {
        assertEquals("10", CalculatorEngine().type("50×20%").state.value.displayedResult)
    }

    @Test
    fun percentFollowedByANumberMultiplies() {
        assertEquals("10", CalculatorEngine().type("50%20").equals())
    }

    @Test
    fun parenthesesGroupBeforeMultiplying() {
        val engine = CalculatorEngine()
        engine.onKeypadClicked(Keypad.PARENTHESIS)
        engine.type("5+3")
        engine.onKeypadClicked(Keypad.PARENTHESIS)
        assertEquals("16", engine.type("×2").equals())
    }

    @Test
    fun equalsRecordsTheLastCalculation() {
        val engine = CalculatorEngine().type("12+3")
        engine.equals()
        assertEquals("12+3 = 15", engine.state.value.lastCalculation)
    }
}
