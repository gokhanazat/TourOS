package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.6 Komisyon Hesaplama Kuralı Domain Modeli.
 */
@Serializable
data class CommissionRule(
    val id: String = "",
    val ruleName: String = "",
    val agentId: String? = null,
    val agentName: String? = null,
    val tourId: String? = null,
    val tourName: String? = null,
    val calculationType: String = "percentage", // percentage, fixed_amount
    val rateValue: Double = 0.0,
    val fixedAmount: Double = 0.0,
    val currency: String = "TRY",
    val isActive: Boolean = true,
    val tenantId: String = ""
)
