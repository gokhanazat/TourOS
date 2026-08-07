package com.mgacreative.touros.domain.usecase.forecast

import com.mgacreative.touros.domain.model.forecast.ForecastChartSeriesItem
import com.mgacreative.touros.domain.repository.SalesForecastRepository

class GetDashboardSalesForecastUseCase(
    private val repository: SalesForecastRepository
) {
    suspend operator fun invoke(tenantId: String, monthsAhead: Int = 6): Result<List<ForecastChartSeriesItem>> {
        return repository.getDashboardForecastSeries(tenantId, monthsAhead)
    }
}
