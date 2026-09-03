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

    private var cachedAccounts: List<CurrentAccountItem> = emptyList()
    private var currentFilter: String? = null
    private var currentQuery: String = ""

    init {
        loadData(showLoading = true)
    }

    private fun resolveTenantId(userTenantId: String?): String {
        val tid = userTenantId?.trim()
        return if (!tid.isNullOrBlank() && tid != "tenant_id") tid else ""
    }

    fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _uiState.value !is CurrentAccountUiState.Success) {
                _uiState.value = CurrentAccountUiState.Loading
            }
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val res = getCurrentAccountsUseCase(tenantId, null)
            cachedAccounts = res.getOrDefault(emptyList())
            applyFilters()
        }
    }

    private fun applyFilters() {
        var filtered = cachedAccounts
        if (currentFilter != null) {
            filtered = filtered.filter { it.entityType == currentFilter }
        }
        if (currentQuery.isNotBlank()) {
            val q = currentQuery.trim()
            filtered = filtered.filter { 
                it.entityName.contains(q, ignoreCase = true) ||
                it.accountCode.contains(q, ignoreCase = true) ||
                (it.taxNo?.contains(q, ignoreCase = true) == true) ||
                (it.phone?.contains(q, ignoreCase = true) == true) ||
                (it.email?.contains(q, ignoreCase = true) == true)
            }
        }

        val custReceivables = cachedAccounts.filter { it.entityType == "customer" }.sumOf { it.balance }
        val suppPayables = cachedAccounts.filter { it.entityType == "supplier" }.sumOf { kotlin.math.abs(it.balance) }
        val net = custReceivables - suppPayables

        val currentSuccess = _uiState.value as? CurrentAccountUiState.Success
        _uiState.value = CurrentAccountUiState.Success(
            accounts = filtered,
            selectedEntityType = currentFilter,
            searchQuery = currentQuery,
            totalCustomerReceivables = custReceivables,
            totalSupplierPayables = suppPayables,
            netBalance = net,
            selectedAccountForStatement = currentSuccess?.selectedAccountForStatement,
            statementDetails = currentSuccess?.statementDetails ?: emptyList()
        )
    }

    fun setFilter(entityType: String?) {
        currentFilter = entityType
        applyFilters()
    }

    fun onSearchQueryChanged(q: String) {
        currentQuery = q
        applyFilters()
    }

    fun selectAccountForStatement(account: CurrentAccountItem?) {
        val state = _uiState.value as? CurrentAccountUiState.Success ?: return
        if (account == null) {
            _uiState.value = state.copy(selectedAccountForStatement = null, statementDetails = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                selectedAccountForStatement = account,
                statementDetails = emptyList()
            )
        }
    }
}
