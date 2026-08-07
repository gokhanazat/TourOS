package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.forecast.ForecastChartSeriesItem
import com.mgacreative.touros.domain.model.forecast.LowOccupancyAlert
import com.mgacreative.touros.domain.model.forecast.TourSalesForecast

interface SalesForecastRepository {
    suspend fun getTourForecast(tourId: String, daysAhead: Int, tenantId: String): Result<TourSalesForecast>
    suspend fun getDashboardForecastSeries(tenantId: String, monthsAhead: Int = 6): Result<List<ForecastChartSeriesItem>>
    suspend fun getLowOccupancyAlerts(tenantId: String): Result<List<LowOccupancyAlert>>
}
