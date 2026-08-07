package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.ExchangeRate

/**
 * 3.2.5 Çoklu Para Birimi Çevrim Motoru (TRY, EUR, USD, GBP, AED, RUB).
 */
class CurrencyConverterEngine {

    fun convert(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        rates: List<ExchangeRate>
    ): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount
        if (amount <= 0) return 0.0

        val rateMap = rates.associateBy { it.targetCurrency.uppercase() }

        val fromEffective = if (fromCurrency.equals("TRY", ignoreCase = true)) 1.0 else rateMap[fromCurrency.uppercase()]?.effectiveRate ?: 1.0
        val toEffective = if (toCurrency.equals("TRY", ignoreCase = true)) 1.0 else rateMap[toCurrency.uppercase()]?.effectiveRate ?: 1.0

        val amountInTry = amount * fromEffective
        val result = amountInTry / toEffective
        return (result * 100).toInt() / 100.0
    }
}
