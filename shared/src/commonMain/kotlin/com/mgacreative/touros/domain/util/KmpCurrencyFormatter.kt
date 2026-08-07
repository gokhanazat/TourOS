package com.mgacreative.touros.domain.util

/**
 * 4.4.2 Pure Kotlin Multiplatform (KMP) Currency Formatter.
 * TRY, EUR, USD, GBP, AED, RUB destekler. String.format() kullanmaz.
 */
object KmpCurrencyFormatter {

    fun format(amount: Double, currencyCode: String): String {
        val symbol = getSymbol(currencyCode)
        val wholePart = amount.toLong()
        val decVal = kotlin.math.abs(((amount - wholePart) * 100).toLong()).coerceIn(0, 99)
        val decStr = if (decVal < 10) "0$decVal" else "$decVal"

        val formattedWhole = formatWholeWithSeparators(wholePart)

        return "$symbol $formattedWhole.$decStr"
    }

    fun getSymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "TRY" -> "₺"
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            "AED" -> "د.إ"
            "RUB" -> "₽"
            else -> currencyCode.uppercase()
        }
    }

    private fun formatWholeWithSeparators(number: Long): String {
        val str = number.toString()
        val isNegative = str.startsWith("-")
        val cleanStr = if (isNegative) str.substring(1) else str

        val sb = StringBuilder()
        var count = 0
        for (i in cleanStr.length - 1 downTo 0) {
            sb.append(cleanStr[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(".")
            }
        }

        if (isNegative) sb.append("-")
        return sb.reverse().toString()
    }
}
