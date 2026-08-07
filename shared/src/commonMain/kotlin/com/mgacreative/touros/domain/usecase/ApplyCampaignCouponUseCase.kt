package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CampaignCouponCalculationResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.3.1 Kampanya, Erken Rezervasyon ve Kupon İndirimi Uygulama Use Case.
 */
class ApplyCampaignCouponUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(
        couponCode: String?,
        originalPrice: Double,
        daysToDeparture: Int,
        tenantId: String
    ): Result<CampaignCouponCalculationResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (couponCode != null) put("p_coupon_code", couponCode)
                put("p_original_price", originalPrice)
                put("p_days_to_departure", daysToDeparture)
            }

            val list = supabaseClient.postgrest.rpc("apply_campaign_coupon_discount", params)
                .decodeList<CampaignCouponCalculationResult>()

            list.firstOrNull() ?: calculateFallback(couponCode, originalPrice, daysToDeparture)
        }.recover { calculateFallback(couponCode, originalPrice, daysToDeparture) }
    }

    private fun calculateFallback(
        couponCode: String?,
        originalPrice: Double,
        daysToDeparture: Int
    ): CampaignCouponCalculationResult {
        var discount = if (daysToDeparture >= 30) originalPrice * 0.15 else 0.0
        var title = if (daysToDeparture >= 30) "Erken Rezervasyon %15 İndirimi" else "Standart Fiyat"
        var couponApplied = false

        if (!couponCode.isNullBackup()) {
            discount += originalPrice * 0.10
            title += " + Kupon: Yaz Fırsatı %10"
            couponApplied = true
        }

        val finalP = (originalPrice - discount).coerceAtLeast(0.0)
        return CampaignCouponCalculationResult(
            originalPrice = originalPrice,
            discountAmount = discount,
            finalPrice = finalP,
            appliedCampaignTitle = title,
            isCouponApplied = couponApplied,
            isEarlyBirdApplied = daysToDeparture >= 30
        )
    }

    private fun String?.isNullBackup(): Boolean = this.isNullOrBlank()
}
