package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.1.4 Cari Hesap ve Bakiye Dökümü Domain Modeli (Benzersiz Cari Kodu & Vergi No Destekli)
 */
@Serializable
data class CurrentAccountItem(
    @SerialName("entity_id") val entityId: String = "",
    @SerialName("account_code") val accountCode: String = "", // Benzersiz Cari Kodu (Örn: CAR-2026-00101)
    @SerialName("tax_no") val taxNo: String? = null, // T.C. Kimlik No veya Vergi Kimlik No
    @SerialName("entity_name") val entityName: String = "",
    @SerialName("entity_type") val entityType: String = "customer", // customer, agency, supplier
    @SerialName("phone") val phone: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("total_debit") val totalDebit: Double = 0.0, // Borç Toplamı
    @SerialName("total_credit") val totalCredit: Double = 0.0, // Alacak Toplamı
    @SerialName("balance") val balance: Double = 0.0, // Net Bakiye
    @SerialName("currency") val currency: String = "TRY",
    @SerialName("last_transaction_date") val lastTransactionDate: String = ""
)

@Serializable
data class AccountTransactionDetail(
    @SerialName("id") val id: String = "",
    @SerialName("date") val date: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("debit") val debit: Double = 0.0,
    @SerialName("credit") val credit: Double = 0.0,
    @SerialName("balance") val balance: Double = 0.0,
    @SerialName("reference_no") val referenceNo: String? = null
)
