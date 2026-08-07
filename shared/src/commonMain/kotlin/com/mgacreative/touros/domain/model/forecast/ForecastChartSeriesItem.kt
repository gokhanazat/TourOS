package com.mgacreative.touros.domain.model.forecast

import kotlinx.serialization.Serializable

@Serializable
data class ForecastChartSeriesItem(
    val periodLabel: String,
    val actualRevenue: Double? = null,
    val predictedRevenue: Double? = null,
    val actualOccupancyRate: Double? = null,
    val predictedOccupancyRate: Double? = null,
    val isForecast: Boolean = false
)
