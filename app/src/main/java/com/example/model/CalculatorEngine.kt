package com.example.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object CalculatorEngine {

    /**
     * Evaluates a mathematical expression string.
     * @param expr The raw expression typed by user
     * @param isRadian Whether trigonometric functions use radians (true) or degrees (false)
     * @return EvaluationResult containing either success with formatted string, or error message
     */
    fun evaluate(expr: String, isRadian: Boolean = true): EvaluationResult {
        if (expr.isBlank()) return EvaluationResult.Empty

        try {
            val normalized = prepareExpression(expr)
            if (normalized.isBlank()) return EvaluationResult.Empty

            val parser = ExpressionParser(normalized, isRadian)
            val resultValue = parser.parse()

            if (resultValue.isNaN()) {
                return EvaluationResult.Error("Result is undefined")
            }
            if (resultValue.isInfinite()) {
                return EvaluationResult.Error("Cannot divide by zero")
            }

            val formatted = formatNumber(resultValue)
            return EvaluationResult.Success(resultValue, formatted)
        } catch (e: Exception) {
            return EvaluationResult.Error(e.message ?: "Invalid expression")
        }
    }

    /**
     * Attempts a tentative evaluation for preview while typing.
     */
    fun evaluatePreview(expr: String, isRadian: Boolean = true): String? {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return null
        
        // Don't show preview for a simple single number without operations
        if (trimmed.toDoubleOrNull() != null) return null

        // Try evaluating with auto-closing open parens and stripping trailing operators
        var sanitized = trimmed
        while (sanitized.isNotEmpty() && isTrailingOperator(sanitized.last())) {
            sanitized = sanitized.dropLast(1).trim()
        }
        if (sanitized.isEmpty()) return null

        val openCount = sanitized.count { it == '(' }
        val closeCount = sanitized.count { it == ')' }
        if (openCount > closeCount) {
            sanitized += ")".repeat(openCount - closeCount)
        }

        return when (val res = evaluate(sanitized, isRadian)) {
            is EvaluationResult.Success -> res.formatted
            else -> null
        }
    }

    private fun isTrailingOperator(c: Char): Boolean {
        return c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^'
    }

    /**
     * Normalizes the user expression to standard math tokens.
     */
    fun prepareExpression(expr: String): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("π", "PI")
            .replace("e", "E")

        // Insert implicit multiplication:
        // 1) digit followed by ( or function or PI/E
        // 2) ) followed by digit, (, or PI/E
        // 3) PI or E followed by digit or (
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            sb.append(c)

            if (i < s.length - 1) {
                val next = s[i + 1]
                val currentIsDigitOrConst = c.isDigit() || c == '.' || c == ')' || c == 'I' || c == 'E'
                val nextStartsImplicit = next == '(' || next == 'P' || next == 'E' ||
                        next == 's' || next == 'c' || next == 't' || next == 'l' || next == 'a'

                if (c.isDigit() && (next == '(' || nextStartsImplicit)) {
                    sb.append('*')
                } else if (c == ')' && (next.isDigit() || next == '(' || nextStartsImplicit)) {
                    sb.append('*')
                } else if ((c == 'I' || c == 'E') && (next.isDigit() || next == '(')) {
                    sb.append('*')
                }
            }
            i++
        }

        // Auto balance parentheses if needed
        var finalStr = sb.toString()
        val openCount = finalStr.count { it == '(' }
        val closeCount = finalStr.count { it == ')' }
        if (openCount > closeCount) {
            finalStr += ")".repeat(openCount - closeCount)
        }

        return finalStr
    }

    fun formatNumber(value: Double): String {
        // Handle precise zero
        if (abs(value) < 1e-15) return "0"

        // Handle very large or very small numbers with scientific notation
        if (abs(value) >= 1e12 || (abs(value) < 1e-6 && value != 0.0)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(value)
        }

        // Round to 10 decimal places to eliminate floating point issues (e.g. 0.1 + 0.2 = 0.30000000000000004)
        val bd = BigDecimal(value.toString()).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = bd.toPlainString()

        // Format integer part with commas
        val parts = plain.split(".")
        val integerPart = parts[0]
        val formattedInt = try {
            val longVal = integerPart.toLong()
            val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
            df.format(longVal)
        } catch (_: Exception) {
            integerPart
        }

        return if (parts.size > 1 && parts[1].isNotEmpty()) {
            "$formattedInt.${parts[1]}"
        } else {
            formattedInt
        }
    }

    /**
     * Recursive Descent Parser for arithmetic & scientific expressions.
     */
    private class ExpressionParser(val str: String, val isRadian: Boolean) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            return x
        }

        // Expression = Term (+ | - Term)*
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        // Term = Factor (* | / | % Factor)*
        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%'.code) -> {
                        // Modulo operator or percentage
                        val divisor = parseFactor()
                        x %= divisor
                    }
                    else -> return x
                }
            }
        }

        // Factor = (+ | - Factor) | (Primary ^ Factor)
        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x = parsePrimary()

            // Postfix factorial
            while (eat('!'.code)) {
                x = factorial(x)
            }

            // Exponentiation (right-associative)
            if (eat('^'.code)) {
                val exponent = parseFactor()
                x = x.pow(exponent)
            }

            return x
        }

        // Primary = Number | Constant | ( Expression ) | Function( Expression )
        fun parsePrimary(): Double {
            var x: Double
            val startPos = this.pos

            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                val numStr = str.substring(startPos, this.pos)
                x = numStr.toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code || ch >= 'A'.code && ch <= 'Z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code || ch >= 'A'.code && ch <= 'Z'.code) nextChar()
                val func = str.substring(startPos, this.pos)

                if (func == "PI") {
                    x = PI
                } else if (func == "E") {
                    x = Math.E
                } else {
                    // Function call with argument in parentheses
                    if (!eat('('.code)) {
                        throw IllegalArgumentException("Expected '(' after $func")
                    }
                    val arg = parseExpression()
                    eat(')'.code)

                    x = when (func.lowercase()) {
                        "sqrt" -> {
                            if (arg < 0) throw IllegalArgumentException("Negative root")
                            sqrt(arg)
                        }
                        "cbrt" -> cbrt(arg)
                        "sin" -> sin(if (isRadian) arg else Math.toRadians(arg))
                        "cos" -> cos(if (isRadian) arg else Math.toRadians(arg))
                        "tan" -> {
                            val rad = if (isRadian) arg else Math.toRadians(arg)
                            tan(rad)
                        }
                        "asin" -> {
                            val r = asin(arg)
                            if (isRadian) r else Math.toDegrees(r)
                        }
                        "acos" -> {
                            val r = acos(arg)
                            if (isRadian) r else Math.toDegrees(r)
                        }
                        "atan" -> {
                            val r = atan(arg)
                            if (isRadian) r else Math.toDegrees(r)
                        }
                        "ln" -> {
                            if (arg <= 0) throw IllegalArgumentException("Domain error")
                            ln(arg)
                        }
                        "log" -> {
                            if (arg <= 0) throw IllegalArgumentException("Domain error")
                            log10(arg)
                        }
                        "exp" -> exp(arg)
                        "abs" -> abs(arg)
                        else -> throw IllegalArgumentException("Unknown function: $func")
                    }
                }
            } else {
                throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != kotlin.math.floor(n)) {
                throw IllegalArgumentException("Factorial requires non-negative integer")
            }
            if (n > 170) return Double.POSITIVE_INFINITY
            var res = 1.0
            val limit = n.roundToLong()
            for (i in 2..limit) {
                res *= i
            }
            return res
        }
    }
}

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
    object Empty : EvaluationResult()
}
