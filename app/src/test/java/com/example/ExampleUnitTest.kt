package com.example

import com.example.math.EvaluationResult
import com.example.math.MathEvaluator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun basicArithmetic_isCorrect() {
        val result = MathEvaluator.evaluate("2 + 3 * 4")
        assertTrue(result is EvaluationResult.Success)
        assertEquals("14", (result as EvaluationResult.Success).formatted)
    }

    @Test
    fun trigonometryDegree_isCorrect() {
        val result = MathEvaluator.evaluate("sin(30)", isDegreeMode = true)
        assertTrue(result is EvaluationResult.Success)
        assertEquals("0.5", (result as EvaluationResult.Success).formatted)
    }

    @Test
    fun powerAndRoots_isCorrect() {
        val power = MathEvaluator.evaluate("2^3")
        assertTrue(power is EvaluationResult.Success)
        assertEquals("8", (power as EvaluationResult.Success).formatted)

        val root = MathEvaluator.evaluate("sqrt(16)")
        assertTrue(root is EvaluationResult.Success)
        assertEquals("4", (root as EvaluationResult.Success).formatted)
    }

    @Test
    fun factorial_isCorrect() {
        val fact = MathEvaluator.evaluate("5!")
        assertTrue(fact is EvaluationResult.Success)
        assertEquals("120", (fact as EvaluationResult.Success).formatted)
    }

    @Test
    fun implicitMultiplication_isCorrect() {
        val implicit = MathEvaluator.evaluate("3(4 + 5)")
        assertTrue(implicit is EvaluationResult.Success)
        assertEquals("27", (implicit as EvaluationResult.Success).formatted)
    }
}
