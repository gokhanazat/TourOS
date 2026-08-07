package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.3 En Çok Satan / En Kârlı Tur Performansı Modeli.
 */
@Serializable
data class TopTourPerformance(
    @SerialName("tour_id") val tourId: String = "",
    @SerialName("tour_title") val tourTitle: String = "",
    @SerialName("total_sales_count") val totalSalesCount: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("net_profit") val netProfit: Double = 0.0,
    @SerialName("profit_margin") val profitMargin: Double = 0.0
)
