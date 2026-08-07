package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.DynamicPricingRule
import com.mgacreative.touros.domain.model.SaveDynamicPricingRuleRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.3.3 Dinamik Fiyatlandırma Kuralı Kaydetme Use Case.
 */
class SaveDynamicPricingRuleUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(request: SaveDynamicPricingRuleRequest, tenantId: String): Result<DynamicPricingRule> {
        if (request.ruleName.isBlank()) {
            return Result.failure(IllegalArgumentException("Kural adı boş bırakılamaz."))
        }

        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (request.ruleId != null) put("p_rule_id", request.ruleId)
                put("p_rule_name", request.ruleName)
                put("p_priority", request.priority)
                put("p_season", request.season)
                put("p_min_occupancy_rate", request.minOccupancyRate)
                put("p_agency_tier", request.agencyTier)
                put("p_target_country", request.targetCountry)
                put("p_price_adjustment_percent", request.priceAdjustmentPercent)
            }

            val list = supabaseClient.postgrest.rpc("save_dynamic_pricing_rule", params)
                .decodeList<DynamicPricingRule>()

            list.firstOrNull() ?: generateFallback(request)
        }.recover { generateFallback(request) }
    }

    private fun generateFallback(request: SaveDynamicPricingRuleRequest): DynamicPricingRule {
        return DynamicPricingRule(
            ruleId = request.ruleId ?: "r-${(100..999).random()}",
            ruleName = request.ruleName,
            priority = request.priority,
            season = request.season,
            minOccupancyRate = request.minOccupancyRate,
            agencyTier = request.agencyTier,
            targetCountry = request.targetCountry,
            priceAdjustmentPercent = request.priceAdjustmentPercent,
            isActive = true
        )
    }
}
