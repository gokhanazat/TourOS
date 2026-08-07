package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.ReportExportResult
import com.mgacreative.touros.domain.model.ReportFilter
import com.mgacreative.touros.domain.usecase.ExportReportUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportFilterExportUiState(
    val filter: ReportFilter = ReportFilter(),
    val availableCompanies: List<String> = listOf("Tüm Firmalar", "TourOS Merkez A.Ş.", "Lemora Travel", "ProjeMatik A.Ş."),
    val availableCurrencies: List<String> = listOf("TRY", "EUR", "USD", "GBP", "AED", "RUB"),
    val exportHistory: List<ReportExportResult> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class ReportFilterExportViewModel(
    private val exportReportUseCase: ExportReportUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportFilterExportUiState())
    val uiState: StateFlow<ReportFilterExportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val history = listOf(
                ReportExportResult("exp1", "Finansal_Rapor_Tüm_Firmalar_TRY_20260801.pdf", "https://touros.storage.supabase.co/reports/exports/Finansal_Rapor_TRY.pdf", 42, "PDF", "2026-08-01 10:00"),
                ReportExportResult("exp2", "KDV_Raporu_Lemora_EUR_20260805.xlsx", "https://touros.storage.supabase.co/reports/exports/KDV_Raporu_EUR.xlsx", 28, "EXCEL", "2026-08-05 16:30")
            )
            _uiState.value = _uiState.value.copy(exportHistory = history)
        }
    }

    fun updateFilter(company: String? = null, currency: String? = null, startDate: String? = null, endDate: String? = null) {
        val current = _uiState.value.filter
        _uiState.value = _uiState.value.copy(
            filter = current.copy(
                companyName = company ?: current.companyName,
                currency = currency ?: current.currency,
                startDate = startDate ?: current.startDate,
                endDate = endDate ?: current.endDate
            )
        )
    }

    fun exportReport(format: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val targetFilter = state.filter.copy(exportFormat = format)
            _uiState.value = state.copy(isLoading = true, filter = targetFilter)

            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = exportReportUseCase(targetFilter, tenantId)
            res.onSuccess { exportResult ->
                _uiState.value = state.copy(
                    isLoading = false,
                    exportHistory = listOf(exportResult) + state.exportHistory,
                    notificationMessage = "✅ ${format.uppercase()} Raporu Başarıyla Üretildi: ${exportResult.documentName}"
                )
            }.onFailure { err ->
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Rapor üretme hatası."
                )
            }
        }
    }
}
