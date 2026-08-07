package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.5.3 Rehber Mobil Atanmış Tur Domain Modeli.
 */
@Serializable
data class GuideAssignedTour(
    val departureId: String = "",
    val tourTitle: String = "",
    val departureDate: String = "",
    val returnDate: String = "",
    val assignedVehiclePlate: String = "",
    val assignedDriverName: String = "",
    val assignedDriverPhone: String? = null,
    val totalPaxCount: Int = 0,
    val status: String = "active", // active, upcoming, completed
    val passengers: List<GuidePassengerInfo> = emptyList(),
    val pickups: List<PickupPoint> = emptyList()
)
