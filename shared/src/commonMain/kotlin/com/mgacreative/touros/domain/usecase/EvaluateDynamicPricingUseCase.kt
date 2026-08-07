package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.DynamicPricingEvaluationResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.3.2 Sezon, Doluluk, Acente ve Ülkeye Göre Kural Motorunu Çalıştırma Use Case.
 */
class EvaluateDynamicPricingUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(
        basePrice: Double,
        season: String,
        occupancyRate: Double,
        agencyTier: String,
        targetCountry: String,
        tenantId: String
    ): Result<DynamicPricingEvaluationResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_base_price", basePrice)
                put("p_season", season)
                put("p_occupancy_rate", occupancyRate)
                put("p_agency_tier", agencyTier)
                put("p_target_country", targetCountry)
            }

            val list = supabaseClient.postgrest.rpc("evaluate_dynamic_pricing_rules", params)
                .decodeList<DynamicPricingEvaluationResult>()

            list.firstOrNull() ?: calculateFallback(basePrice, season, occupancyRate, agencyTier, targetCountry)
        }.recover { calculateFallback(basePrice, season, occupancyRate, agencyTier, targetCountry) }
    }

    private fun calculateFallback(
        basePrice: Double,
        season: String,
        occupancyRate: Double,
        agencyTier: String,
        targetCountry: String
    ): DynamicPricingEvaluationResult {
        var adj = 0.0
        var summary = ""

        if (occupancyRate >= 80.0) {
            adj += 15.0
            summary += " [Öncelik 1: Yüksek Doluluk (+15%)]"
        }
        if (season == "HIGH_SEASON" && agencyTier == "VIP_AGENCY") {
            adj -= 5.0
            summary += " [Öncelik 2: VIP Acente (-5%)]"
        }
        if (targetCountry == "GERMANY") {
            adj += 10.0
            summary += " [Öncelik 3: Almanya Pazarı (+10%)]"
        }

        val finalP = basePrice * (1.0 + (adj / 100.0))
        return DynamicPricingEvaluationResult(
            basePrice = basePrice,
            adjustedPrice = finalP,
            matchedRuleName = "Dinamik Kural Motoru Eşleşmesi",
            matchedPriority = 1,
            totalAdjustmentPercent = adj,
            appliedRulesSummary = if (summary.isBlank()) "Standart Tarife" else summary
        )
    }
}
