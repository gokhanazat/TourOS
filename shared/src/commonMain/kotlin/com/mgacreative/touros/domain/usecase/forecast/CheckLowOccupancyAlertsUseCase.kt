package com.mgacreative.touros.domain.usecase.forecast

import com.mgacreative.touros.domain.model.forecast.LowOccupancyAlert
import com.mgacreative.touros.domain.repository.SalesForecastRepository

class CheckLowOccupancyAlertsUseCase(
    private val repository: SalesForecastRepository
) {
    suspend operator fun invoke(tenantId: String): Result<List<LowOccupancyAlert>> {
        return repository.getLowOccupancyAlerts(tenantId)
    }
}
