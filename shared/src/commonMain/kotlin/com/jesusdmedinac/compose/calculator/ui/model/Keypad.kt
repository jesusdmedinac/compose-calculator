package com.jesusdmedinac.compose.calculator.ui.model

enum class Keypad(
    val label: String,
    val type: KeypadType,
    val operation: Operation = Operation.NONE,
    val value: String = label,
) {
    CLEAR("AC", KeypadType.CLEAR),
    PARENTHESIS("( )", KeypadType.OPERATOR, value = "()"),
    PERCENT("%", KeypadType.OPERATOR),
    DIVIDE("÷", KeypadType.OPERATOR, operation = Operation.DIVIDE, value = "/"),
    SEVEN("7", KeypadType.NUMBER),
    EIGHT("8", KeypadType.NUMBER),
    NINE("9", KeypadType.NUMBER),
    MULTIPLY("*", KeypadType.OPERATOR, operation = Operation.MULTIPLY),
    FOUR("4", KeypadType.NUMBER),
    FIVE("5", KeypadType.NUMBER),
    SIX("6", KeypadType.NUMBER),
    SUBTRACT("-", KeypadType.OPERATOR, operation = Operation.SUBTRACT),
    ONE("1", KeypadType.NUMBER),
    TWO("2", KeypadType.NUMBER),
    THREE("3", KeypadType.NUMBER),
    ADD("+", KeypadType.OPERATOR, operation = Operation.ADD),
    ZERO("0", KeypadType.NUMBER),
    DECIMAL(".", KeypadType.DECIMAL),
    UNDO("⌫", KeypadType.UNDO),
    EQUALS("=", KeypadType.EQUALS),
}
