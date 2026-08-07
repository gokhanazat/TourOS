package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 4.1.2 B2B Acente Adına Rezervasyon Talep Modeli.
 */
@Serializable
data class B2BBookingRequest(
    val agencyId: String = "acn-101",
    val departureId: String = "dep-201",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val paxCount: Int = 1,
    val notes: String? = null,
    val useCreditLimit: Boolean = true
)
