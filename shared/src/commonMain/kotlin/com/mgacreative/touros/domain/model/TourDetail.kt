package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * B2B ve B2C yeniden kullanılabilir Tur Detayı Domain Sarmal Modeli.
 */
@Serializable
data class TourDetail(
    val tour: Tour,
    val departures: List<Departure> = emptyList(),
    val itineraries: List<Itinerary> = emptyList(),
    val mediaUrls: List<String> = emptyList()
)
