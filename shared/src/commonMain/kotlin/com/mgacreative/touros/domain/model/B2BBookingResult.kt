package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.1.2 B2B Acente Rezervasyon Sonuç Modeli.
 */
@Serializable
data class B2BBookingResult(
    @SerialName("booking_id") val bookingId: String = "",
    @SerialName("booking_code") val bookingCode: String = "",
    @SerialName("total_price") val totalPrice: Double = 0.0,
    @SerialName("commission_amount") val commissionAmount: Double = 0.0,
    @SerialName("net_agent_payable") val netAgentPayable: Double = 0.0,
    @SerialName("new_agency_balance") val newAgencyBalance: Double = 0.0,
    @SerialName("created_at") val createdAt: String = ""
)
