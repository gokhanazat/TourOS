package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Tur Gün Bazlı Program (Itinerary) Domain Modeli.
 */
@Serializable
data class Itinerary(
    val id: String = "",
    val tourId: String = "",
    val dayNumber: Int = 1,
    val title: String = "",
    val description: String? = null,
    val location: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val sortOrder: Int = 0
)
