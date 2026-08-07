package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.4.3 Harita İşaretçi ve Konum Noktası Modeli.
 */
@Serializable
data class SharedMapPoint(
    @SerialName("point_id") val pointId: String = "p1",
    val title: String = "Konum Noktası",
    val category: String = "HOTEL", // HOTEL, ROUTE_STOP, VEHICLE
    val latitude: Double = 36.8647,
    val longitude: Double = 31.0601,
    val snippet: String = "Açıklama"
)
