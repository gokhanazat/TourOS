package com.mgacreative.touros.domain.model.faq

import kotlinx.serialization.Serializable

enum class ChatSender {
    USER,
    BOT
}

enum class FaqCategory {
    BOOKING_STATUS,
    CANCELLATION_POLICY,
    VISA_REQUIREMENTS,
    GENERAL
}

@Serializable
data class ChatMessage(
    val id: String,
    val sender: ChatSender,
    val content: String,
    val timestamp: String,
    val matchedCategory: FaqCategory = FaqCategory.GENERAL
)

@Serializable
data class FaqChatbotResponse(
    val responseId: String,
    val botResponse: String,
    val matchedCategory: String
)
