package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.5.3 Rehber Mobil Yoklama & Yolcu Bilgisi Domain Modeli.
 */
@Serializable
data class GuidePassengerInfo(
    val passengerId: String = "",
    val fullName: String = "",
    val tcPassport: String? = null,
    val phone: String? = null,
    val pickupHotel: String = "",
    val seatNumber: String? = null,
    val isCheckIn: Boolean = false, // Yoklamada Var/Yok
    val specialNotes: String? = null
)
