package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Expense
import com.mgacreative.touros.domain.model.SupplierTransaction
import com.mgacreative.touros.domain.repository.FinanceRepository
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
        val isCreatingExpense: Boolean = false,
        val notificationMessage: String? = null
    ) : SupplierExpenseUiState
    data class Error(val message: String) : SupplierExpenseUiState
}

class SupplierExpenseViewModel(
    private val financeRepository: FinanceRepository,
    private val processSupplierExpenseUseCase: ProcessSupplierExpenseUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SupplierExpenseUiState>(SupplierExpenseUiState.Loading)
    val uiState: StateFlow<SupplierExpenseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun resolveTenantId(userTenantId: String?): String {
        val tid = userTenantId?.trim()
        return if (!tid.isNullOrBlank() && tid != "tenant_id") tid else "00000000-0000-0000-0000-000000000001"
    }

    fun loadData(categoryFilter: String? = null) {
        viewModelScope.launch {
            _uiState.value = SupplierExpenseUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val res = financeRepository.getExpenses(tenantId)
            val fetchedExpenses = res.getOrDefault(emptyList())

            val transactions = fetchedExpenses.map { exp ->
                val supplierType = when {
                    exp.category.lowercase().contains("otel") || exp.category.lowercase().contains("hotel") -> "hotel"
                    exp.category.lowercase().contains("araç") || exp.category.lowercase().contains("otobüs") || exp.category.lowercase().contains("transfer") || exp.category.lowercase().contains("yakıt") -> "vehicle"
                    exp.category.lowercase().contains("rehber") -> "guide"
                    else -> "other"
                }

                SupplierTransaction(
                    id = exp.id,
                    supplierName = exp.description.takeIf { it.isNotBlank() } ?: "Tedarikçi Firma",
                    supplierType = supplierType,
                    departureId = exp.departureId ?: "dep-genel",
                    transactionType = "debt",
                    amount = exp.amount,
                    currency = exp.currency,
                    description = "${exp.category} - ${exp.description}",
                    isSettled = true,
                    tenantId = tenantId,
                    createdAt = exp.expenseDate.take(10).ifBlank { "Bugün" }
                )
            }

            var filtered = transactions
            if (categoryFilter != null) {
                filtered = filtered.filter { it.supplierType == categoryFilter }
            }

            val hotelDebt = transactions.filter { it.supplierType == "hotel" }.sumOf { it.amount }
            val vehicleDebt = transactions.filter { it.supplierType == "vehicle" }.sumOf { it.amount }
            val guideDebt = transactions.filter { it.supplierType == "guide" }.sumOf { it.amount }

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

    fun createSupplierExpense(
        supplierName: String,
        supplierType: String, // "hotel", "vehicle", "guide", "other"
        amount: Double,
        categoryName: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value as? SupplierExpenseUiState.Success ?: return@launch
            _uiState.value = currentState.copy(isCreatingExpense = true, notificationMessage = null)

            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val expenseCategory = when (supplierType) {
                "hotel" -> "Otel Konaklama Gideri"
                "vehicle" -> "Araç & Yakıt Transfer Gideri"
                "guide" -> "Kokartlı Rehber Hakedişi"
                else -> categoryName.ifBlank { "Operasyonel Gider" }
            }

            val newExpense = Expense(
                category = expenseCategory,
                amount = amount,
                currency = "TRY",
                description = "$supplierName - ${notes ?: expenseCategory}",
                departureId = "EXP-${(1000..9999).random()}",
                notes = notes,
                tenantId = tenantId
            )

            val res = financeRepository.createExpense(newExpense)
            res.onSuccess {
                loadData()
            }.onFailure { err ->
                _uiState.value = SupplierExpenseUiState.Error(err.message ?: "Gider kaydı veritabanına işlenemedi.")
            }
        }
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
