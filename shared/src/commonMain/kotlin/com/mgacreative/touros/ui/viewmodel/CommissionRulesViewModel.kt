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

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CommissionRulesUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getCommissionRulesUseCase(tenantId)
            val fetched = res.getOrDefault(emptyList())

            val rules = if (fetched.isEmpty()) {
                listOf(
                    CommissionRule("cr1", "Jolly Tur Özel Oranı", "a1", "Jolly Tur", null, null, "percentage", 12.5, 0.0, "TRY", true, tenantId),
                    CommissionRule("cr2", "Kapadokya Turu VIP Sabit Komisyon", null, null, "t1", "Kapadokya Balon Turu", "fixed_amount", 0.0, 750.0, "TRY", true, tenantId),
                    CommissionRule("cr3", "Standart Acente Komisyonu", null, null, null, null, "percentage", 8.0, 0.0, "TRY", true, tenantId)
                )
            } else {
                fetched
            }

            _uiState.value = CommissionRulesUiState.Success(rules = rules)
        }
    }

    fun saveRule(
        ruleName: String,
        agentName: String?,
        tourName: String?,
        calculationType: String,
        rateValue: Double,
        fixedAmount: Double
    ) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val rule = CommissionRule(
                ruleName = ruleName,
                agentName = agentName.ifNullOrBlank(),
                tourName = tourName.ifNullOrBlank(),
                calculationType = calculationType,
                rateValue = rateValue,
                fixedAmount = fixedAmount,
                currency = "TRY",
                isActive = true,
                tenantId = tenantId
            )

            val res = saveCommissionRuleUseCase(rule)
            res.onSuccess {
                loadData()
            }.onFailure { err ->
                _uiState.value = CommissionRulesUiState.Error(err.message ?: "Kural kaydedilemedi.")
            }
        }
    }

    fun simulateCalculation(bookingPrice: Double, rule: CommissionRule) {
        val state = _uiState.value as? CommissionRulesUiState.Success ?: return
        val calculated = commissionCalculationEngine.calculateCommissionAmount(bookingPrice, rule)
        val typeLabel = if (rule.calculationType == "percentage") "%${rule.rateValue} Oran" else "${rule.fixedAmount} TRY Sabit Tutar"

        _uiState.value = state.copy(
            simulatedAmount = calculated,
            simulatedResultText = "🧮 ${bookingPrice} TRY satış için '${rule.ruleName}' ($typeLabel) uyarınca komisyon tutarı: ${calculated} TRY"
        )
    }

    private fun String?.ifNullOrBlank(): String? = if (this.isNullOrBlank()) null else this
}
