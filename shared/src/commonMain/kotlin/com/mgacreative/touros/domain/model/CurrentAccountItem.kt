package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.4 Cari Hesap ve Bakiye Dökümü Domain Modeli.
 */
@Serializable
data class CurrentAccountItem(
    val entityId: String = "",
    val entityName: String = "",
    val entityType: String = "customer", // customer, agency, supplier
    val phone: String? = null,
    val email: String? = null,
    val totalDebit: Double = 0.0, // Borç Toplamı
    val totalCredit: Double = 0.0, // Alacak Toplamı
    val balance: Double = 0.0, // Net Bakiye
    val currency: String = "TRY",
    val lastTransactionDate: String = ""
)

@Serializable
data class AccountTransactionDetail(
    val id: String = "",
    val date: String = "",
    val description: String = "",
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0,
    val referenceNo: String? = null
)
