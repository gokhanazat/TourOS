package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.2.1 Tur Kalkış Tarihi (Departure) Domain Modeli.
 * Bir turun birden fazla kalkış tarihi, özel yetişkin/çocuk/bebek fiyatlandırması ve konaklayacağı otel eşleşmelerini destekler.
 */
@Serializable
data class Departure(
    val id: String = "",
    val tourId: String = "",
    val departureDate: String = "",
    val returnDate: String? = null,
    val priceOverride: Double? = null,
    val childPriceOverride: Double? = null,
    val infantPriceOverride: Double? = null,
    val currency: String = "TRY",
    val capacity: Int? = 30,
    val bookedCount: Int = 0,
    val optionDeadlineDays: Int = 7,
    val isGuaranteed: Boolean = false,
    val status: String = "planned",
    val notes: String? = null,
    val assignedHotels: List<DepartureHotel> = emptyList()
) {
    val remainingCapacity: Int
        get() = ((capacity ?: 30) - bookedCount).coerceAtLeast(0)

    val occupancyPercentage: Double
        get() {
            val totalCap = (capacity ?: 30).coerceAtLeast(1)
            return ((bookedCount.toDouble() / totalCap.toDouble()) * 100.0)
        }

    val isSoldOut: Boolean
        get() = remainingCapacity == 0
}
