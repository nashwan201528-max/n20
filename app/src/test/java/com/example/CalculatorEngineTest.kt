package com.example

import com.example.domain.calculator.EvaluationResult
import com.example.domain.calculator.ExpressionEvaluator
import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testOperatorPrecedence() {
        val result = ExpressionEvaluator.evaluate("2+3*4")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(14.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testParenthesesPrecedence() {
        val result = ExpressionEvaluator.evaluate("(2+3)*4")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(20.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testDivisionByZeroHandledGracefully() {
        val result = ExpressionEvaluator.evaluate("100/0")
        assertTrue(result is EvaluationResult.Error)
        assertEquals("Cannot divide by zero", (result as EvaluationResult.Error).message)
    }

    @Test
    fun testModuloByZeroHandledGracefully() {
        val result = ExpressionEvaluator.evaluate("50%0")
        assertTrue(result is EvaluationResult.Error)
        assertEquals("Cannot divide by zero", (result as EvaluationResult.Error).message)
    }

    @Test
    fun testUnaryMinus() {
        val result = ExpressionEvaluator.evaluate("-5+12")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(7.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testImplicitMultiplication() {
        val result = ExpressionEvaluator.evaluate("2(3+4)")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(14.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testPowerOperator() {
        val result = ExpressionEvaluator.evaluate("2^10")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(1024.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testSquareRoot() {
        val result = ExpressionEvaluator.evaluate("sqrt(144)")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(12.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testNegativeSquareRootThrowsError() {
        val result = ExpressionEvaluator.evaluate("sqrt(-25)")
        assertTrue(result is EvaluationResult.Error)
    }

    @Test
    fun testTrigonometryDegreeMode() {
        val sin90 = ExpressionEvaluator.evaluate("sin(90)", isDegree = true)
        assertTrue(sin90 is EvaluationResult.Success)
        assertEquals(1.0, (sin90 as EvaluationResult.Success).value, 0.0001)

        val cos0 = ExpressionEvaluator.evaluate("cos(0)", isDegree = true)
        assertTrue(cos0 is EvaluationResult.Success)
        assertEquals(1.0, (cos0 as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testFactorial() {
        val result = ExpressionEvaluator.evaluate("fact(5)")
        assertTrue(result is EvaluationResult.Success)
        assertEquals(120.0, (result as EvaluationResult.Success).value, 0.0001)
    }

    @Test
    fun testLiveEvaluationIncompleteExpressionReturnsNull() {
        assertNull(ExpressionEvaluator.evaluateLive("5+"))
        assertNull(ExpressionEvaluator.evaluateLive("12*("))
        assertEquals("17", ExpressionEvaluator.evaluateLive("12+5"))
    }
}
