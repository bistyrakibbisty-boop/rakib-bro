package com.example

import com.example.model.CalculatorEngine
import com.example.model.EvaluationResult
import com.example.model.TipCalculation
import com.example.model.UnitCategory
import com.example.model.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun basicArithmetic_isCorrect() {
        val result1 = CalculatorEngine.evaluate("2+3")
        assertTrue(result1 is EvaluationResult.Success)
        assertEquals("5", (result1 as EvaluationResult.Success).formatted)

        val result2 = CalculatorEngine.evaluate("10-4")
        assertTrue(result2 is EvaluationResult.Success)
        assertEquals("6", (result2 as EvaluationResult.Success).formatted)

        val result3 = CalculatorEngine.evaluate("6*7")
        assertTrue(result3 is EvaluationResult.Success)
        assertEquals("42", (result3 as EvaluationResult.Success).formatted)

        val result4 = CalculatorEngine.evaluate("15/3")
        assertTrue(result4 is EvaluationResult.Success)
        assertEquals("5", (result4 as EvaluationResult.Success).formatted)
    }

    @Test
    fun operatorPrecedence_andParentheses() {
        val result = CalculatorEngine.evaluate("2+3*4")
        assertTrue(result is EvaluationResult.Success)
        assertEquals("14", (result as EvaluationResult.Success).formatted)

        val resultParen = CalculatorEngine.evaluate("(2+3)*4")
        assertTrue(resultParen is EvaluationResult.Success)
        assertEquals("20", (resultParen as EvaluationResult.Success).formatted)
    }

    @Test
    fun scientificFunctions_areCorrect() {
        val sqrtResult = CalculatorEngine.evaluate("sqrt(16)")
        assertTrue(sqrtResult is EvaluationResult.Success)
        assertEquals("4", (sqrtResult as EvaluationResult.Success).formatted)

        val powerResult = CalculatorEngine.evaluate("2^3")
        assertTrue(powerResult is EvaluationResult.Success)
        assertEquals("8", (powerResult as EvaluationResult.Success).formatted)

        val sinResult = CalculatorEngine.evaluate("sin(90)", isRadian = false)
        assertTrue(sinResult is EvaluationResult.Success)
        assertEquals("1", (sinResult as EvaluationResult.Success).formatted)
    }

    @Test
    fun unitConverter_convertsCorrectly() {
        val lengthUnits = UnitConverter.getUnitsForCategory(UnitCategory.LENGTH)
        val meter = lengthUnits.first { it.id == "m" }
        val km = lengthUnits.first { it.id == "km" }

        val converted = UnitConverter.convert(1000.0, meter, km)
        assertEquals(1.0, converted, 0.0001)
    }

    @Test
    fun tipCalculator_computesCorrectly() {
        val tip = TipCalculation(
            billAmount = 100.0,
            tipPercentage = 20.0,
            splitCount = 2,
            roundUpTotal = false
        )
        assertEquals(20.0, tip.tipAmount, 0.001)
        assertEquals(120.0, tip.totalAmount, 0.001)
        assertEquals(60.0, tip.totalPerPerson, 0.001)
        assertEquals(10.0, tip.tipPerPerson, 0.001)
    }
}
