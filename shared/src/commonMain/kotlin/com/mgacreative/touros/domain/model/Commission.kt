package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.1 Komisyon Domain Modeli.
 */
@Serializable
data class Commission(
    val id: String = "",
    val bookingId: String = "",
    val agentName: String = "",
    val agentType: String = "agency", // agency, individual, platform
    val rate: Double = 0.0,
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val isPaid: Boolean = false,
    val paidAt: String? = null,
    val notes: String? = null,
    val tenantId: String = ""
)
