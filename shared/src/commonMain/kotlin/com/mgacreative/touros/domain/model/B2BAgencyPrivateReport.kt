package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.1.5 B2B Acenteye Özel Satış ve İptal Oranı Rapor Modeli.
 */
@Serializable
data class B2BAgencyPrivateReport(
    @SerialName("total_sales_count") val totalSalesCount: Int = 38,
    @SerialName("total_gross_sales") val totalGrossSales: Double = 485000.0,
    @SerialName("cancelled_count") val cancelledCount: Int = 1,
    @SerialName("cancellation_rate") val cancellationRate: Double = 2.63,
    @SerialName("active_confirmed_count") val activeConfirmedCount: Int = 37,
    @SerialName("net_earned_commission") val netEarnedCommission: Double = 48500.0,
    @SerialName("monthly_growth_rate") val monthlyGrowthRate: Double = 14.20,
    @SerialName("top_selling_tour_title") val topSellingTourTitle: String = "Kapadokya Balon & Vadi Turu"
)
