package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.DashboardSummary
import com.mgacreative.touros.domain.model.GuideStatusInfo
import com.mgacreative.touros.domain.model.UpcomingTour
import com.mgacreative.touros.domain.model.VehicleOccupancy

interface DashboardRepository {
    suspend fun getDashboardSummary(tenantId: String): Result<DashboardSummary>
    suspend fun getUpcomingTours(tenantId: String): Result<List<UpcomingTour>>
    suspend fun getVehicleOccupancies(tenantId: String): Result<List<VehicleOccupancy>>
    suspend fun getGuideStatuses(tenantId: String): Result<List<GuideStatusInfo>>
    suspend fun getAnalyticsCharts(tenantId: String): Result<com.mgacreative.touros.domain.model.DashboardAnalyticsCharts>
}
