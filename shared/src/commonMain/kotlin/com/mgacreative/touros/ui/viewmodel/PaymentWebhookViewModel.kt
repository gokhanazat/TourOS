package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.PaymentWebhookSyncResult
import com.mgacreative.touros.domain.usecase.ProcessPaymentWebhookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WebhookLogItem(
    val timestamp: String,
    val linkCode: String,
    val provider: String,
    val transactionId: String,
    val status: String
)

sealed interface PaymentWebhookUiState {
    data object Idle : PaymentWebhookUiState
    data object Processing : PaymentWebhookUiState
    data class Success(
        val lastSyncResult: PaymentWebhookSyncResult? = null,
        val logs: List<WebhookLogItem> = emptyList(),
        val notificationMessage: String? = null
    ) : PaymentWebhookUiState
    data class Error(val message: String) : PaymentWebhookUiState
}

class PaymentWebhookViewModel(
    private val processPaymentWebhookUseCase: ProcessPaymentWebhookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentWebhookUiState>(
        PaymentWebhookUiState.Success(
            logs = listOf(
                WebhookLogItem("13:00:12", "cs_live_981238", "stripe", "pi_3M98123490", "🟢 SYNC_SUCCESS (PAID)"),
                WebhookLogItem("12:45:00", "iyzi_link_441209", "iyzico", "iyzi_tx_554123", "🟢 SYNC_SUCCESS (PAID)")
            )
        )
    )
    val uiState: StateFlow<PaymentWebhookUiState> = _uiState.asStateFlow()

    fun triggerWebhookCallback(linkCode: String, provider: String, txId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? PaymentWebhookUiState.Success ?: return@launch
            _uiState.value = PaymentWebhookUiState.Processing

            val res = processPaymentWebhookUseCase(linkCode, txId, provider)
            res.onSuccess { syncRes ->
                val newLog = WebhookLogItem("13:06:10", linkCode, provider, txId, "🟢 ${syncRes.syncStatus}")
                _uiState.value = currentState.copy(
                    lastSyncResult = syncRes,
                    logs = listOf(newLog) + currentState.logs,
                    notificationMessage = "⚡ Webhook Başarıyla İşlendi! Rezervasyon & Fatura 'PAID' Durumuna Güncellendi."
                )
            }.onFailure { err ->
                _uiState.value = PaymentWebhookUiState.Error(err.message ?: "Webhook işleme hatası.")
            }
        }
    }
}
