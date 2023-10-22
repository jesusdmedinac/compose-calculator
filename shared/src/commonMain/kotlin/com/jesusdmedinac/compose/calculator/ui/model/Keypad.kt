package com.jesusdmedinac.compose.calculator.ui.model

enum class Keypad(
    val label: String,
    val type: KeypadType,
    val size: KeypadSize = KeypadSize.NORMAL,
) {
    CLEAR("AC", KeypadType.CLEAR),
    NEGATE("+/-", KeypadType.NEGATE),
    PERCENT("%", KeypadType.PERCENT),
    DIVIDE("/", KeypadType.OPERATOR),
    SEVEN("7", KeypadType.NUMBER),
    EIGHT("8", KeypadType.NUMBER),
    NINE("9", KeypadType.NUMBER),
    MULTIPLY("*", KeypadType.OPERATOR),
    FOUR("4", KeypadType.NUMBER),
    FIVE("5", KeypadType.NUMBER),
    SIX("6", KeypadType.NUMBER),
    SUBTRACT("-", KeypadType.OPERATOR),
    ONE("1", KeypadType.NUMBER),
    TWO("2", KeypadType.NUMBER),
    THREE("3", KeypadType.NUMBER),
    ADD("+", KeypadType.OPERATOR),
    ZERO("0", KeypadType.NUMBER, KeypadSize.DOUBLE_WIDE),
    DECIMAL(".", KeypadType.DECIMAL),
    EQUALS("=", KeypadType.EQUALS),
}
