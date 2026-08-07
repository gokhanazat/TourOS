package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.3.3 Dinamik Fiyatlandırma Kuralı Oluşturma ve Düzenleme Talep Modeli.
 */
@Serializable
data class SaveDynamicPricingRuleRequest(
    @SerialName("rule_id") val ruleId: String? = null,
    @SerialName("rule_name") val ruleName: String = "Yüksek Doluluk Artışı (>80%)",
    val priority: Int = 1,
    val season: String = "HIGH_SEASON",
    @SerialName("min_occupancy_rate") val minOccupancyRate: Double = 80.0,
    @SerialName("agency_tier") val agencyTier: String = "VIP_AGENCY",
    @SerialName("target_country") val targetCountry: String = "GERMANY",
    @SerialName("price_adjustment_percent") val priceAdjustmentPercent: Double = 15.0
)
