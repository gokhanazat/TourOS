package com.mgacreative.touros.domain.gateway

import kotlinx.serialization.Serializable

/**
 * 3.2.3 Link ile Ödeme Domain Modeli.
 */
@Serializable
data class PaymentLinkInfo(
    val id: String = "",
    val paymentLinkCode: String = "",
    val bookingId: String = "",
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val gatewayProvider: String = "stripe", // stripe, iyzico, paytr, mock
    val checkoutUrl: String = "",
    val status: String = "PENDING", // PENDING, PAID, EXPIRED, CANCELLED
    val expiresAt: String = "",
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val tenantId: String = ""
)
