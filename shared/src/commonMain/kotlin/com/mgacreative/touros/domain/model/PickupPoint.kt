package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.4.3 Şoför Pickup Noktası Domain Modeli.
 */
@Serializable
data class PickupPoint(
    val id: String = "",
    val transferId: String = "",
    val passengerName: String = "",
    val passengerPhone: String? = null,
    val hotelName: String = "",
    val locationName: String = "",
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val scheduledTime: String = "",
    val status: String = "pending", // pending, picked_up, no_show
    val paxCount: Int = 1,
    val roomNumber: String? = null,
    val notes: String? = null,
    val tenantId: String = ""
)
