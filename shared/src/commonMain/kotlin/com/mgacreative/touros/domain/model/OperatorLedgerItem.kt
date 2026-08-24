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
    @SerialName("total_sales") val totalSales: Double = 0.0, // Tur Satış Fiyatı (Paket Satış Tutarı)
    @SerialName("commission_rate") val commissionRate: Double = 0.0, // Acenta %
    @SerialName("tour_cost") val tourCost: Double = 0.0, // Tur Satış (Maliyeti) = Satış Fiyatı - Komisyon
    @SerialName("total_paid") val totalPaid: Double = 0.0, // TO Ödemesi
    @SerialName("balance") val balance: Double = 0.0, // Bakiye = Tur Satış (Maliyeti) - TO Ödemesi
    @SerialName("created_at") val createdAt: String = ""
)
