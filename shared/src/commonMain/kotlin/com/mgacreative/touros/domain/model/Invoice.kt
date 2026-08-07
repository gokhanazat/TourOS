package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.1 Fatura Domain Modeli.
 */
@Serializable
data class Invoice(
    val id: String = "",
    val invoiceNo: String = "",
    val bookingId: String? = null,
    val invoiceType: String = "sale", // sale, purchase, refund
    val customerName: String = "",
    val customerTaxNo: String? = null,
    val subtotal: Double = 0.0,
    val taxRate: Double = 20.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val currency: String = "TRY",
    val status: String = "issued", // draft, issued, paid, cancelled
    val issuedAt: String? = null,
    val dueDate: String? = null,
    val notes: String? = null,
    val tenantId: String = ""
)
