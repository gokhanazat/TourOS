package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.1 B2C Müşteri Tur Modeli.
 */
@Serializable
data class B2CTourItem(
    @SerialName("tour_id") val tourId: String = "",
    @SerialName("tour_code") val tourCode: String = "",
    val title: String = "",
    val category: String = "Kültür Turu",
    @SerialName("destination_country") val destinationCountry: String = "Türkiye",
    @SerialName("duration_days") val durationDays: Int = 3,
    val price: Double = 2500.0,
    val currency: String = "TRY",
    val rating: Double = 4.85,
    @SerialName("review_count") val reviewCount: Int = 124,
    @SerialName("cover_image_url") val coverImageUrl: String = "",
    @SerialName("next_departure_date") val nextDepartureDate: String = "15.08.2026"
)
