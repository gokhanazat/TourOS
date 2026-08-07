package com.mgacreative.touros.domain.model.forecast

import kotlinx.serialization.Serializable

enum class ForecastModelType {
    HEURISTIC_HISTORICAL,
    MACHINE_LEARNING_REGRESSION,
    HYBRID
}

@Serializable
data class TourSalesForecast(
    val forecastId: String,
    val tourId: String,
    val predictedOccupancyRate: Double = 85.5,
    val predictedRevenue: Double = 14250.0,
    val confidenceScore: Double = 91.2,
    val modelType: ForecastModelType = ForecastModelType.HEURISTIC_HISTORICAL,
    val forecastDaysAhead: Int = 30
)
