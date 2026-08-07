package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.3.1 Kampanya ve Kupon Kodu İndirim Hesaplama Sonuç Modeli.
 */
@Serializable
data class CampaignCouponCalculationResult(
    @SerialName("original_price") val originalPrice: Double = 2500.0,
    @SerialName("discount_amount") val discountAmount: Double = 625.0,
    @SerialName("final_price") val finalPrice: Double = 1875.0,
    @SerialName("applied_campaign_title") val appliedCampaignTitle: String = "Erken Rezervasyon %15 İndirimi + Kupon: SUMMER2026",
    @SerialName("is_coupon_applied") val isCouponApplied: Boolean = true,
    @SerialName("is_early_bird_applied") val isEarlyBirdApplied: Boolean = true
)
