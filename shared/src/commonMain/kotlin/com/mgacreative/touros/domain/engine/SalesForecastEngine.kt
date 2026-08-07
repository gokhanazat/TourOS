package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.forecast.TourSalesForecast

/**
 * 5.2.1 Soyutlanmış Tahminleme Servis Arayüzü (Strategy Pattern).
 * İleride TensorFlow, Edge ML veya Python ML modellerine sorunsuz geçişi destekler.
 */
interface SalesForecastEngine {
    suspend fun generateForecast(tourId: String, daysAhead: Int, tenantId: String): Result<TourSalesForecast>
}
