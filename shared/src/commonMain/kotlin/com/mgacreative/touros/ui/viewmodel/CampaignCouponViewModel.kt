package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.CampaignCouponCalculationResult
import com.mgacreative.touros.domain.usecase.ApplyCampaignCouponUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CampaignCouponUiState(
    val originalPrice: Double = 2500.0,
    val daysToDeparture: Int = 45,
    val couponCode: String = "SUMMER2026",
    val result: CampaignCouponCalculationResult = CampaignCouponCalculationResult(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class CampaignCouponViewModel(
    private val applyCampaignCouponUseCase: ApplyCampaignCouponUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignCouponUiState())
    val uiState: StateFlow<CampaignCouponUiState> = _uiState.asStateFlow()

    init {
        calculateDiscount()
    }

    fun updateCouponCode(code: String) {
        _uiState.value = _uiState.value.copy(couponCode = code)
    }

    fun updateDaysToDeparture(days: Int) {
        _uiState.value = _uiState.value.copy(daysToDeparture = days)
        calculateDiscount()
    }

    fun calculateDiscount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = applyCampaignCouponUseCase(
                couponCode = _uiState.value.couponCode,
                originalPrice = _uiState.value.originalPrice,
                daysToDeparture = _uiState.value.daysToDeparture,
                tenantId = tenantId
            )

            res.onSuccess { calcResult ->
                _uiState.value = _uiState.value.copy(
                    result = calcResult,
                    isLoading = false,
                    notificationMessage = "🏷️ Kampanya ve Kupon İndirimi Otomatik Uygulandı!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }
}
