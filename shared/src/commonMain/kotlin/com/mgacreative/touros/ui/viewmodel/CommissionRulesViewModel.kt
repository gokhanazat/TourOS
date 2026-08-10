package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.engine.CommissionCalculationEngine
import com.mgacreative.touros.domain.model.CommissionRule
import com.mgacreative.touros.domain.usecase.GetCommissionRulesUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.SaveCommissionRuleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CommissionRulesUiState {
    data object Loading : CommissionRulesUiState
    data class Success(
        val rules: List<CommissionRule> = emptyList(),
        val simulatedAmount: Double = 0.0,
        val simulatedResultText: String? = null,
        val isSaving: Boolean = false,
        val notificationMessage: String? = null
    ) : CommissionRulesUiState
    data class Error(val message: String) : CommissionRulesUiState
}

class CommissionRulesViewModel(
    private val getCommissionRulesUseCase: GetCommissionRulesUseCase,
    private val saveCommissionRuleUseCase: SaveCommissionRuleUseCase,
    private val commissionCalculationEngine: CommissionCalculationEngine,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommissionRulesUiState>(CommissionRulesUiState.Loading)
    val uiState: StateFlow<CommissionRulesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun resolveTenantId(userTenantId: String?): String {
        val tid = userTenantId?.trim()
        return if (!tid.isNullOrBlank() && tid != "tenant_id") tid else "00000000-0000-0000-0000-000000000001"
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CommissionRulesUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val res = getCommissionRulesUseCase(tenantId)
            val rules = res.getOrDefault(emptyList())

            _uiState.value = CommissionRulesUiState.Success(rules = rules)
        }
    }

    fun saveNewRule(
        ruleName: String,
        agencyName: String?,
        tourTitle: String?,
        calculationType: String, // "percentage" vs "fixed_amount"
        ratePercent: Double,
        fixedAmount: Double
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value as? CommissionRulesUiState.Success ?: return@launch
            _uiState.value = currentState.copy(isSaving = true)

            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val newRule = CommissionRule(
                ruleName = ruleName,
                agentId = if (!agencyName.isNullOrBlank()) "a-${(100..999).random()}" else null,
                agentName = agencyName.takeIf { !it.isNullOrBlank() },
                tourId = if (!tourTitle.isNullOrBlank()) "t-${(100..999).random()}" else null,
                tourName = tourTitle.takeIf { !it.isNullOrBlank() },
                calculationType = calculationType,
                rateValue = ratePercent,
                fixedAmount = fixedAmount,
                currency = "TRY",
                isActive = true,
                tenantId = tenantId
            )

            val res = saveCommissionRuleUseCase(newRule)
            res.onSuccess {
                loadData()
            }.onFailure { err ->
                _uiState.value = CommissionRulesUiState.Error(err.message ?: "Komisyon kuralı kaydedilemedi.")
            }
        }
    }

    fun simulateCommission(salesAmount: Double, selectedRule: CommissionRule?) {
        val state = _uiState.value as? CommissionRulesUiState.Success ?: return
        if (selectedRule == null || salesAmount <= 0) {
            _uiState.value = state.copy(simulatedAmount = 0.0, simulatedResultText = null)
            return
        }

        val calculated = if (selectedRule.calculationType == "percentage") {
            salesAmount * (selectedRule.rateValue / 100.0)
        } else {
            selectedRule.fixedAmount
        }

        val text = "💡 Satış Tutarı: ₺$salesAmount ➔ Hakediş Komisyon: ₺$calculated (${selectedRule.ruleName})"
        _uiState.value = state.copy(
            simulatedAmount = calculated,
            simulatedResultText = text
        )
    }
}
