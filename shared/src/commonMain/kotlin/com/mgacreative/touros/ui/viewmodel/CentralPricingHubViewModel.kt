package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.CentralPricingRequest
import com.mgacreative.touros.domain.model.CentralPricingResponse
import com.mgacreative.touros.domain.usecase.CentralPricingEngineUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CentralPricingHubUiState(
    val selectedChannel: String = "B2C", // B2C, B2B_AGENCY, ADMIN_PANEL
    val basePrice: Double = 2500.0,
    val paxCount: Int = 2,
    val couponCode: String = "SUMMER2026",
    val daysToDeparture: Int = 45,
    val occupancyRate: Double = 85.0,
    val agencyTier: String = "VIP_AGENCY",
    val country: String = "GERMANY",
    val response: CentralPricingResponse = CentralPricingResponse(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class CentralPricingHubViewModel(
    private val centralPricingEngineUseCase: CentralPricingEngineUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CentralPricingHubUiState())
    val uiState: StateFlow<CentralPricingHubUiState> = _uiState.asStateFlow()

    init {
        calculatePricing()
    }

    fun selectChannel(channel: String) {
        _uiState.value = _uiState.value.copy(selectedChannel = channel)
        calculatePricing()
    }

    fun updateInputs(
        basePrice: Double = _uiState.value.basePrice,
        paxCount: Int = _uiState.value.paxCount,
        couponCode: String = _uiState.value.couponCode,
        daysToDeparture: Int = _uiState.value.daysToDeparture,
        occupancyRate: Double = _uiState.value.occupancyRate,
        agencyTier: String = _uiState.value.agencyTier,
        country: String = _uiState.value.country
    ) {
        _uiState.value = _uiState.value.copy(
            basePrice = basePrice,
            paxCount = paxCount,
            couponCode = couponCode,
            daysToDeparture = daysToDeparture,
            occupancyRate = occupancyRate,
            agencyTier = agencyTier,
            country = country
        )
        calculatePricing()
    }

    fun calculatePricing() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = CentralPricingRequest(
                channel = _uiState.value.selectedChannel,
                basePrice = _uiState.value.basePrice,
                paxCount = _uiState.value.paxCount,
                couponCode = _uiState.value.couponCode,
                daysToDeparture = _uiState.value.daysToDeparture,
                occupancyRate = _uiState.value.occupancyRate,
                agencyTier = _uiState.value.agencyTier,
                country = _uiState.value.country
            )

            val res = centralPricingEngineUseCase(req, tenantId)
            res.onSuccess { pricingRes ->
                _uiState.value = _uiState.value.copy(
                    response = pricingRes,
                    isLoading = false,
                    notificationMessage = "🎯 Tek Merkezi PricingEngine Tarafından Tutarlı Fiyat Hesaplandı!"
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
