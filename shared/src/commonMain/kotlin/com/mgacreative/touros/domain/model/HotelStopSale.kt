package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.3.5 Stop Sale / Release Domain Modeli.
 */
@Serializable
data class HotelStopSale(
    val id: String = "",
    val hotelId: String = "",
    val roomTypeId: String? = null,
    val actionType: String = "STOP_SALE", // STOP_SALE, RELEASE
    val startDate: String = "",
    val endDate: String = "",
    val reason: String? = null,
    val isActive: Boolean = true,
    val tenantId: String = "",
    val createdAt: String = ""
)
