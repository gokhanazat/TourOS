package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.3 Tedarikçi Cari & Otomatik Gider Domain Modeli.
 */
@Serializable
data class SupplierTransaction(
    val id: String = "",
    val supplierName: String = "",
    val supplierType: String = "hotel", // hotel, vehicle, guide
    val departureId: String? = null,
    val transactionType: String = "debt", // debt, credit
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val description: String = "",
    val isSettled: Boolean = false,
    val tenantId: String = "",
    val createdAt: String = ""
)
