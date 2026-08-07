package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.2.2 Kısmi Ödeme / Depozito Özet Domain Modeli.
 */
@Serializable
data class PartialPaymentSummary(
    val paymentId: String = "",
    val bookingId: String = "",
    val totalPrice: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val paymentStatus: String = "UNPAID" // PAID, PARTIALLY_PAID, UNPAID
)
