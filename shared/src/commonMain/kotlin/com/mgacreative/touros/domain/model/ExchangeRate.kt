package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.2.5 Döviz Kuru Domain Modeli (TRY, EUR, USD, GBP, AED, RUB).
 */
@Serializable
data class ExchangeRate(
    val id: String = "",
    @SerialName("base_currency") val baseCurrency: String = "TRY",
    @SerialName("target_currency") val targetCurrency: String = "USD", // EUR, USD, GBP, AED, RUB
    @SerialName("buying_rate") val buyingRate: Double = 0.0,
    @SerialName("selling_rate") val sellingRate: Double = 0.0,
    @SerialName("effective_rate") val effectiveRate: Double = 0.0,
    @SerialName("rate_date") val rateDate: String = "",
    val source: String = "TCMB", // TCMB, ECB, MANUAL
    @SerialName("tenant_id") val tenantId: String = ""
)
