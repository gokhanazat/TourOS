package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DepartureHotel(
    val id: String = "",
    val departureId: String = "",
    val hotelId: String = "",
    val hotelName: String = "",
    val starRating: Int = 4,
    val nightCount: Int = 1,
    val sortOrder: Int = 1
)
