package com.example.domain.financial

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val displayName: String, val iconName: String) {
    DISTANCE("Distance", "📏"),
    WEIGHT("Weight / Mass", "⚖️"),
    TEMPERATURE("Temperature", "🌡️")
}

data class ConversionUnit(
    val code: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    val factorToBase: Double = 1.0 // Base: Meter for distance, Gram for weight
)

data class UnitConversionResult(
    val category: UnitCategory,
    val fromUnit: ConversionUnit,
    val toUnit: ConversionUnit,
    val inputValue: Double,
    val outputValue: Double,
    val formulaExplanation: String,
    val quickEquivalents: List<Pair<ConversionUnit, Double>>
)

object UnitConverter {

    private val symbols = DecimalFormatSymbols(Locale.US)
    val standardFormat = DecimalFormat("#,##0.######", symbols)
    val scientificFormat = DecimalFormat("0.######E0", symbols)

    fun formatUnitValue(value: Double): String {
        if (value.isNaN()) return "0"
        val absVal = kotlin.math.abs(value)
        return if (absVal != 0.0 && (absVal >= 1e9 || absVal < 1e-4)) {
            scientificFormat.format(value)
        } else {
            standardFormat.format(value)
        }
    }

    val distanceUnits = listOf(
        ConversionUnit("mm", "Millimeter", "mm", UnitCategory.DISTANCE, 0.001),
        ConversionUnit("cm", "Centimeter", "cm", UnitCategory.DISTANCE, 0.01),
        ConversionUnit("m", "Meter", "m", UnitCategory.DISTANCE, 1.0),
        ConversionUnit("km", "Kilometer", "km", UnitCategory.DISTANCE, 1000.0),
        ConversionUnit("in", "Inch", "in", UnitCategory.DISTANCE, 0.0254),
        ConversionUnit("ft", "Foot", "ft", UnitCategory.DISTANCE, 0.3048),
        ConversionUnit("yd", "Yard", "yd", UnitCategory.DISTANCE, 0.9144),
        ConversionUnit("mi", "Mile", "mi", UnitCategory.DISTANCE, 1609.344),
        ConversionUnit("nm", "Nautical Mile", "NM", UnitCategory.DISTANCE, 1852.0)
    )

    val weightUnits = listOf(
        ConversionUnit("mg", "Milligram", "mg", UnitCategory.WEIGHT, 0.001),
        ConversionUnit("g", "Gram", "g", UnitCategory.WEIGHT, 1.0),
        ConversionUnit("kg", "Kilogram", "kg", UnitCategory.WEIGHT, 1000.0),
        ConversionUnit("t", "Metric Ton", "t", UnitCategory.WEIGHT, 1000000.0),
        ConversionUnit("oz", "Ounce", "oz", UnitCategory.WEIGHT, 28.349523125),
        ConversionUnit("lb", "Pound", "lb", UnitCategory.WEIGHT, 453.59237),
        ConversionUnit("st", "Stone", "st", UnitCategory.WEIGHT, 6350.29318)
    )

    val temperatureUnits = listOf(
        ConversionUnit("C", "Celsius", "°C", UnitCategory.TEMPERATURE),
        ConversionUnit("F", "Fahrenheit", "°F", UnitCategory.TEMPERATURE),
        ConversionUnit("K", "Kelvin", "K", UnitCategory.TEMPERATURE)
    )

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return when (category) {
            UnitCategory.DISTANCE -> distanceUnits
            UnitCategory.WEIGHT -> weightUnits
            UnitCategory.TEMPERATURE -> temperatureUnits
        }
    }

    fun convert(
        value: Double,
        from: ConversionUnit,
        to: ConversionUnit
    ): UnitConversionResult {
        val safeInput = if (value.isNaN()) 0.0 else value

        val output = if (from.category == UnitCategory.TEMPERATURE) {
            convertTemperature(safeInput, from.code, to.code)
        } else {
            // Convert to base unit, then convert to target unit
            val baseValue = safeInput * from.factorToBase
            baseValue / to.factorToBase
        }

        val explanation = when (from.category) {
            UnitCategory.TEMPERATURE -> {
                when {
                    from.code == to.code -> "1 ${from.symbol} = 1 ${to.symbol}"
                    from.code == "C" && to.code == "F" -> "(${formatUnitValue(safeInput)} × 9/5) + 32 = ${formatUnitValue(output)} °F"
                    from.code == "F" && to.code == "C" -> "(${formatUnitValue(safeInput)} - 32) × 5/9 = ${formatUnitValue(output)} °C"
                    from.code == "C" && to.code == "K" -> "${formatUnitValue(safeInput)} + 273.15 = ${formatUnitValue(output)} K"
                    from.code == "K" && to.code == "C" -> "${formatUnitValue(safeInput)} - 273.15 = ${formatUnitValue(output)} °C"
                    from.code == "F" && to.code == "K" -> "(${formatUnitValue(safeInput)} - 32) × 5/9 + 273.15 = ${formatUnitValue(output)} K"
                    from.code == "K" && to.code == "F" -> "(${formatUnitValue(safeInput)} - 273.15) × 9/5 + 32 = ${formatUnitValue(output)} °F"
                    else -> ""
                }
            }
            else -> {
                val rate = from.factorToBase / to.factorToBase
                "1 ${from.symbol} = ${formatUnitValue(rate)} ${to.symbol}"
            }
        }

        // Quick equivalents across other units of same category
        val allUnits = getUnitsForCategory(from.category)
        val equivalents = allUnits
            .filter { it.code != from.code }
            .map { otherUnit ->
                val eqVal = if (from.category == UnitCategory.TEMPERATURE) {
                    convertTemperature(safeInput, from.code, otherUnit.code)
                } else {
                    (safeInput * from.factorToBase) / otherUnit.factorToBase
                }
                otherUnit to eqVal
            }

        return UnitConversionResult(
            category = from.category,
            fromUnit = from,
            toUnit = to,
            inputValue = safeInput,
            outputValue = output,
            formulaExplanation = explanation,
            quickEquivalents = equivalents
        )
    }

    private fun convertTemperature(value: Double, fromCode: String, toCode: String): Double {
        if (fromCode == toCode) return value
        // Convert to Celsius first
        val celsius = when (fromCode) {
            "C" -> value
            "F" -> (value - 32.0) * (5.0 / 9.0)
            "K" -> value - 273.15
            else -> value
        }
        // Convert Celsius to target
        return when (toCode) {
            "C" -> celsius
            "F" -> (celsius * (9.0 / 5.0)) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }
}
