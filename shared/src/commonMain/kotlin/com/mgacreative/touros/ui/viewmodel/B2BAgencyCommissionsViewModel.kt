package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2BAgencyCommissionItem
import com.mgacreative.touros.domain.usecase.GetB2BAgencyCommissionsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2BAgencyCommissionsUiState(
    val selectedPeriod: String = "Tüm Dönemler",
    val availablePeriods: List<String> = listOf("Tüm Dönemler", "Ağustos 2026", "Temmuz 2026", "Haziran 2026"),
    val commissions: List<B2BAgencyCommissionItem> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
) {
    val totalGrossSales: Double get() = commissions.sumOf { it.grossSalesAmount }
    val totalEarnedCommission: Double get() = commissions.sumOf { it.commissionAmount }
    val paidCommission: Double get() = commissions.filter { it.status == "ODENDI" }.sumOf { it.commissionAmount }
    val pendingCommission: Double get() = commissions.filter { it.status != "ODENDI" }.sumOf { it.commissionAmount }
}

class B2BAgencyCommissionsViewModel(
    private val getB2BAgencyCommissionsUseCase: GetB2BAgencyCommissionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2BAgencyCommissionsUiState())
    val uiState: StateFlow<B2BAgencyCommissionsUiState> = _uiState.asStateFlow()

    init {
        loadCommissions()
    }

    fun loadCommissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2BAgencyCommissionsUseCase(tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    commissions = list,
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

    fun selectPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }
}
