package com.example.math

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object MathEvaluator {

    private const val PI_VAL = Math.PI
    private const val E_VAL = Math.E
    private const val PHI_VAL = 1.618033988749895

    fun evaluate(expression: String, isDegreeMode: Boolean = true): EvaluationResult {
        if (expression.isBlank()) {
            return EvaluationResult.Success(0.0, "0")
        }

        return try {
            val sanitized = sanitize(expression)
            val tokens = tokenize(sanitized)
            val rpn = toRpn(tokens)
            val result = evalRpn(rpn, isDegreeMode)
            if (result.isNaN()) {
                EvaluationResult.Error("Undefined result")
            } else if (result.isInfinite()) {
                EvaluationResult.Error("Cannot divide by zero")
            } else {
                EvaluationResult.Success(result, formatResult(result))
            }
        } catch (e: Exception) {
            EvaluationResult.Error(e.message ?: "Syntax Error")
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("φ", "phi")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace(" ", "")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            // Number or decimal
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < len && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || expr[i] == 'e')) {
                    if ((expr[i] == 'E' || expr[i] == 'e') && i + 1 < len && (expr[i + 1] == '+' || expr[i + 1] == '-')) {
                        sb.append(expr[i])
                        i++
                    }
                    sb.append(expr[i])
                    i++
                }
                // Check implicit multiplication with upcoming parenthesis or function
                tokens.add(sb.toString())
                if (i < len && (expr[i] == '(' || expr[i].isLetter())) {
                    tokens.add("*")
                }
                continue
            }

            // Word: function or constant
            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < len && expr[i].isLetter()) {
                    sb.append(expr[i])
                    i++
                }
                val word = sb.toString()
                when (word) {
                    "pi" -> {
                        tokens.add(PI_VAL.toString())
                        if (i < len && (expr[i] == '(' || expr[i].isLetter() || expr[i].isDigit())) {
                            tokens.add("*")
                        }
                    }
                    "e" -> {
                        tokens.add(E_VAL.toString())
                        if (i < len && (expr[i] == '(' || expr[i].isLetter() || expr[i].isDigit())) {
                            tokens.add("*")
                        }
                    }
                    "phi" -> {
                        tokens.add(PHI_VAL.toString())
                        if (i < len && (expr[i] == '(' || expr[i].isLetter() || expr[i].isDigit())) {
                            tokens.add("*")
                        }
                    }
                    else -> {
                        tokens.add(word)
                    }
                }
                continue
            }

            // Unary minus vs binary minus
            if (c == '-') {
                val prev = tokens.lastOrNull()
                val isUnary = prev == null || prev == "(" || isOperator(prev)
                if (isUnary) {
                    tokens.add("neg")
                } else {
                    tokens.add("-")
                }
                i++
                continue
            }

            // Operator or Parenthesis
            when (c) {
                '+', '*', '/', '^', '%', '!' -> {
                    tokens.add(c.toString())
                    i++
                }
                '(' -> {
                    // Check implicit multiplication: e.g. 5(3) or )(
                    val prev = tokens.lastOrNull()
                    if (prev != null && (!isOperator(prev) && prev != "(" && !isFunction(prev) && prev != "neg")) {
                        tokens.add("*")
                    }
                    tokens.add("(")
                    i++
                }
                ')' -> {
                    tokens.add(")")
                    i++
                    // Check implicit multiplication: e.g. (2)3 or (2)(3)
                    if (i < len && (expr[i].isDigit() || expr[i] == '(' || expr[i].isLetter())) {
                        tokens.add("*")
                    }
                }
                else -> {
                    i++
                }
            }
        }

        // Auto-close missing right parentheses
        var openCount = tokens.count { it == "(" }
        val closeCount = tokens.count { it == ")" }
        while (openCount > closeCount) {
            tokens.add(")")
            openCount--
        }

        return tokens
    }

    private fun isOperator(t: String): Boolean =
        t == "+" || t == "-" || t == "*" || t == "/" || t == "^" || t == "%" || t == "!"

    private fun isFunction(t: String): Boolean =
        t in setOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "ln", "log", "log2",
            "sqrt", "cbrt", "abs", "round", "floor", "ceil"
        )

    private fun precedence(op: String): Int = when (op) {
        "!" -> 5
        "neg" -> 4
        "^" -> 4
        "*", "/", "%" -> 3
        "+", "-" -> 2
        else -> 0
    }

    private fun isRightAssociative(op: String): Boolean = op == "^" || op == "neg"

    private fun toRpn(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                isFunction(token) -> stack.addLast(token)
                token == "(" -> stack.addLast(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty() && stack.last() == "(") {
                        stack.removeLast()
                    }
                    if (stack.isNotEmpty() && isFunction(stack.last())) {
                        output.add(stack.removeLast())
                    }
                }
                isOperator(token) || token == "neg" -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top == "(") break
                        val p1 = precedence(token)
                        val p2 = precedence(top)
                        if ((!isRightAssociative(token) && p1 <= p2) || (isRightAssociative(token) && p1 < p2)) {
                            output.add(stack.removeLast())
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evalRpn(rpn: List<String>, isDegreeMode: Boolean): Double {
        val stack = ArrayDeque<Double>()

        fun toAngle(v: Double): Double = if (isDegreeMode) Math.toRadians(v) else v
        fun fromAngle(v: Double): Double = if (isDegreeMode) Math.toDegrees(v) else v

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addLast(num)
                continue
            }

            when (token) {
                "neg" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(-a)
                }
                "!" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(factorial(a))
                }
                "+" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a + b)
                }
                "-" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a - b)
                }
                "*" -> {
                    val b = stack.removeLastOrNull() ?: 1.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a * b)
                }
                "/" -> {
                    val b = stack.removeLastOrNull() ?: 1.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (b == 0.0) throw ArithmeticException("Division by zero")
                    stack.addLast(a / b)
                }
                "%" -> {
                    val b = stack.removeLastOrNull() ?: 1.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a % b)
                }
                "^" -> {
                    val b = stack.removeLastOrNull() ?: 1.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a.pow(b))
                }
                "sin" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(sin(toAngle(a)))
                }
                "cos" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(cos(toAngle(a)))
                }
                "tan" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(tan(toAngle(a)))
                }
                "asin" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(fromAngle(asin(a)))
                }
                "acos" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(fromAngle(acos(a)))
                }
                "atan" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(fromAngle(atan(a)))
                }
                "sinh" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(sinh(a))
                }
                "cosh" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(cosh(a))
                }
                "tanh" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(tanh(a))
                }
                "ln" -> {
                    val a = stack.removeLastOrNull() ?: 1.0
                    stack.addLast(ln(a))
                }
                "log" -> {
                    val a = stack.removeLastOrNull() ?: 1.0
                    stack.addLast(log10(a))
                }
                "log2" -> {
                    val a = stack.removeLastOrNull() ?: 1.0
                    stack.addLast(log2(a))
                }
                "sqrt" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a < 0) throw IllegalArgumentException("Negative square root")
                    stack.addLast(sqrt(a))
                }
                "cbrt" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(cbrt(a))
                }
                "abs" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(abs(a))
                }
                else -> throw IllegalArgumentException("Unknown token: $token")
            }
        }

        return stack.lastOrNull() ?: 0.0
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n > 170 || n != floor(n)) {
            throw IllegalArgumentException("Factorial undefined for $n")
        }
        val intN = n.toLong()
        var result = 1.0
        for (i in 2..intN) {
            result *= i
        }
        return result
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        // Round tiny floating inaccuracies like 0.00000000000000001 -> 0
        val cleanValue = if (abs(value) < 1e-12) 0.0 else value

        if (cleanValue == floor(cleanValue) && abs(cleanValue) < 1e14) {
            return cleanValue.toLong().toString()
        }

        val symbols = DecimalFormatSymbols(Locale.US)
        return if (abs(cleanValue) >= 1e10 || (abs(cleanValue) < 1e-4 && cleanValue != 0.0)) {
            DecimalFormat("0.######E0", symbols).format(cleanValue)
        } else {
            val formatted = DecimalFormat("#.##########", symbols).format(cleanValue)
            if (formatted == "-0") "0" else formatted
        }
    }
}

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
}
