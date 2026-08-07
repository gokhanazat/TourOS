package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.1.1 B2B Acente Profili ve Cari Hesap Bakiyesi Modeli.
 */
@Serializable
data class B2BAgencyProfile(
    @SerialName("agency_id") val agencyId: String = "",
    @SerialName("agency_code") val agencyCode: String = "",
    @SerialName("agency_name") val agencyName: String = "",
    @SerialName("contact_email") val contactEmail: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    @SerialName("credit_limit") val creditLimit: Double = 0.0,
    @SerialName("current_balance") val currentBalance: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("active_bookings_count") val activeBookingsCount: Int = 0,
    @SerialName("pending_commission") val pendingCommission: Double = 0.0,
    @SerialName("account_status") val accountStatus: String = "ACTIVE",
    @SerialName("last_transaction_at") val lastTransactionAt: String = ""
)
