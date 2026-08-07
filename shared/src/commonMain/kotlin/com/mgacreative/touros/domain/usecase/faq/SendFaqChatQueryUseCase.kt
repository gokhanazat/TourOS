package com.mgacreative.touros.domain.usecase.faq

import com.mgacreative.touros.domain.model.faq.ChatMessage
import com.mgacreative.touros.domain.repository.FaqChatbotRepository

class SendFaqChatQueryUseCase(
    private val repository: FaqChatbotRepository
) {
    suspend operator fun invoke(queryText: String, tenantId: String): Result<ChatMessage> {
        return repository.sendMessage(queryText, tenantId)
    }
}
