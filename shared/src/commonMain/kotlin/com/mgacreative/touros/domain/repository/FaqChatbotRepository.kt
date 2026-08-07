package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.faq.ChatMessage

interface FaqChatbotRepository {
    suspend fun sendMessage(userQuery: String, tenantId: String): Result<ChatMessage>
}
