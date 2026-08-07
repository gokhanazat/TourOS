package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.gateway.PaymentGatewayFactory
import com.mgacreative.touros.domain.gateway.PaymentRequest
import com.mgacreative.touros.domain.gateway.PaymentResponse
import com.mgacreative.touros.domain.usecase.ExecutePaymentUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.RefundPaymentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PaymentGatewayUiState {
    data object Idle : PaymentGatewayUiState
    data object Processing : PaymentGatewayUiState
    data class Success(
        val activeProvider: String = "iyzico",
        val availableProviders: List<String> = listOf("iyzico", "stripe", "mock"),
        val lastPaymentResponse: PaymentResponse? = null,
        val notificationMessage: String? = null
    ) : PaymentGatewayUiState
    data class Error(val message: String) : PaymentGatewayUiState
}

class PaymentGatewayViewModel(
    private val executePaymentUseCase: ExecutePaymentUseCase,
    private val refundPaymentUseCase: RefundPaymentUseCase,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentGatewayUiState>(
        PaymentGatewayUiState.Success(
            activeProvider = "iyzico",
            availableProviders = paymentGatewayFactory.getAvailableProviders()
        )
    )
    val uiState: StateFlow<PaymentGatewayUiState> = _uiState.asStateFlow()

    fun setProvider(provider: String) {
        val currentState = _uiState.value as? PaymentGatewayUiState.Success ?: return
        _uiState.value = currentState.copy(activeProvider = provider)
    }

    fun executePayment(
        cardNumber: String,
        holderName: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        amount: Double
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value as? PaymentGatewayUiState.Success ?: return@launch
            _uiState.value = PaymentGatewayUiState.Processing

            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = PaymentRequest(
                bookingId = "b-test",
                amount = amount,
                currency = "TRY",
                cardHolderName = holderName,
                cardNumber = cardNumber,
                expireMonth = expireMonth,
                expireYear = expireYear,
                cvc = cvc,
                customerEmail = user?.email ?: "customer@example.com",
                tenantId = tenantId
            )

            val res = executePaymentUseCase(currentState.activeProvider, req)
            res.onSuccess { response ->
                val msg = if (response.isSuccess) {
                    "✅ Ödeme (${currentState.activeProvider.uppercase()}) ile Başarıyla Alındı! İşlem ID: ${response.transactionId}"
                } else {
                    "❌ Ödeme Başarısız (${currentState.activeProvider.uppercase()}): ${response.errorMessage}"
                }
                _uiState.value = currentState.copy(
                    lastPaymentResponse = response,
                    notificationMessage = msg
                )
            }.onFailure { err ->
                _uiState.value = PaymentGatewayUiState.Error(err.message ?: "Ödeme işlemi başarısız.")
            }
        }
    }
}
