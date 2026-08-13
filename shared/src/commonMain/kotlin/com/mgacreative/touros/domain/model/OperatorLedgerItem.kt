package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tur Operatörü PNR Bazlı Cari Ekstre Kalemi Domain Modeli.
 */
@Serializable
data class OperatorLedgerItem(
    @SerialName("operator_pnr_code") val operatorPnrCode: String = "-",
    @SerialName("customer_name") val customerName: String = "",
    @SerialName("booking_code") val bookingCode: String = "",
    @SerialName("operator_name") val operatorName: String = "",
    @SerialName("total_sales") val totalSales: Double = 0.0,
    @SerialName("total_paid") val totalPaid: Double = 0.0,
    @SerialName("balance") val balance: Double = 0.0,
    @SerialName("created_at") val createdAt: String = ""
)
