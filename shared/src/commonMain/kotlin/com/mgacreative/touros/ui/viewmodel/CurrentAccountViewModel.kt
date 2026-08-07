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

    fun loadData(entityTypeFilter: String? = null, query: String = "") {
        viewModelScope.launch {
            _uiState.value = CurrentAccountUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getCurrentAccountsUseCase(tenantId, entityTypeFilter)
            val fetched = res.getOrDefault(emptyList())

            val items = if (fetched.isEmpty()) {
                listOf(
                    CurrentAccountItem("c1", "Hans Müller", "customer", "+49 151 123456", "hans@example.com", 12000.0, 12000.0, 0.0, "TRY", "2026-08-06"),
                    CurrentAccountItem("c2", "Sarah Jenkins", "customer", "+44 7700 900077", "sarah@example.com", 24000.0, 10000.0, 14000.0, "TRY", "2026-08-05"),
                    CurrentAccountItem("a1", "Jolly Tur Acentesi", "agency", "+90 212 555 0100", "info@jollytur.com", 8500.0, 0.0, 8500.0, "TRY", "2026-08-04"),
                    CurrentAccountItem("s1", "Hilton Istanbul Bosphorus", "supplier", "+90 212 310 0000", "reservation@hilton.com", 45000.0, 0.0, -45000.0, "TRY", "2026-08-05"),
                    CurrentAccountItem("s2", "Lüks Otobüs Transfer", "supplier", "+90 212 444 0202", "operasyon@luksotobus.com", 18000.0, 0.0, -18000.0, "TRY", "2026-08-04")
                )
            } else {
                fetched
            }

            var filtered = items
            if (entityTypeFilter != null) {
                filtered = filtered.filter { it.entityType == entityTypeFilter }
            }
            if (query.isNotBlank()) {
                filtered = filtered.filter { it.entityName.contains(query, ignoreCase = true) }
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

        // Mock statement details (Hareket Dökümü)
        val statement = listOf(
            AccountTransactionDetail("tx1", "2026-08-01", "Açılış Bakiyesi", 0.0, 0.0, 0.0, "REF-001"),
            AccountTransactionDetail("tx2", "2026-08-03", "Rezervasyon Borç Kaydı (INV-B-2026-001)", account.totalDebit, 0.0, account.totalDebit, "INV-B-2026-001"),
            AccountTransactionDetail("tx3", "2026-08-05", "Banka Havale Tahsilatı", 0.0, account.totalCredit, account.balance, "DEKONT-8821")
        )

        _uiState.value = state.copy(selectedAccountForStatement = account, statementDetails = statement)
    }
}
