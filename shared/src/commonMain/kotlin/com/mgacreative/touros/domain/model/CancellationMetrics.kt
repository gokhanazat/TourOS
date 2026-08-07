package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.3 İptal Oranı Metrikleri Modeli.
 */
@Serializable
data class CancellationMetrics(
    @SerialName("total_bookings") val totalBookings: Int = 0,
    @SerialName("cancelled_bookings") val cancelledBookings: Int = 0,
    @SerialName("cancellation_rate") val cancellationRate: Double = 0.0
)
