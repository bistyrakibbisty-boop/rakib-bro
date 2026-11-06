package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    MASS("Weight & Mass"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    SPEED("Speed"),
    STORAGE("Digital Storage")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

object UnitConverter {

    val categories = UnitCategory.values().toList()

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return when (category) {
            UnitCategory.LENGTH -> listOf(
                ConversionUnit("m", "Meters", "m", { it }, { it }),
                ConversionUnit("km", "Kilometers", "km", { it * 1000.0 }, { it / 1000.0 }),
                ConversionUnit("cm", "Centimeters", "cm", { it * 0.01 }, { it / 0.01 }),
                ConversionUnit("mm", "Millimeters", "mm", { it * 0.001 }, { it / 0.001 }),
                ConversionUnit("mi", "Miles", "mi", { it * 1609.344 }, { it / 1609.344 }),
                ConversionUnit("yd", "Yards", "yd", { it * 0.9144 }, { it / 0.9144 }),
                ConversionUnit("ft", "Feet", "ft", { it * 0.3048 }, { it / 0.3048 }),
                ConversionUnit("in", "Inches", "in", { it * 0.0254 }, { it / 0.0254 })
            )
            UnitCategory.MASS -> listOf(
                ConversionUnit("kg", "Kilograms", "kg", { it }, { it }),
                ConversionUnit("g", "Grams", "g", { it * 0.001 }, { it / 0.001 }),
                ConversionUnit("mg", "Milligrams", "mg", { it * 0.000001 }, { it / 0.000001 }),
                ConversionUnit("lb", "Pounds", "lbs", { it * 0.45359237 }, { it / 0.45359237 }),
                ConversionUnit("oz", "Ounces", "oz", { it * 0.028349523125 }, { it / 0.028349523125 }),
                ConversionUnit("ton", "Metric Tons", "t", { it * 1000.0 }, { it / 1000.0 })
            )
            UnitCategory.TEMPERATURE -> listOf(
                ConversionUnit("C", "Celsius", "°C", { it }, { it }),
                ConversionUnit("F", "Fahrenheit", "°F", { (it - 32.0) * 5.0 / 9.0 }, { (it * 9.0 / 5.0) + 32.0 }),
                ConversionUnit("K", "Kelvin", "K", { it - 273.15 }, { it + 273.15 })
            )
            UnitCategory.AREA -> listOf(
                ConversionUnit("sqm", "Square Meters", "m²", { it }, { it }),
                ConversionUnit("sqkm", "Square Kilometers", "km²", { it * 1_000_000.0 }, { it / 1_000_000.0 }),
                ConversionUnit("sqft", "Square Feet", "ft²", { it * 0.092903 }, { it / 0.092903 }),
                ConversionUnit("acre", "Acres", "ac", { it * 4046.856422 }, { it / 4046.856422 }),
                ConversionUnit("ha", "Hectares", "ha", { it * 10000.0 }, { it / 10000.0 })
            )
            UnitCategory.VOLUME -> listOf(
                ConversionUnit("l", "Liters", "L", { it }, { it }),
                ConversionUnit("ml", "Milliliters", "mL", { it * 0.001 }, { it / 0.001 }),
                ConversionUnit("gal", "Gallons (US)", "gal", { it * 3.78541 }, { it / 3.78541 }),
                ConversionUnit("qt", "Quarts (US)", "qt", { it * 0.946353 }, { it / 0.946353 }),
                ConversionUnit("pt", "Pints (US)", "pt", { it * 0.473176 }, { it / 0.473176 }),
                ConversionUnit("cup", "Cups (US)", "cup", { it * 0.236588 }, { it / 0.236588 }),
                ConversionUnit("floz", "Fluid Ounces", "fl oz", { it * 0.0295735 }, { it / 0.0295735 })
            )
            UnitCategory.SPEED -> listOf(
                ConversionUnit("mps", "Meters / Second", "m/s", { it }, { it }),
                ConversionUnit("kmh", "Kilometers / Hour", "km/h", { it / 3.6 }, { it * 3.6 }),
                ConversionUnit("mph", "Miles / Hour", "mph", { it * 0.44704 }, { it / 0.44704 }),
                ConversionUnit("knot", "Knots", "kn", { it * 0.514444 }, { it / 0.514444 })
            )
            UnitCategory.STORAGE -> listOf(
                ConversionUnit("B", "Bytes", "B", { it }, { it }),
                ConversionUnit("KB", "Kilobytes", "KB", { it * 1024.0 }, { it / 1024.0 }),
                ConversionUnit("MB", "Megabytes", "MB", { it * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0) }),
                ConversionUnit("GB", "Gigabytes", "GB", { it * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0) }),
                ConversionUnit("TB", "Terabytes", "TB", { it * 1024.0 * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0 * 1024.0) })
            )
        }
    }

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        val baseValue = from.toBase(value)
        return to.fromBase(baseValue)
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        if (kotlin.math.abs(value) >= 1e9 || (kotlin.math.abs(value) < 1e-4 && value != 0.0)) {
            val df = DecimalFormat("0.####E0", DecimalFormatSymbols(Locale.US))
            return df.format(value)
        }
        val df = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))
        return df.format(value)
    }
}
