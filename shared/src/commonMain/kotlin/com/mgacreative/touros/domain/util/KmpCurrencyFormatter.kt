package com.mgacreative.touros.domain.util

import kotlin.math.abs

/**
 * TourOS Merkezi KMP Para Birimi & Sayı Formatlayıcı.
 * Tüm projede standart binlik ayracı ve 2 basamaklı kuruş formatı sağlar.
 * Örnek: 1037717.47 -> "1,037,717.47" | format(1037717.47, "TRY") -> "₺ 1,037,717.47"
 */
object KmpCurrencyFormatter {

    /**
     * Sadece sayıyı binlik ayracı ile formatlar (Örn: 12345 -> "12.345" | 1037717.47 -> "1.037.717,47")
     */
    fun formatAmount(
        amount: Double,
        decimals: Boolean = true,
        useTurkishSeparators: Boolean = true
    ): String {
        val isNegative = amount < 0
        val absAmount = abs(amount)

        val thousandSep = if (useTurkishSeparators) "." else ","
        val decimalSep = if (useTurkishSeparators) "," else "."

        if (!decimals) {
            val whole = (absAmount + 0.5).toLong()
            val formattedWhole = formatThousands(whole.toString(), thousandSep)
            return if (isNegative) "-$formattedWhole" else formattedWhole
        }

        val totalCents = (absAmount * 100 + 0.5).toLong()
        val integerPart = totalCents / 100
        val decimalPart = (totalCents % 100).toInt()

        val formattedWhole = formatThousands(integerPart.toString(), thousandSep)
        val formattedDec = if (decimalPart < 10) "0$decimalPart" else "$decimalPart"
        val sign = if (isNegative) "-" else ""

        return "$sign$formattedWhole$decimalSep$formattedDec"
    }

    fun formatAmount(
        amount: Int,
        decimals: Boolean = false,
        useTurkishSeparators: Boolean = true
    ): String = formatAmount(amount.toDouble(), decimals, useTurkishSeparators)

    fun formatAmount(
        amount: Long,
        decimals: Boolean = false,
        useTurkishSeparators: Boolean = true
    ): String = formatAmount(amount.toDouble(), decimals, useTurkishSeparators)

    /**
     * Para birimi sembolü ile birlikte formatlar (Örn: "₺ 12.345" veya "12.345 ₺")
     */
    fun format(
        amount: Double,
        currencyCode: String = "TRY",
        decimals: Boolean = true,
        useTurkishSeparators: Boolean = true
    ): String {
        val symbol = getSymbol(currencyCode)
        val formatted = formatAmount(amount, decimals, useTurkishSeparators)
        return "$symbol $formatted"
    }

    fun format(
        amount: Int,
        currencyCode: String = "TRY",
        decimals: Boolean = false,
        useTurkishSeparators: Boolean = true
    ): String = format(amount.toDouble(), currencyCode, decimals, useTurkishSeparators)

    fun format(
        amount: Long,
        currencyCode: String = "TRY",
        decimals: Boolean = false,
        useTurkishSeparators: Boolean = true
    ): String = format(amount.toDouble(), currencyCode, decimals, useTurkishSeparators)

    fun getSymbol(currencyCode: String): String {
        return when (currencyCode.uppercase().trim()) {
            "TRY", "TL" -> "₺"
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            "AED" -> "د.إ"
            "RUB" -> "₽"
            else -> currencyCode.uppercase()
        }
    }

    private fun formatThousands(digits: String, separator: String): String {
        val len = digits.length
        val sb = StringBuilder()
        for (i in 0 until len) {
            sb.append(digits[i])
            val remaining = len - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append(separator)
            }
        }
        return sb.toString()
    }
}
