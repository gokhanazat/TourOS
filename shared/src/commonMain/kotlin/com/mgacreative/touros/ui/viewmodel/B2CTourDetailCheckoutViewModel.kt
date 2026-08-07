package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2CCheckoutRequest
import com.mgacreative.touros.domain.model.B2CCheckoutResult
import com.mgacreative.touros.domain.model.B2CTourDetail
import com.mgacreative.touros.domain.usecase.GetB2CTourDetailUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ProcessB2CCheckoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2CTourDetailCheckoutUiState(
    val selectedTab: Int = 0, // 0: Detaylar, 1: Ödeme & Rezervasyon
    val tourDetail: B2CTourDetail = B2CTourDetail(),
    val paxCount: Int = 1,
    val checkoutResult: B2CCheckoutResult? = null,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
) {
    val totalPrice: Double get() = tourDetail.price * paxCount
}

class B2CTourDetailCheckoutViewModel(
    private val getB2CTourDetailUseCase: GetB2CTourDetailUseCase,
    private val processB2CCheckoutUseCase: ProcessB2CCheckoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2CTourDetailCheckoutUiState())
    val uiState: StateFlow<B2CTourDetailCheckoutUiState> = _uiState.asStateFlow()

    fun loadTourDetail(tourId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2CTourDetailUseCase(tourId, tenantId)
            res.onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    tourDetail = detail,
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

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updatePaxCount(count: Int) {
        if (count >= 1) {
            _uiState.value = _uiState.value.copy(paxCount = count)
        }
    }

    fun processCheckout(passengerName: String, phone: String, email: String, cardHolder: String, cardNumber: String, expiry: String, cvv: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = B2CCheckoutRequest(
                tourId = _uiState.value.tourDetail.tourId,
                departureId = "dep-201",
                passengerName = passengerName,
                passengerPhone = phone,
                passengerEmail = email,
                paxCount = _uiState.value.paxCount,
                cardNumberMasked = if (cardNumber.length >= 4) "**** **** **** ${cardNumber.takeLast(4)}" else "**** **** **** 4242",
                cardHolder = cardHolder,
                cardExpiry = expiry,
                cvv = cvv
            )

            val res = processB2CCheckoutUseCase(req, tenantId)
            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkoutResult = result,
                    notificationMessage = "🎉 Ödeme Başarılı! Rezervasyon Kodu: ${result.bookingCode} | Ref: ${result.paymentReference}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Ödeme işlemi gerçekleştirilemedi."
                )
            }
        }
    }
}
