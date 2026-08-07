package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.SaveDynamicPricingRuleRequest
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.SaveDynamicPricingRuleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DynamicPricingRuleFormUiState(
    val selectedTab: Int = 0, // 0: Form, 1: Fiyat Önizleme Simülatörü
    val ruleName: String = "Yüksek Sezon & VIP Acente Fiyat Ayarı",
    val priority: Int = 1,
    val season: String = "HIGH_SEASON",
    val minOccupancyRate: Double = 80.0,
    val agencyTier: String = "VIP_AGENCY",
    val targetCountry: String = "GERMANY",
    val priceAdjustmentPercent: Double = 15.0,
    val sampleBasePrice: Double = 3000.0,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
) {
    val simulatedPrice: Double
        get() = sampleBasePrice * (1.0 + (priceAdjustmentPercent / 100.0))
}

class DynamicPricingRuleFormViewModel(
    private val saveDynamicPricingRuleUseCase: SaveDynamicPricingRuleUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DynamicPricingRuleFormUiState())
    val uiState: StateFlow<DynamicPricingRuleFormUiState> = _uiState.asStateFlow()

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateForm(
        ruleName: String = _uiState.value.ruleName,
        priority: Int = _uiState.value.priority,
        season: String = _uiState.value.season,
        minOccupancyRate: Double = _uiState.value.minOccupancyRate,
        agencyTier: String = _uiState.value.agencyTier,
        targetCountry: String = _uiState.value.targetCountry,
        priceAdjustmentPercent: Double = _uiState.value.priceAdjustmentPercent,
        sampleBasePrice: Double = _uiState.value.sampleBasePrice
    ) {
        _uiState.value = _uiState.value.copy(
            ruleName = ruleName,
            priority = priority,
            season = season,
            minOccupancyRate = minOccupancyRate,
            agencyTier = agencyTier,
            targetCountry = targetCountry,
            priceAdjustmentPercent = priceAdjustmentPercent,
            sampleBasePrice = sampleBasePrice
        )
    }

    fun saveRule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = SaveDynamicPricingRuleRequest(
                ruleName = _uiState.value.ruleName,
                priority = _uiState.value.priority,
                season = _uiState.value.season,
                minOccupancyRate = _uiState.value.minOccupancyRate,
                agencyTier = _uiState.value.agencyTier,
                targetCountry = _uiState.value.targetCountry,
                priceAdjustmentPercent = _uiState.value.priceAdjustmentPercent
            )

            val res = saveDynamicPricingRuleUseCase(req, tenantId)
            res.onSuccess { savedRule ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notificationMessage = "✅ Dinamik Fiyatlandırma Kuralı Öncelik #${savedRule.priority} İle Başarıyla Kaydedildi!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Kural kaydedilemedi."
                )
            }
        }
    }
}
