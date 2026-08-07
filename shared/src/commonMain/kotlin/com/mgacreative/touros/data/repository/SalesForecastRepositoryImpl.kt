package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.factory.ForecastEngineFactory
import com.mgacreative.touros.domain.model.forecast.AlertSeverity
import com.mgacreative.touros.domain.model.forecast.ForecastChartSeriesItem
import com.mgacreative.touros.domain.model.forecast.ForecastModelType
import com.mgacreative.touros.domain.model.forecast.LowOccupancyAlert
import com.mgacreative.touros.domain.model.forecast.TourSalesForecast
import com.mgacreative.touros.domain.repository.SalesForecastRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SalesForecastRepositoryImpl(
    private val forecastEngineFactory: ForecastEngineFactory,
    private val supabaseClient: SupabaseClient? = null
) : SalesForecastRepository {

    override suspend fun getTourForecast(
        tourId: String,
        daysAhead: Int,
        tenantId: String
    ): Result<TourSalesForecast> {
        val engine = forecastEngineFactory.getEngine(ForecastModelType.HEURISTIC_HISTORICAL)
        return engine.generateForecast(tourId, daysAhead, tenantId)
    }

    override suspend fun getDashboardForecastSeries(
        tenantId: String,
        monthsAhead: Int
    ): Result<List<ForecastChartSeriesItem>> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject {
                    put("p_tenant_id", tenantId)
                    put("p_months_ahead", monthsAhead)
                }
                supabaseClient.postgrest.rpc("get_dashboard_sales_forecast_series", params)
                    .decodeList<ForecastChartSeriesItem>()
            } else {
                getMockDashboardForecastSeries()
            }
        }.recover {
            getMockDashboardForecastSeries()
        }
    }

    override suspend fun getLowOccupancyAlerts(tenantId: String): Result<List<LowOccupancyAlert>> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject { put("p_tenant_id", tenantId) }
                supabaseClient.postgrest.rpc("check_low_occupancy_tour_departures", params)
                    .decodeList<LowOccupancyAlert>()
            } else {
                getMockLowOccupancyAlerts()
            }
        }.recover {
            getMockLowOccupancyAlerts()
        }
    }

    private fun getMockDashboardForecastSeries(): List<ForecastChartSeriesItem> {
        return listOf(
            ForecastChartSeriesItem(periodLabel = "Ocak", actualRevenue = 12500.0, actualOccupancyRate = 75.0, isForecast = false),
            ForecastChartSeriesItem(periodLabel = "Şubat", actualRevenue = 14200.0, actualOccupancyRate = 80.0, isForecast = false),
            ForecastChartSeriesItem(periodLabel = "Mart", actualRevenue = 18900.0, actualOccupancyRate = 88.0, isForecast = false),
            ForecastChartSeriesItem(periodLabel = "Nisan (Tahmini)", predictedRevenue = 22400.0, predictedOccupancyRate = 92.5, isForecast = true),
            ForecastChartSeriesItem(periodLabel = "Mayıs (Tahmini)", predictedRevenue = 26800.0, predictedOccupancyRate = 95.0, isForecast = true),
            ForecastChartSeriesItem(periodLabel = "Haziran (Tahmini)", predictedRevenue = 31000.0, predictedOccupancyRate = 98.0, isForecast = true)
        )
    }

    private fun getMockLowOccupancyAlerts(): List<LowOccupancyAlert> {
        return listOf(
            LowOccupancyAlert(
                alertId = "alert-001",
                tourId = "tour-901",
                tourName = "Kapadokya Gün Batımı ATV Turu",
                departureDate = "10 Ağustos 2026 (3 Gün Kaldı)",
                currentCapacity = 30,
                bookedCount = 8,
                occupancyRate = 26.67,
                suggestedCampaign = "%20 Son Dakika Kampanyası & B2B Acente Fırsatı",
                severity = AlertSeverity.CRITICAL
            ),
            LowOccupancyAlert(
                alertId = "alert-002",
                tourId = "tour-902",
                tourName = "Pamukkale Hierapolis Günübirlik VIP",
                departureDate = "12 Ağustos 2026 (5 Gün Kaldı)",
                currentCapacity = 25,
                bookedCount = 11,
                occupancyRate = 44.0,
                suggestedCampaign = "B2B Acentelerine Özel +%5 Ek Komisyon Teşviki",
                severity = AlertSeverity.WARNING
            )
        )
    }
}
