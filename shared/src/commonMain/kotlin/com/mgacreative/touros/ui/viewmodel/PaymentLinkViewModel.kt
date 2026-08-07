package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.gateway.PaymentLinkInfo
import com.mgacreative.touros.domain.usecase.CreatePaymentLinkUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PaymentLinkUiState {
    data object Loading : PaymentLinkUiState
    data class Success(
        val links: List<PaymentLinkInfo> = emptyList(),
        val selectedProvider: String = "stripe",
        val notificationMessage: String? = null,
        val createdCheckoutUrl: String? = null
    ) : PaymentLinkUiState
    data class Error(val message: String) : PaymentLinkUiState
}

class PaymentLinkViewModel(
    private val createPaymentLinkUseCase: CreatePaymentLinkUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentLinkUiState>(PaymentLinkUiState.Loading)
    val uiState: StateFlow<PaymentLinkUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PaymentLinkUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val fallbackLinks = listOf(
                PaymentLinkInfo("pl1", "cs_live_981238", "b1", 12000.0, "TRY", "stripe", "https://checkout.stripe.com/c/pay/cs_live_981238", "PENDING", "2026-08-07 13:00", "hans@example.com", "+49 151 123456", tenantId),
                PaymentLinkInfo("pl2", "iyzi_link_441209", "b2", 18000.0, "TRY", "iyzico", "https://pay.iyzipay.com/link/iyzi_link_441209", "PAID", "2026-08-06 18:00", "sarah@example.com", "+44 7700 900077", tenantId)
            )

            _uiState.value = PaymentLinkUiState.Success(links = fallbackLinks)
        }
    }

    fun setProvider(provider: String) {
        val state = _uiState.value as? PaymentLinkUiState.Success ?: return
        _uiState.value = state.copy(selectedProvider = provider)
    }

    fun generateLink(bookingId: String, amount: Double, customerEmail: String?) {
        viewModelScope.launch {
            val state = _uiState.value as? PaymentLinkUiState.Success ?: return@launch

            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = createPaymentLinkUseCase(
                providerName = state.selectedProvider,
                bookingId = bookingId,
                amount = amount,
                currency = "TRY",
                customerEmail = customerEmail,
                tenantId = tenantId
            )

            res.onSuccess { linkInfo ->
                _uiState.value = state.copy(
                    links = listOf(linkInfo) + state.links,
                    notificationMessage = "🚀 ${state.selectedProvider.uppercase()} Ödeme Linki Oluşturuldu: ${linkInfo.checkoutUrl}",
                    createdCheckoutUrl = linkInfo.checkoutUrl
                )
            }.onFailure { err ->
                _uiState.value = PaymentLinkUiState.Error(err.message ?: "Ödeme linki oluşturulamadı.")
            }
        }
    }
}
