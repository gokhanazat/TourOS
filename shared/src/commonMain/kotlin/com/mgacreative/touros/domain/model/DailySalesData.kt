package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.2 Günlük Satış Grafiği Veri Modeli.
 */
@Serializable
data class DailySalesData(
    @SerialName("sale_date") val saleDate: String = "",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("booking_count") val bookingCount: Int = 0
)
