package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.4.2 Para Birimi Modeli.
 */
@Serializable
data class CurrencyItem(
    val code: String = "TRY",
    val symbol: String = "₺",
    @SerialName("rate_to_try") val rateToTry: Double = 1.0
)

/**
 * 4.4.2 Para Birimi Çeviri ve Formatlama Sonuç Modeli.
 */
@Serializable
data class CurrencyConversionResult(
    @SerialName("from_currency") val fromCurrency: String = "TRY",
    @SerialName("to_currency") val toCurrency: String = "EUR",
    @SerialName("original_amount") val originalAmount: Double = 1000.0,
    @SerialName("converted_amount") val convertedAmount: Double = 25.97,
    @SerialName("exchange_rate") val exchangeRate: Double = 0.02597,
    @SerialName("formatted_result") val formattedResult: String = "€ 25.97"
)
