package com.example

import com.example.domain.financial.UnitCategory
import com.example.domain.financial.UnitConverter
import org.junit.Assert.*
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testDistanceConversions() {
        val km = UnitConverter.distanceUnits.first { it.code == "km" }
        val m = UnitConverter.distanceUnits.first { it.code == "m" }
        val mi = UnitConverter.distanceUnits.first { it.code == "mi" }
        val inch = UnitConverter.distanceUnits.first { it.code == "in" }
        val cm = UnitConverter.distanceUnits.first { it.code == "cm" }

        // 1 km = 1000 m
        val res1 = UnitConverter.convert(1.0, km, m)
        assertEquals(1000.0, res1.outputValue, 0.001)

        // 1 mi = 1.609344 km
        val res2 = UnitConverter.convert(1.0, mi, km)
        assertEquals(1.609344, res2.outputValue, 0.0001)

        // 1 in = 2.54 cm
        val res3 = UnitConverter.convert(1.0, inch, cm)
        assertEquals(2.54, res3.outputValue, 0.0001)
    }

    @Test
    fun testWeightConversions() {
        val kg = UnitConverter.weightUnits.first { it.code == "kg" }
        val g = UnitConverter.weightUnits.first { it.code == "g" }
        val lb = UnitConverter.weightUnits.first { it.code == "lb" }
        val oz = UnitConverter.weightUnits.first { it.code == "oz" }

        // 1 kg = 1000 g
        val res1 = UnitConverter.convert(1.0, kg, g)
        assertEquals(1000.0, res1.outputValue, 0.001)

        // 1 lb = 0.453592 kg
        val res2 = UnitConverter.convert(1.0, lb, kg)
        assertEquals(0.453592, res2.outputValue, 0.001)

        // 16 oz = 1 lb
        val res3 = UnitConverter.convert(16.0, oz, lb)
        assertEquals(1.0, res3.outputValue, 0.001)
    }

    @Test
    fun testTemperatureConversions() {
        val c = UnitConverter.temperatureUnits.first { it.code == "C" }
        val f = UnitConverter.temperatureUnits.first { it.code == "F" }
        val k = UnitConverter.temperatureUnits.first { it.code == "K" }

        // 0 °C = 32 °F
        val res0 = UnitConverter.convert(0.0, c, f)
        assertEquals(32.0, res0.outputValue, 0.001)

        // 100 °C = 212 °F
        val res100 = UnitConverter.convert(100.0, c, f)
        assertEquals(212.0, res100.outputValue, 0.001)

        // 0 °C = 273.15 K
        val resK = UnitConverter.convert(0.0, c, k)
        assertEquals(273.15, resK.outputValue, 0.001)

        // 98.6 °F = 37 °C
        val resBody = UnitConverter.convert(98.6, f, c)
        assertEquals(37.0, resBody.outputValue, 0.01)

        // -40 °C = -40 °F
        val resNeg40 = UnitConverter.convert(-40.0, c, f)
        assertEquals(-40.0, resNeg40.outputValue, 0.001)
    }

    @Test
    fun testQuickEquivalentsPopulated() {
        val km = UnitConverter.distanceUnits.first { it.code == "km" }
        val mi = UnitConverter.distanceUnits.first { it.code == "mi" }
        val result = UnitConverter.convert(10.0, km, mi)

        assertTrue(result.quickEquivalents.isNotEmpty())
        assertEquals(UnitCategory.DISTANCE, result.category)
        assertTrue(result.formulaExplanation.isNotBlank())
    }
}
