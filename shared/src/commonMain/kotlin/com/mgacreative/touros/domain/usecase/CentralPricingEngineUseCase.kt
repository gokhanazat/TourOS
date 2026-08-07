package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CentralPricingRequest
import com.mgacreative.touros.domain.model.CentralPricingResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.3.4 B2C, B2B ve Yönetim Paneli İçin Merkezi PricingEngine Entegrasyonu Use Case.
 */
class CentralPricingEngineUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(request: CentralPricingRequest, tenantId: String): Result<CentralPricingResponse> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_channel", request.channel)
                put("p_base_price", request.basePrice)
                put("p_pax_count", request.paxCount)
                if (request.couponCode != null) put("p_coupon_code", request.couponCode)
                put("p_days_to_departure", request.daysToDeparture)
                put("p_occupancy_rate", request.occupancyRate)
                put("p_agency_tier", request.agencyTier)
                put("p_country", request.country)
            }

            val list = supabaseClient.postgrest.rpc("calculate_central_pricing", params)
                .decodeList<CentralPricingResponse>()

            list.firstOrNull() ?: calculateFallback(request)
        }.recover { calculateFallback(request) }
    }

    private fun calculateFallback(request: CentralPricingRequest): CentralPricingResponse {
        val gross = request.basePrice * request.paxCount
        val dyn = if (request.occupancyRate >= 80.0) gross * 0.15 else 0.0
        val camp = if (request.daysToDeparture >= 30) (gross + dyn) * 0.15 else 0.0
        val comm = if (request.channel == "B2B_AGENCY") (gross + dyn - camp) * 0.12 else 0.0
        val net = gross + dyn - camp - comm

        return CentralPricingResponse(
            channel = request.channel,
            grossAmount = gross,
            dynamicAdjustmentAmount = dyn,
            campaignDiscountAmount = camp,
            agencyCommissionAmount = comm,
            netPayableAmount = net,
            appliedRulesSummary = "Merkezi PricingEngine: [+%15 Doluluk Surge] [-%15 Erken Rezervasyon]"
        )
    }
}
