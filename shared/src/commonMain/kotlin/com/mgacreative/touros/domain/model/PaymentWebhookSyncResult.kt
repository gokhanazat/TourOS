package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.2.4 Webhook Senkronizasyon Sonuç Modeli.
 */
@Serializable
data class PaymentWebhookSyncResult(
    val linkId: String = "",
    val bookingId: String = "",
    val invoiceId: String = "",
    val paidAmount: Double = 0.0,
    val syncStatus: String = "SYNC_SUCCESS"
)
