package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.faq.ChatMessage
import com.mgacreative.touros.domain.model.faq.ChatSender
import com.mgacreative.touros.domain.model.faq.SupportHandoffTicket
import com.mgacreative.touros.domain.usecase.faq.InitiateHumanHandoffUseCase
import com.mgacreative.touros.domain.usecase.faq.SendFaqChatQueryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FaqSupportChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome-001",
            sender = ChatSender.BOT,
            content = "Merhaba! TourOS 7/24 Destek Asistanına hoş geldiniz. Rezervasyon durumu, iptal koşulları veya vize gereklilikleri hakkında soru sorabilirsiniz.",
            timestamp = "Şimdi"
        )
    ),
    val handoffTicket: SupportHandoffTicket? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FaqSupportChatViewModel(
    private val sendFaqChatQueryUseCase: SendFaqChatQueryUseCase,
    private val initiateHumanHandoffUseCase: InitiateHumanHandoffUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaqSupportChatUiState())
    val uiState: StateFlow<FaqSupportChatUiState> = _uiState.asStateFlow()

    fun sendUserQuery(query: String, tenantId: String = "tenant-001") {
        if (query.isBlank()) return

        val userMessage = ChatMessage(
            id = "user-${query.hashCode()}",
            sender = ChatSender.USER,
            content = query,
            timestamp = "Şimdi"
        )

        _uiState.update { it.copy(messages = it.messages + userMessage, isLoading = true) }

        viewModelScope.launch {
            val result = sendFaqChatQueryUseCase(query, tenantId)
            result.onSuccess { botMsg ->
                _uiState.update { it.copy(messages = it.messages + botMsg, isLoading = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    fun requestHumanOperator(customerId: String = "cust-001", tenantId: String = "tenant-001") {
        val userMsg = ChatMessage(
            id = "user-handoff-req",
            sender = ChatSender.USER,
            content = "Canlı Müşteri Temsilcisine Bağlanmak İstiyorum",
            timestamp = "Şimdi"
        )
        _uiState.update { it.copy(messages = it.messages + userMsg, isLoading = true) }

        viewModelScope.launch {
            if (initiateHumanHandoffUseCase != null) {
                val res = initiateHumanHandoffUseCase("cust-001", "Müşteri canlı destek talep etti", tenantId)
                res.onSuccess { ticket ->
                    val botMsg = ChatMessage(
                        id = "bot-handoff-${ticket.ticketId}",
                        sender = ChatSender.BOT,
                        content = "Talebiniz alındı! ${ticket.assignedAgentName} size aktarılıyor. Tahmini bekleme süresi: ${ticket.estimatedWaitMinutes} dakika.",
                        timestamp = "Şimdi"
                    )
                    _uiState.update { it.copy(messages = it.messages + botMsg, handoffTicket = ticket, isLoading = false) }
                }.onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            } else {
                val botMsg = ChatMessage(
                    id = "bot-handoff-fallback",
                    sender = ChatSender.BOT,
                    content = "Talebiniz alındı! Müşteri Temsilcisi Zeynep size aktarılıyor. Tahmini bekleme süresi: 2 dakika.",
                    timestamp = "Şimdi"
                )
                _uiState.update { it.copy(messages = it.messages + botMsg, isLoading = false) }
            }
        }
    }
}
