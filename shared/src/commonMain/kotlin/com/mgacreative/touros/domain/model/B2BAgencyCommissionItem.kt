package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.1.3 B2B Tur ve Dönemsel Komisyon Döküm Modeli.
 */
@Serializable
data class B2BAgencyCommissionItem(
    @SerialName("tour_id") val tourId: String = "",
    @SerialName("tour_title") val tourTitle: String = "",
    @SerialName("booking_count") val bookingCount: Int = 0,
    @SerialName("gross_sales_amount") val grossSalesAmount: Double = 0.0,
    @SerialName("commission_rate") val commissionRate: Double = 10.0,
    @SerialName("commission_amount") val commissionAmount: Double = 0.0,
    val status: String = "HAK_EDILDI", // HAK_EDILDI, ODENDI, BEKLIYOR
    @SerialName("period_name") val periodName: String = "Ağustos 2026"
)
