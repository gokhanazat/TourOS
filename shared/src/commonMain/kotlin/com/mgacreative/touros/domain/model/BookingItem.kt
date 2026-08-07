package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Rezervasyon Kalemi (Servis, Ekstra, Konaklama vb.) Domain Modeli.
 */
@Serializable
data class BookingItem(
    val id: String = "",
    val bookingId: String = "",
    val description: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = quantity * unitPrice,
    val itemType: String = "service",
    val notes: String? = null
)
