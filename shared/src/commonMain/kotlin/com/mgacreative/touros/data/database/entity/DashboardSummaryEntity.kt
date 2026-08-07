package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummaryEntity(
    @SerialName("daily_sales") val dailySales: Double = 0.0,
    @SerialName("monthly_sales") val monthlySales: Double = 0.0,
    @SerialName("occupancy_rate") val occupancyRate: Double = 0.0,
    @SerialName("cancellation_count") val cancellationCount: Int = 0,
    @SerialName("pending_payments_amount") val pendingPaymentsAmount: Double = 0.0
)
