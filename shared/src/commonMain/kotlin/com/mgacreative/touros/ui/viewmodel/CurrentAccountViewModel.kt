package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.AccountTransactionDetail
import com.mgacreative.touros.domain.model.CurrentAccountItem
import com.mgacreative.touros.domain.usecase.GetCurrentAccountsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CurrentAccountUiState {
    data object Loading : CurrentAccountUiState
    data class Success(
        val accounts: List<CurrentAccountItem> = emptyList(),
        val selectedEntityType: String? = null,
        val searchQuery: String = "",
        val totalCustomerReceivables: Double = 0.0,
        val totalSupplierPayables: Double = 0.0,
        val netBalance: Double = 0.0,
        val selectedAccountForStatement: CurrentAccountItem? = null,
        val statementDetails: List<AccountTransactionDetail> = emptyList()
    ) : CurrentAccountUiState
    data class Error(val message: String) : CurrentAccountUiState
}

class CurrentAccountViewModel(
    private val getCurrentAccountsUseCase: GetCurrentAccountsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CurrentAccountUiState>(CurrentAccountUiState.Loading)
    val uiState: StateFlow<CurrentAccountUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun resolveTenantId(userTenantId: String?): String {
        val tid = userTenantId?.trim()
        return if (!tid.isNullOrBlank() && tid != "tenant_id") tid else "00000000-0000-0000-0000-000000000001"
    }

    fun loadData(entityTypeFilter: String? = null, query: String = "") {
        viewModelScope.launch {
            _uiState.value = CurrentAccountUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val res = getCurrentAccountsUseCase(tenantId, entityTypeFilter)
            val items = res.getOrDefault(emptyList())

            var filtered = items
            if (entityTypeFilter != null) {
                filtered = filtered.filter { it.entityType == entityTypeFilter }
            }
            if (query.isNotBlank()) {
                filtered = filtered.filter { 
                    it.entityName.contains(query, ignoreCase = true) ||
                    it.accountCode.contains(query, ignoreCase = true) ||
                    (it.taxNo?.contains(query, ignoreCase = true) == true)
                }
            }

            val custReceivables = items.filter { it.entityType == "customer" }.sumOf { it.balance }
            val suppPayables = items.filter { it.entityType == "supplier" }.sumOf { kotlin.math.abs(it.balance) }
            val net = custReceivables - suppPayables

            _uiState.value = CurrentAccountUiState.Success(
                accounts = filtered,
                selectedEntityType = entityTypeFilter,
                searchQuery = query,
                totalCustomerReceivables = custReceivables,
                totalSupplierPayables = suppPayables,
                netBalance = net
            )
        }
    }

    fun setFilter(entityType: String?) {
        val current = _uiState.value as? CurrentAccountUiState.Success
        loadData(entityType, current?.searchQuery ?: "")
    }

    fun onSearchQueryChanged(q: String) {
        val current = _uiState.value as? CurrentAccountUiState.Success
        loadData(current?.selectedEntityType, q)
    }

    fun selectAccountForStatement(account: CurrentAccountItem?) {
        val state = _uiState.value as? CurrentAccountUiState.Success ?: return
        if (account == null) {
            _uiState.value = state.copy(selectedAccountForStatement = null, statementDetails = emptyList())
            return
        }

        viewModelScope.launch {
            val sampleDetails = listOf(
                AccountTransactionDetail("t1", "07.08.2026", "Rezervasyon Satış Faturası", account.balance.coerceAtLeast(0.0), 0.0, account.balance, "INV-1001"),
                AccountTransactionDetail("t2", "05.08.2026", "Banka Havale Tahsilatı", 0.0, 5000.0, (account.balance - 5000.0).coerceAtLeast(0.0), "DEC-2004")
            )
            _uiState.value = state.copy(
                selectedAccountForStatement = account,
                statementDetails = sampleDetails
            )
        }
    }
}
