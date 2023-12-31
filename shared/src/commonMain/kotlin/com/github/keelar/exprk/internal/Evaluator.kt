package com.github.keelar.exprk.internal

import com.github.keelar.exprk.ExpressionException
import com.github.keelar.exprk.internal.TokenType.AMP_AMP
import com.github.keelar.exprk.internal.TokenType.BAR_BAR
import com.github.keelar.exprk.internal.TokenType.EQUAL_EQUAL
import com.github.keelar.exprk.internal.TokenType.EXPONENT
import com.github.keelar.exprk.internal.TokenType.GREATER
import com.github.keelar.exprk.internal.TokenType.GREATER_EQUAL
import com.github.keelar.exprk.internal.TokenType.LESS
import com.github.keelar.exprk.internal.TokenType.LESS_EQUAL
import com.github.keelar.exprk.internal.TokenType.MINUS
import com.github.keelar.exprk.internal.TokenType.MODULO
import com.github.keelar.exprk.internal.TokenType.NOT_EQUAL
import com.github.keelar.exprk.internal.TokenType.PLUS
import com.github.keelar.exprk.internal.TokenType.SLASH
import com.github.keelar.exprk.internal.TokenType.SQUARE_ROOT
import com.github.keelar.exprk.internal.TokenType.STAR
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

internal class Evaluator() : ExprVisitor<BigDecimal> {
    internal var decimalMode: DecimalMode = DecimalMode.DEFAULT

    private val variables: LinkedHashMap<String, BigDecimal> = linkedMapOf()
    private val functions: MutableMap<String, Function> = mutableMapOf()

    private fun define(name: String, value: BigDecimal) {
        variables += name to value
    }

    fun define(name: String, expr: Expr): Evaluator {
        define(name.toLowerCase(), eval(expr))

        return this
    }

    fun addFunction(name: String, function: Function): Evaluator {
        functions += name.toLowerCase() to function

        return this
    }

    fun eval(expr: Expr): BigDecimal {
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: AssignExpr): BigDecimal {
        val value = eval(expr.value)

        define(expr.name.lexeme, value)

        return value
    }

    override fun visitLogicalExpr(expr: LogicalExpr): BigDecimal {
        val left = expr.left
        val right = expr.right

        return when (expr.operator.type) {
            BAR_BAR -> left or right
            AMP_AMP -> left and right
            else -> throw ExpressionException(
                "Invalid logical operator '${expr.operator.lexeme}'",
            )
        }
    }

    override fun visitBinaryExpr(expr: BinaryExpr): BigDecimal {
        val left = eval(expr.left)
        val right = eval(expr.right)

        return when (expr.operator.type) {
            PLUS -> left + right
            MINUS -> left - right
            STAR -> left * right
            SLASH -> left.divide(right, decimalMode)
            MODULO -> left.remainder(right)
            EXPONENT -> left pow right
            EQUAL_EQUAL -> (left == right).toBigDecimal()
            NOT_EQUAL -> (left != right).toBigDecimal()
            GREATER -> (left > right).toBigDecimal()
            GREATER_EQUAL -> (left >= right).toBigDecimal()
            LESS -> (left < right).toBigDecimal()
            LESS_EQUAL -> (left <= right).toBigDecimal()
            else -> throw ExpressionException(
                "Invalid binary operator '${expr.operator.lexeme}'",
            )
        }
    }

    override fun visitUnaryExpr(expr: UnaryExpr): BigDecimal {
        val right = eval(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                right.negate()
            }

            SQUARE_ROOT -> {
                right.pow(BigDecimal.fromDouble(0.5))
            }

            else -> throw ExpressionException("Invalid unary operator")
        }
    }

    override fun visitCallExpr(expr: CallExpr): BigDecimal {
        val name = expr.name
        val function =
            functions[name.toLowerCase()] ?: throw ExpressionException("Undefined function '$name'")

        return function.call(expr.arguments.map { eval(it) })
    }

    override fun visitLiteralExpr(expr: LiteralExpr): BigDecimal {
        return expr.value
    }

    override fun visitVariableExpr(expr: VariableExpr): BigDecimal {
        val name = expr.name.lexeme

        return variables[name.toLowerCase()]
            ?: throw ExpressionException("Undefined variable '$name'")
    }

    override fun visitGroupingExpr(expr: GroupingExpr): BigDecimal {
        return eval(expr.expression)
    }

    private infix fun Expr.or(right: Expr): BigDecimal {
        val left = eval(this)

        // short-circuit if left is truthy
        if (left.isTruthy()) return BigDecimal.ONE

        return eval(right).isTruthy().toBigDecimal()
    }

    private infix fun Expr.and(right: Expr): BigDecimal {
        val left = eval(this)

        // short-circuit if left is falsey
        if (!left.isTruthy()) return BigDecimal.ZERO

        return eval(right).isTruthy().toBigDecimal()
    }

    private fun BigDecimal.isTruthy(): Boolean {
        return this != BigDecimal.ZERO
    }

    private fun Boolean.toBigDecimal(): BigDecimal {
        return if (this) BigDecimal.ONE else BigDecimal.ZERO
    }

    private infix fun BigDecimal.pow(n: BigDecimal): BigDecimal {
        var right = n
        val signOfRight = right.signum()
        right = right.multiply(BigDecimal.fromInt(signOfRight))
        val remainderOfRight = right.remainder(BigDecimal.ONE)
        val n2IntPart = right.subtract(remainderOfRight)
        val intPow = pow(n2IntPart.intValue())
        val doublePow = BigDecimal.fromDouble(pow(remainderOfRight).doubleValue())

        var result = intPow.multiply(doublePow, this@Evaluator.decimalMode)
        if (signOfRight == -1) {
            result = BigDecimal
                .ONE.divide(
                    result,
                    DecimalMode(
                        this@Evaluator.decimalMode.decimalPrecision,
                        RoundingMode.ROUND_HALF_CEILING,
                    ),
                )
        }

        return result
    }
}
