package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.FinancialReportSummary
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetFinancialReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FinancialReportsUiState(
    val selectedTab: Int = 0, // 0: KDV, 1: Gelir/Gider, 2: Nakit & Banka, 3: Kârlılık
    val summary: FinancialReportSummary = FinancialReportSummary(),
    val isLoading: Boolean = false,
    val dateFilter: String = "30_DAYS", // 30_DAYS, THIS_MONTH, THIS_YEAR
    val selectedCompany: String = "Tüm Şirketler",
    val companyOptions: List<String> = listOf("Tüm Şirketler", "Merkez Acente", "Antalya Şube"),
    val selectedCurrency: String = "TRY ₺",
    val currencyOptions: List<String> = listOf("TRY ₺", "EUR €", "USD $"),
    val errorMessage: String? = null
) {
    val currencySymbol: String
        get() = when {
            selectedCurrency.contains("EUR") || selectedCurrency.contains("€") -> "€"
            selectedCurrency.contains("USD") || selectedCurrency.contains("$") -> "$"
            else -> "₺"
        }

    val currencyRate: Double
        get() = when {
            selectedCurrency.contains("EUR") || selectedCurrency.contains("€") -> 0.026 // ~1 EUR = 38.5 TRY
            selectedCurrency.contains("USD") || selectedCurrency.contains("$") -> 0.028 // ~1 USD = 36.0 TRY
            else -> 1.0
        }
}

class FinancialReportsViewModel(
    private val getFinancialReportUseCase: GetFinancialReportUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialReportsUiState())
    val uiState: StateFlow<FinancialReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getFinancialReportUseCase(tenantId)
            res.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(
                    summary = summary,
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

    fun setDateFilter(filter: String) {
        _uiState.value = _uiState.value.copy(dateFilter = filter)
        loadReports()
    }

    fun setSelectedCompany(company: String) {
        _uiState.value = _uiState.value.copy(selectedCompany = company)
    }

    fun setSelectedCurrency(currency: String) {
        _uiState.value = _uiState.value.copy(selectedCurrency = currency)
    }
}
