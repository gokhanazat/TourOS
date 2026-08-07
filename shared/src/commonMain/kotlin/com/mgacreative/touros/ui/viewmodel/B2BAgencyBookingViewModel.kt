package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2BBookingRequest
import com.mgacreative.touros.domain.model.B2BBookingResult
import com.mgacreative.touros.domain.usecase.CreateB2BAgencyBookingUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2BAgencyBookingUiState(
    val selectedDepartureTitle: String = "Kapadokya Balon Turu (15-18 Ağustos)",
    val unitPrice: Double = 2500.0,
    val paxCount: Int = 2,
    val commissionPercentage: Double = 10.0, // %10 Acente İndirimi
    val bookingResult: B2BBookingResult? = null,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
) {
    val totalPrice: Double get() = unitPrice * paxCount
    val commissionAmount: Double get() = totalPrice * (commissionPercentage / 100.0)
    val netPayable: Double get() = totalPrice - commissionAmount
}

class B2BAgencyBookingViewModel(
    private val createB2BAgencyBookingUseCase: CreateB2BAgencyBookingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2BAgencyBookingUiState())
    val uiState: StateFlow<B2BAgencyBookingUiState> = _uiState.asStateFlow()

    fun updatePaxCount(count: Int) {
        if (count >= 1) {
            _uiState.value = _uiState.value.copy(paxCount = count)
        }
    }

    fun submitB2BBooking(name: String, phone: String, email: String, notes: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = B2BBookingRequest(
                agencyId = "acn-101",
                departureId = "dep-201",
                customerName = name,
                customerPhone = phone,
                customerEmail = email,
                paxCount = _uiState.value.paxCount,
                notes = notes,
                useCreditLimit = true
            )

            val res = createB2BAgencyBookingUseCase(req, tenantId)
            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bookingResult = result,
                    notificationMessage = "✅ Rezervasyon Acente Adına Oluşturuldu! Kodu: ${result.bookingCode}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "B2B Rezervasyon oluşturulamadı."
                )
            }
        }
    }
}
