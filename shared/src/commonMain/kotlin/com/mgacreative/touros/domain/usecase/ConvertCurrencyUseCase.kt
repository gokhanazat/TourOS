package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CurrencyConversionResult
import com.mgacreative.touros.domain.util.KmpCurrencyFormatter
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.4.2 TRY, EUR, USD, GBP, AED, RUB Arasında Anlık Çeviri ve Formatlama Use Case.
 */
class ConvertCurrencyUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        tenantId: String
    ): Result<CurrencyConversionResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_amount", amount)
                put("p_from_currency", fromCurrency)
                put("p_to_currency", toCurrency)
            }

            val list = supabaseClient.postgrest.rpc("convert_and_format_currency", params)
                .decodeList<CurrencyConversionResult>()

            list.firstOrNull() ?: calculateFallback(amount, fromCurrency, toCurrency)
        }.recover { calculateFallback(amount, fromCurrency, toCurrency) }
    }

    private fun calculateFallback(amount: Double, fromCode: String, toCode: String): CurrencyConversionResult {
        val rateMap = mapOf(
            "TRY" to 1.0,
            "EUR" to 38.50,
            "USD" to 34.20,
            "GBP" to 45.80,
            "AED" to 9.31,
            "RUB" to 0.38
        )

        val fromRate = rateMap[fromCode.uppercase()] ?: 1.0
        val toRate = rateMap[toCode.uppercase()] ?: 1.0
        val rate = fromRate / toRate
        val converted = amount * rate
        val formatted = KmpCurrencyFormatter.format(converted, toCode)

        return CurrencyConversionResult(
            fromCurrency = fromCode,
            toCurrency = toCode,
            originalAmount = amount,
            convertedAmount = converted,
            exchangeRate = rate,
            formattedResult = formatted
        )
    }
}
