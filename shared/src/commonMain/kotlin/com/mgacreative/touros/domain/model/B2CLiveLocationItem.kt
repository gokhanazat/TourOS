package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.5 B2C Canlı Araç ve Rehber Konum Modeli.
 */
@Serializable
data class B2CLiveLocationItem(
    @SerialName("vehicle_id") val vehicleId: String = "veh-101",
    @SerialName("vehicle_plate") val vehiclePlate: String = "34 TUR 2026",
    @SerialName("driver_name") val driverName: String = "Ahmet Yılmaz (Kaptan)",
    @SerialName("guide_name") val guideName: String = "Mehmet Demir (Kokartlı Rehber)",
    val latitude: Double = 38.6431,
    val longitude: Double = 34.8289,
    @SerialName("speed_kmh") val speedKmh: Double = 65.5,
    @SerialName("heading_degrees") val headingDegrees: Double = 120.0,
    @SerialName("updated_at") val updatedAt: String = "14:28:55"
)
