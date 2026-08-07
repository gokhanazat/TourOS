package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.CancellationMetrics
import com.mgacreative.touros.domain.model.PerformerRanking
import com.mgacreative.touros.domain.model.TopTourPerformance
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetPerformanceReportsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerformanceReportsUiState(
    val selectedTab: Int = 0, // 0: Top Tours, 1: Cancellation, 2: Performers
    val topTours: List<TopTourPerformance> = emptyList(),
    val cancellationMetrics: CancellationMetrics = CancellationMetrics(),
    val performers: List<PerformerRanking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PerformanceReportsViewModel(
    private val getPerformanceReportsUseCase: GetPerformanceReportsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceReportsUiState())
    val uiState: StateFlow<PerformanceReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getPerformanceReportsUseCase(tenantId)
            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    topTours = result.topTours,
                    cancellationMetrics = result.cancellationMetrics,
                    performers = result.performers,
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

    fun setSelectedTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }
}
