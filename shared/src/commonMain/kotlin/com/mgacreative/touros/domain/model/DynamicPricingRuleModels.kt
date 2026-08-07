package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.3.2 Dinamik Fiyatlandırma Kural Modeli.
 */
@Serializable
data class DynamicPricingRule(
    @SerialName("rule_id") val ruleId: String = "r1",
    @SerialName("rule_name") val ruleName: String = "Yüksek Doluluk Artışı (>80%)",
    val priority: Int = 1,
    val season: String = "ALL", // HIGH_SEASON, MID_SEASON, LOW_SEASON, ALL
    @SerialName("min_occupancy_rate") val minOccupancyRate: Double = 80.0,
    @SerialName("agency_tier") val agencyTier: String = "ALL", // VIP_AGENCY, REGULAR_AGENCY, ALL
    @SerialName("target_country") val targetCountry: String = "ALL", // GERMANY, JAPAN, DOMESTIC, ALL
    @SerialName("price_adjustment_percent") val priceAdjustmentPercent: Double = 15.0,
    @SerialName("is_active") val isActive: Boolean = true
)

/**
 * 4.3.2 Kural Motoru Fiyat Değerlendirme Sonuç Modeli.
 */
@Serializable
data class DynamicPricingEvaluationResult(
    @SerialName("base_price") val basePrice: Double = 2500.0,
    @SerialName("adjusted_price") val adjustedPrice: Double = 3125.0,
    @SerialName("matched_rule_name") val matchedRuleName: String = "Yüksek Doluluk Artışı (>80%)",
    @SerialName("matched_priority") val matchedPriority: Int = 1,
    @SerialName("total_adjustment_percent") val totalAdjustmentPercent: Double = 25.0,
    @SerialName("applied_rules_summary") val appliedRulesSummary: String = "[Öncelik 1: Yüksek Doluluk (+15%)] [Öncelik 3: Almanya Pazarı (+10%)]"
)
