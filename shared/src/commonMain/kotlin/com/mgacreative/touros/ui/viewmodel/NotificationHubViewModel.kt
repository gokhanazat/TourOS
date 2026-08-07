package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.NotificationChannel
import com.mgacreative.touros.domain.model.NotificationPayload
import com.mgacreative.touros.domain.model.NotificationResult
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.SendNotificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationHubUiState(
    val selectedChannel: NotificationChannel = NotificationChannel.PUSH,
    val dispatchHistory: List<NotificationResult> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class NotificationHubViewModel(
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationHubUiState())
    val uiState: StateFlow<NotificationHubUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        val initial = listOf(
            NotificationResult("n1", NotificationChannel.PUSH, "user_hans_99", "Rezervasyon Onayı", "Rezervasyonunuz başarıyla onaylandı.", "SENT", "Firebase/FCM", "2026-08-06 10:00"),
            NotificationResult("n2", NotificationChannel.WHATSAPP, "+905329998877", "Voucher Bağlantısı", "Sayın Hans Müller, seyahat voucher belgeniz: https://touros.storage.supabase.co/v/101", "SENT", "MetaWhatsApp", "2026-08-06 11:30"),
            NotificationResult("n3", NotificationChannel.SMS, "+905329998877", "Kalkış Hatırlatması", "Kapadokya Turu yarın saat 08:30'da kalkacaktır.", "SENT", "Netgsm/SMS", "2026-08-06 12:45")
        )
        _uiState.value = _uiState.value.copy(dispatchHistory = initial)
    }

    fun setSelectedChannel(channel: NotificationChannel) {
        _uiState.value = _uiState.value.copy(selectedChannel = channel)
    }

    fun sendNotification(recipient: String, title: String, content: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val payload = NotificationPayload(
                recipient = recipient,
                title = title.ifBlank { null },
                content = content,
                channel = _uiState.value.selectedChannel
            )

            val res = sendNotificationUseCase(payload, tenantId)
            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dispatchHistory = listOf(result) + _uiState.value.dispatchHistory,
                    notificationMessage = "✅ Bildirim '${result.channel}' Kanalından (${result.provider}) Gönderildi!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Bildirim gönderim hatası."
                )
            }
        }
    }
}
