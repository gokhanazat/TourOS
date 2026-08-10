package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.DynamicPricingEvaluationResult
import com.mgacreative.touros.domain.model.DynamicPricingRule
import com.mgacreative.touros.domain.usecase.EvaluateDynamicPricingUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetDynamicPricingRulesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DynamicPricingRuleEngineUiState(
    val selectedTab: Int = 0, // 0: Simülatör, 1: Kural Listesi
    val basePrice: Double = 2500.0,
    val selectedSeason: String = "HIGH_SEASON",
    val occupancyRate: Double = 85.0,
    val selectedAgencyTier: String = "VIP_AGENCY",
    val selectedCountry: String = "GERMANY",
    val evaluationResult: DynamicPricingEvaluationResult = DynamicPricingEvaluationResult(),
    val rules: List<DynamicPricingRule> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class DynamicPricingRuleEngineViewModel(
    private val getDynamicPricingRulesUseCase: GetDynamicPricingRulesUseCase,
    private val evaluateDynamicPricingUseCase: EvaluateDynamicPricingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DynamicPricingRuleEngineUiState())
    val uiState: StateFlow<DynamicPricingRuleEngineUiState> = _uiState.asStateFlow()

    init {
        loadRules()
        evaluateRules()
    }

    fun loadRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getDynamicPricingRulesUseCase(tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(rules = list, isLoading = false)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateBasePrice(price: Double) {
        _uiState.value = _uiState.value.copy(basePrice = price)
        evaluateRules()
    }

    fun updateSeason(season: String) {
        _uiState.value = _uiState.value.copy(selectedSeason = season)
        evaluateRules()
    }

    fun updateOccupancyRate(rate: Double) {
        _uiState.value = _uiState.value.copy(occupancyRate = rate)
        evaluateRules()
    }

    fun updateAgencyTier(tier: String) {
        _uiState.value = _uiState.value.copy(selectedAgencyTier = tier)
        evaluateRules()
    }

    fun updateCountry(country: String) {
        _uiState.value = _uiState.value.copy(selectedCountry = country)
        evaluateRules()
    }

    fun evaluateRules() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = evaluateDynamicPricingUseCase(
                basePrice = _uiState.value.basePrice,
                season = _uiState.value.selectedSeason,
                occupancyRate = _uiState.value.occupancyRate,
                agencyTier = _uiState.value.selectedAgencyTier,
                targetCountry = _uiState.value.selectedCountry,
                tenantId = tenantId
            )

            res.onSuccess { eval ->
                _uiState.value = _uiState.value.copy(
                    evaluationResult = eval,
                    notificationMessage = "⚡ Dinamik Fiyat Öncelikli Kural Engine Tarafından Yeniden Hesaplandı!"
                )
            }
        }
    }
}
