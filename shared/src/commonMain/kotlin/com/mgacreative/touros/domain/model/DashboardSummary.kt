package com.mgacreative.touros.domain.model

data class DashboardSummary(
    val dailySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val occupancyRate: Double = 0.0,
    val cancellationCount: Int = 0,
    val pendingPaymentsAmount: Double = 0.0
)
