package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.CountrySalesData
import com.mgacreative.touros.domain.model.DailySalesData
import com.mgacreative.touros.domain.usecase.ChannelSalesData
import com.mgacreative.touros.domain.usecase.GetAnalyticsChartsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsChartsUiState(
    val dailySales: List<DailySalesData> = emptyList(),
    val countrySales: List<CountrySalesData> = emptyList(),
    val channelSales: List<ChannelSalesData> = emptyList(),
    val totalRevenue: Double = 0.0,
    val totalBookingsCount: Int = 0,
    val totalPaxOrNights: Int = 0,
    val averageBookingValue: Double = 0.0,
    val selectedDays: Int = 7,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AnalyticsChartsViewModel(
    private val getAnalyticsChartsUseCase: GetAnalyticsChartsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsChartsUiState())
    val uiState: StateFlow<AnalyticsChartsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData(days: Int = _uiState.value.selectedDays) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedDays = days)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getAnalyticsChartsUseCase(tenantId, days)
            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    dailySales = result.dailySales,
                    countrySales = result.countrySales,
                    channelSales = result.channelSales,
                    totalRevenue = result.totalRevenue,
                    totalBookingsCount = result.totalBookingsCount,
                    totalPaxOrNights = result.totalPaxOrNights,
                    averageBookingValue = result.averageBookingValue,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }
}
