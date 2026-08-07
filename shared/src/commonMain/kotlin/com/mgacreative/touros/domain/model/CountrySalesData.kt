package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.2 Ülke Bazlı Satış Grafiği Veri Modeli.
 */
@Serializable
data class CountrySalesData(
    @SerialName("country_code") val countryCode: String = "TR",
    @SerialName("country_name") val countryName: String = "Türkiye",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("booking_count") val bookingCount: Int = 0,
    val percentage: Double = 0.0
)
