package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Oda Tipi Domain Modeli.
 */
@Serializable
data class RoomType(
    val id: String = "",
    val hotelId: String = "",
    val name: String = "",
    val description: String? = null,
    val basePricePerNight: Double = 0.0,
    val currency: String = "TRY",
    val maxOccupancy: Int = 2,
    val totalRooms: Int = 0,
    val allotment: Int = 0,
    val bookedRooms: Int = 0,
    val isActive: Boolean = true,
    val tenantId: String = ""
) {
    val availableRooms: Int get() = (allotment - bookedRooms).coerceAtLeast(0)
}
