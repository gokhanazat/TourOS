package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.3.4 Merkezi PricingEngine Hesaplama Talep Modeli.
 */
@Serializable
data class CentralPricingRequest(
    val channel: String = "B2C", // B2C, B2B_AGENCY, ADMIN_PANEL
    @SerialName("base_price") val basePrice: Double = 2500.0,
    @SerialName("pax_count") val paxCount: Int = 2,
    @SerialName("coupon_code") val couponCode: String? = "SUMMER2026",
    @SerialName("days_to_departure") val daysToDeparture: Int = 45,
    @SerialName("occupancy_rate") val occupancyRate: Double = 85.0,
    @SerialName("agency_tier") val agencyTier: String = "VIP_AGENCY",
    val country: String = "GERMANY"
)

/**
 * 4.3.4 Merkezi PricingEngine Hesaplama Sonuç Modeli.
 */
@Serializable
data class CentralPricingResponse(
    val channel: String = "B2C",
    @SerialName("gross_amount") val grossAmount: Double = 5000.0,
    @SerialName("dynamic_adjustment_amount") val dynamicAdjustmentAmount: Double = 750.0,
    @SerialName("campaign_discount_amount") val campaignDiscountAmount: Double = 1437.5,
    @SerialName("agency_commission_amount") val agencyCommissionAmount: Double = 517.5,
    @SerialName("net_payable_amount") val netPayableAmount: Double = 3795.0,
    @SerialName("applied_rules_summary") val appliedRulesSummary: String = "Merkezi PricingEngine: [+%15 Doluluk Surge] [-%15 Erken Rezervasyon] [-%10 Kupon: SUMMER2026]"
)
