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
    val selectedDepartureId: String? = null,
    val selectedDepartureDate: String? = null,
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
                val firstDeparture = detail.availableDepartures.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    tourDetail = detail,
                    selectedDepartureId = firstDeparture?.id,
                    selectedDepartureDate = firstDeparture?.departureDate,
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

    fun selectDeparture(departureId: String, date: String) {
        val selectedDep = _uiState.value.tourDetail.availableDepartures.find { it.id == departureId || it.departureDate == date }
        val updatedPrice = selectedDep?.price?.takeIf { it > 0 } ?: _uiState.value.tourDetail.price
        _uiState.value = _uiState.value.copy(
            selectedDepartureId = departureId,
            selectedDepartureDate = date,
            tourDetail = _uiState.value.tourDetail.copy(price = updatedPrice)
        )
    }

    fun processCheckout(
        passengerName: String,
        phone: String,
        email: String,
        paymentMethod: String = "KREDİ_KARTI",
        cardHolder: String = "",
        cardNumber: String = "",
        expiry: String = "",
        cvv: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = B2CCheckoutRequest(
                tourId = _uiState.value.tourDetail.tourId,
                departureId = _uiState.value.selectedDepartureId ?: "dep-201",
                passengerName = passengerName,
                passengerPhone = phone,
                passengerEmail = email,
                paxCount = _uiState.value.paxCount,
                totalAmount = _uiState.value.totalPrice,
                cardNumberMasked = if (cardNumber.length >= 4) "**** **** **** ${cardNumber.takeLast(4)}" else if (paymentMethod == "NAKİT") "NAKİT ÖDEME" else if (paymentMethod == "PAYPAL") "PAYPAL ÖDEME" else "BANKA HAVALESİ",
                cardHolder = cardHolder.ifBlank { passengerName },
                cardExpiry = expiry,
                cvv = cvv
            )

            val res = processB2CCheckoutUseCase(req, tenantId)
            res.onSuccess { result ->
                val message = when (paymentMethod) {
                    "BANKA_HAVALESİ" -> "🎉 Havale/EFT Talebi Alındı! Rezervasyon Kodu: ${result.bookingCode}. Ödeme onayının ardından rezervasyonunuz kesinleşecektir."
                    "NAKİT" -> "🎉 Nakit Ödeme Talebi Alındı! Rezervasyon Kodu: ${result.bookingCode}. Ödeme tur günü tahsil edilecektir."
                    else -> "🎉 Kredi Kartı Ödemesi Başarılı! Rezervasyon Kodu: ${result.bookingCode} | Ref: ${result.paymentReference}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkoutResult = result,
                    notificationMessage = message
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "İşlem gerçekleştirilemedi."
                )
            }
        }
    }
}
