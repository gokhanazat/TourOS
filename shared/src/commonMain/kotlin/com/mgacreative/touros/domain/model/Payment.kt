package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.1 Ödeme / Tahsilat Domain Modeli.
 */
@Serializable
data class Payment(
    val id: String = "",
    val invoiceId: String = "",
    val accountId: String? = null,
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val paymentMethod: String = "cash", // cash, credit_card, bank_transfer, online
    val paymentDate: String = "",
    val referenceNo: String? = null,
    val notes: String? = null,
    val tenantId: String = ""
)
