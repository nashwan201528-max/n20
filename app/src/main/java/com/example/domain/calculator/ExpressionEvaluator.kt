package com.example.domain.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack
import kotlin.math.*

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
}

object ExpressionEvaluator {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat = DecimalFormat("#,##0.########", symbols)
    private val scientificFormat = DecimalFormat("0.######E0", symbols)

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        val absVal = abs(value)
        return if (absVal != 0.0 && (absVal >= 1e12 || absVal < 1e-6)) {
            scientificFormat.format(value)
        } else {
            // Strip trailing zero decimals if integer
            decimalFormat.format(value)
        }
    }

    /**
     * Cleans up expression display tokens (e.g. replacing standard x with ×, / with ÷)
     */
    fun sanitizeForDisplay(input: String): String {
        return input.replace("*", "×").replace("/", "÷")
    }

    /**
     * Normalizes expression for evaluation: converts × to *, ÷ to /, π to pi, etc.
     */
    fun normalize(input: String): String {
        var s = input.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("π", "PI")
            .replace("e", "E_CONST")
        return s
    }

    /**
     * Evaluates live preview. If expression is incomplete (e.g. ending in operator or unmatched '('),
     * returns null without throwing.
     */
    fun evaluateLive(expression: String, isDegree: Boolean = true): String? {
        if (expression.isBlank()) return null
        val clean = expression.trim()
        val lastChar = clean.last()
        if (lastChar in listOf('+', '-', '×', '÷', '*', '/', '^', '(')) {
            return null
        }
        return when (val res = evaluate(expression, isDegree)) {
            is EvaluationResult.Success -> res.formatted
            is EvaluationResult.Error -> null
        }
    }

    /**
     * Complete evaluation with comprehensive error handling.
     */
    fun evaluate(expression: String, isDegree: Boolean = true): EvaluationResult {
        if (expression.isBlank()) return EvaluationResult.Error("Empty expression")

        try {
            val normalized = normalize(expression)
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) return EvaluationResult.Error("Empty expression")

            val rpn = shuntingYard(tokens)
            val value = evaluateRPN(rpn, isDegree)

            return when {
                value.isNaN() -> EvaluationResult.Error("Undefined")
                value.isInfinite() -> EvaluationResult.Error("Cannot divide by zero")
                else -> EvaluationResult.Success(value, formatNumber(value))
            }
        } catch (e: ArithmeticException) {
            return EvaluationResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            return EvaluationResult.Error(e.message ?: "Syntax error")
        } catch (e: Exception) {
            return EvaluationResult.Error("Syntax error")
        }
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: Char, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        object LeftParen : Token()
        object RightParen : Token()
        object Comma : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = expr.length

        var expectUnary = true // start of expression or after operator/left-paren

        while (i < len) {
            val c = expr[i]

            when {
                c.isWhitespace() -> {
                    i++
                }

                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    val num = sb.toString().toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number")
                    tokens.add(Token.Number(num))
                    expectUnary = false
                }

                c == '(' -> {
                    // Implicit multiplication check, e.g. 5(2) or )(
                    if (!expectUnary && tokens.isNotEmpty() && (tokens.last() is Token.Number || tokens.last() is Token.RightParen)) {
                        tokens.add(Token.Operator('*', 2))
                    }
                    tokens.add(Token.LeftParen)
                    expectUnary = true
                    i++
                }

                c == ')' -> {
                    tokens.add(Token.RightParen)
                    expectUnary = false
                    i++
                }

                c == ',' -> {
                    tokens.add(Token.Comma)
                    expectUnary = true
                    i++
                }

                c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^' -> {
                    if (c == '-' && expectUnary) {
                        // Unary minus treated as 0 - x or negated number
                        tokens.add(Token.Number(0.0))
                        tokens.add(Token.Operator('-', 1))
                    } else if (c == '+' && expectUnary) {
                        // Unary plus ignored
                    } else {
                        val precedence = when (c) {
                            '+', '-' -> 1
                            '*', '/', '%' -> 2
                            '^' -> 3
                            else -> 0
                        }
                        val rightAssoc = (c == '^')
                        tokens.add(Token.Operator(c, precedence, rightAssoc))
                        expectUnary = true
                    }
                    i++
                }

                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isLetter() || expr[i] == '_')) {
                        sb.append(expr[i])
                        i++
                    }
                    val name = sb.toString()
                    when (name) {
                        "PI" -> {
                            if (!expectUnary && tokens.isNotEmpty() && (tokens.last() is Token.Number || tokens.last() is Token.RightParen)) {
                                tokens.add(Token.Operator('*', 2))
                            }
                            tokens.add(Token.Number(Math.PI))
                            expectUnary = false
                        }
                        "E_CONST" -> {
                            if (!expectUnary && tokens.isNotEmpty() && (tokens.last() is Token.Number || tokens.last() is Token.RightParen)) {
                                tokens.add(Token.Operator('*', 2))
                            }
                            tokens.add(Token.Number(Math.E))
                            expectUnary = false
                        }
                        else -> {
                            if (!expectUnary && tokens.isNotEmpty() && (tokens.last() is Token.Number || tokens.last() is Token.RightParen)) {
                                tokens.add(Token.Operator('*', 2))
                            }
                            tokens.add(Token.Function(name.lowercase()))
                            expectUnary = true
                        }
                    }
                }

                else -> {
                    throw IllegalArgumentException("Unexpected character: $c")
                }
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val opStack = Stack<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)

                is Token.Function -> opStack.push(token)

                is Token.Operator -> {
                    while (opStack.isNotEmpty()) {
                        val top = opStack.peek()
                        if (top is Token.Function) {
                            output.add(opStack.pop())
                        } else if (top is Token.Operator) {
                            val cond = if (token.isRightAssociative) {
                                top.precedence > token.precedence
                            } else {
                                top.precedence >= token.precedence
                            }
                            if (cond) {
                                output.add(opStack.pop())
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    opStack.push(token)
                }

                is Token.LeftParen -> opStack.push(token)

                is Token.RightParen -> {
                    var foundParen = false
                    while (opStack.isNotEmpty()) {
                        val top = opStack.pop()
                        if (top is Token.LeftParen) {
                            foundParen = true
                            break
                        } else {
                            output.add(top)
                        }
                    }
                    if (!foundParen) {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                    if (opStack.isNotEmpty() && opStack.peek() is Token.Function) {
                        output.add(opStack.pop())
                    }
                }

                is Token.Comma -> {
                    while (opStack.isNotEmpty() && opStack.peek() !is Token.LeftParen) {
                        output.add(opStack.pop())
                    }
                }
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.pop()
            if (top is Token.LeftParen || top is Token.RightParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRPN(rpn: List<Token>, isDegree: Boolean): Double {
        val stack = Stack<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.push(token.value)

                is Token.Operator -> {
                    if (stack.size < 2) throw IllegalArgumentException("Malformed syntax")
                    val b = stack.pop()
                    val a = stack.pop()
                    val res = when (token.symbol) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> {
                            if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                            a / b
                        }
                        '%' -> {
                            if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                            a % b
                        }
                        '^' -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown operator: ${token.symbol}")
                    }
                    stack.push(res)
                }

                is Token.Function -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Malformed function syntax")
                    val arg = stack.pop()
                    val toRad = if (isDegree) Math.toRadians(arg) else arg
                    val res = when (token.name) {
                        "sin" -> sin(toRad)
                        "cos" -> cos(toRad)
                        "tan" -> {
                            val c = cos(toRad)
                            if (abs(c) < 1e-12) throw ArithmeticException("Undefined (tan 90°)")
                            tan(toRad)
                        }
                        "asin" -> {
                            if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Invalid domain for asin")
                            val r = asin(arg)
                            if (isDegree) Math.toDegrees(r) else r
                        }
                        "acos" -> {
                            if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Invalid domain for acos")
                            val r = acos(arg)
                            if (isDegree) Math.toDegrees(r) else r
                        }
                        "atan" -> {
                            val r = atan(arg)
                            if (isDegree) Math.toDegrees(r) else r
                        }
                        "ln" -> {
                            if (arg <= 0.0) throw ArithmeticException("Invalid domain for ln (must be > 0)")
                            ln(arg)
                        }
                        "log", "log10" -> {
                            if (arg <= 0.0) throw ArithmeticException("Invalid domain for log (must be > 0)")
                            log10(arg)
                        }
                        "sqrt" -> {
                            if (arg < 0.0) throw ArithmeticException("Invalid domain for sqrt (must be ≥ 0)")
                            sqrt(arg)
                        }
                        "cbrt" -> cbrt(arg)
                        "abs" -> abs(arg)
                        "fact" -> factorial(arg)
                        else -> throw IllegalArgumentException("Unknown function: ${token.name}")
                    }
                    stack.push(res)
                }

                else -> {}
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("Malformed syntax")
        return stack.pop()
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw ArithmeticException("Factorial requires non-negative integer")
        if (n > 170) throw ArithmeticException("Result overflow for factorial")
        var res = 1.0
        val limit = n.toLong()
        for (i in 2..limit) {
            res *= i
        }
        return res
    }
}
