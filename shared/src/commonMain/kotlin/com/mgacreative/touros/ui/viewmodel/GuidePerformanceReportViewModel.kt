package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.GuidePerformanceSummary
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetGuidePerformanceReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GuidePerformanceUiState {
    data object Loading : GuidePerformanceUiState
    data class Success(val summary: GuidePerformanceSummary) : GuidePerformanceUiState
    data class Error(val message: String) : GuidePerformanceUiState
}

class GuidePerformanceReportViewModel(
    private val getGuidePerformanceReportUseCase: GetGuidePerformanceReportUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuidePerformanceUiState>(GuidePerformanceUiState.Loading)
    val uiState: StateFlow<GuidePerformanceUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = GuidePerformanceUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getGuidePerformanceReportUseCase(tenantId)
            res.onSuccess { summary ->
                _uiState.value = GuidePerformanceUiState.Success(summary)
            }.onFailure { err ->
                _uiState.value = GuidePerformanceUiState.Error(err.message ?: "Performans raporu yüklenemedi.")
            }
        }
    }
}
