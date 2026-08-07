package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.*
import com.mgacreative.touros.domain.repository.DashboardRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val summary: DashboardSummary,
        val upcomingTours: List<UpcomingTour> = emptyList(),
        val vehicleOccupancies: List<VehicleOccupancy> = emptyList(),
        val guideStatuses: List<GuideStatusInfo> = emptyList(),
        val analyticsCharts: DashboardAnalyticsCharts = DashboardAnalyticsCharts()
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val dashboardRepository: DashboardRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            val user = getCurrentUserUseCase()
            val userRole = user?.role ?: UserRole.TOUR_OPERATOR
            val tenantId = if (userRole == UserRole.SYSTEM_ADMIN) "" else (user?.tenantId ?: "tenant_id")

            val summaryRes = getDashboardSummaryUseCase(tenantId, userRole).getOrDefault(DashboardSummary())
            val toursRes = dashboardRepository.getUpcomingTours(tenantId).getOrDefault(emptyList())
            val vehiclesRes = dashboardRepository.getVehicleOccupancies(tenantId).getOrDefault(emptyList())
            val guidesRes = dashboardRepository.getGuideStatuses(tenantId).getOrDefault(emptyList())
            val chartsRes = dashboardRepository.getAnalyticsCharts(tenantId).getOrDefault(DashboardAnalyticsCharts())

            _uiState.value = DashboardUiState.Success(
                summary = summaryRes,
                upcomingTours = toursRes,
                vehicleOccupancies = vehiclesRes,
                guideStatuses = guidesRes,
                analyticsCharts = chartsRes
            )
        }
    }
}
