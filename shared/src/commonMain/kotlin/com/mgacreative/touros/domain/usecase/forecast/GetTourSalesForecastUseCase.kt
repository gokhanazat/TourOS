package com.mgacreative.touros.domain.usecase.forecast

import com.mgacreative.touros.domain.model.forecast.TourSalesForecast
import com.mgacreative.touros.domain.repository.SalesForecastRepository

class GetTourSalesForecastUseCase(
    private val repository: SalesForecastRepository
) {
    suspend operator fun invoke(tourId: String, daysAhead: Int = 30, tenantId: String): Result<TourSalesForecast> {
        return repository.getTourForecast(tourId, daysAhead, tenantId)
    }
}
