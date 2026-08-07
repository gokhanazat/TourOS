package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.SupplierTransaction
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ProcessSupplierExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SupplierExpenseUiState {
    data object Loading : SupplierExpenseUiState
    data class Success(
        val transactions: List<SupplierTransaction> = emptyList(),
        val selectedCategoryFilter: String? = null,
        val totalHotelDebt: Double = 0.0,
        val totalVehicleDebt: Double = 0.0,
        val totalGuideDebt: Double = 0.0,
        val notificationMessage: String? = null
    ) : SupplierExpenseUiState
    data class Error(val message: String) : SupplierExpenseUiState
}

class SupplierExpenseViewModel(
    private val processSupplierExpenseUseCase: ProcessSupplierExpenseUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SupplierExpenseUiState>(SupplierExpenseUiState.Loading)
    val uiState: StateFlow<SupplierExpenseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData(categoryFilter: String? = null) {
        viewModelScope.launch {
            _uiState.value = SupplierExpenseUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val fallbackList = listOf(
                SupplierTransaction("st1", "Hilton Istanbul Bosphorus", "hotel", "dep-101", "debt", 45000.0, "TRY", "Kapadokya Tur Konaklama Bedeli (15 Oda)", false, tenantId, "2026-08-05"),
                SupplierTransaction("st2", "Lüks Otobüs A.Ş.", "vehicle", "dep-101", "debt", 18000.0, "TRY", "34 TOUR 01 Travego 4 Günlük Transfer Bedeli", false, tenantId, "2026-08-04"),
                SupplierTransaction("st3", "Zeynep Arslan", "guide", "dep-101", "debt", 8500.0, "TRY", "Rehberlik Yevmiye ve Harcırah Bedeli", false, tenantId, "2026-08-05"),
                SupplierTransaction("st4", "Swissôtel Maçka", "hotel", "dep-102", "debt", 32000.0, "TRY", "Ege Turu 8 Oda Kapanış Bedeli", true, tenantId, "2026-08-01")
            )

            var filtered = fallbackList
            if (categoryFilter != null) {
                filtered = filtered.filter { it.supplierType == categoryFilter }
            }

            val hotelDebt = fallbackList.filter { it.supplierType == "hotel" && !it.isSettled }.sumOf { it.amount }
            val vehicleDebt = fallbackList.filter { it.supplierType == "vehicle" && !it.isSettled }.sumOf { it.amount }
            val guideDebt = fallbackList.filter { it.supplierType == "guide" && !it.isSettled }.sumOf { it.amount }

            _uiState.value = SupplierExpenseUiState.Success(
                transactions = filtered,
                selectedCategoryFilter = categoryFilter,
                totalHotelDebt = hotelDebt,
                totalVehicleDebt = vehicleDebt,
                totalGuideDebt = guideDebt
            )
        }
    }

    fun setCategoryFilter(category: String?) {
        loadData(category)
    }

    fun settleTransaction(transaction: SupplierTransaction) {
        viewModelScope.launch {
            val state = _uiState.value as? SupplierExpenseUiState.Success ?: return@launch

            val res = processSupplierExpenseUseCase(transaction)
            res.onSuccess { expense ->
                val updatedList = state.transactions.map {
                    if (it.id == transaction.id) it.copy(isSettled = true) else it
                }
                _uiState.value = state.copy(
                    transactions = updatedList,
                    notificationMessage = "⚡ ${transaction.supplierName} carisi ödendi ve Otomatik Gider Kaydı (${expense.amount} TRY) oluşturuldu."
                )
            }.onFailure { err ->
                _uiState.value = SupplierExpenseUiState.Error(err.message ?: "Cari kapatma işlemi başarısız.")
            }
        }
    }
}
