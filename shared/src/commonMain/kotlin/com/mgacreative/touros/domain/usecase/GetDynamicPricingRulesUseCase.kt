package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.DynamicPricingRule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.3.2 Öncelikli Dinamik Fiyatlandırma Kurallarını Getirme Use Case.
 */
class GetDynamicPricingRulesUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String): Result<List<DynamicPricingRule>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
            }

            val list = supabaseClient.postgrest.rpc("get_dynamic_pricing_rules", params)
                .decodeList<DynamicPricingRule>()

            if (list.isEmpty()) getFallbackRules() else list
        }.recover { getFallbackRules() }
    }

    private fun getFallbackRules(): List<DynamicPricingRule> {
        return listOf(
            DynamicPricingRule("r1", "Yüksek Doluluk Artışı (>80%)", 1, "ALL", 80.0, "ALL", "ALL", 15.0, true),
            DynamicPricingRule("r2", "Yüksek Sezon & VIP Acente İndirimi", 2, "HIGH_SEASON", 0.0, "VIP_AGENCY", "ALL", -5.0, true),
            DynamicPricingRule("r3", "Almanya / AB Pazarı Özel Tarife", 3, "ALL", 0.0, "ALL", "GERMANY", 10.0, true),
            DynamicPricingRule("r4", "Düşük Sezon Promosyon Fiyatı", 4, "LOW_SEASON", 0.0, "ALL", "ALL", -12.0, true)
        )
    }
}
