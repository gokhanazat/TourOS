package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.engine.FaqChatbotEngine
import com.mgacreative.touros.domain.model.faq.ChatMessage
import com.mgacreative.touros.domain.model.faq.ChatSender
import com.mgacreative.touros.domain.model.faq.FaqCategory
import com.mgacreative.touros.domain.model.faq.FaqChatbotResponse
import com.mgacreative.touros.domain.repository.FaqChatbotRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FaqChatbotRepositoryImpl(
    private val engine: FaqChatbotEngine,
    private val supabaseClient: SupabaseClient? = null
) : FaqChatbotRepository {

    override suspend fun sendMessage(userQuery: String, tenantId: String): Result<ChatMessage> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject {
                    put("p_query_text", userQuery)
                    put("p_tenant_id", tenantId)
                }
                val resp = supabaseClient.postgrest.rpc("process_faq_chatbot_query", params)
                    .decodeSingle<FaqChatbotResponse>()
                
                val category = runCatching { FaqCategory.valueOf(resp.matchedCategory) }.getOrDefault(FaqCategory.GENERAL)
                ChatMessage(
                    id = resp.responseId,
                    sender = ChatSender.BOT,
                    content = resp.botResponse,
                    timestamp = "Şimdi",
                    matchedCategory = category
                )
            } else {
                val (botAnswer, category) = engine.matchAnswer(userQuery)
                ChatMessage(
                    id = "msg-${userQuery.hashCode()}",
                    sender = ChatSender.BOT,
                    content = botAnswer,
                    timestamp = "Şimdi",
                    matchedCategory = category
                )
            }
        }.recover {
            val (botAnswer, category) = engine.matchAnswer(userQuery)
            ChatMessage(
                id = "msg-${userQuery.hashCode()}",
                sender = ChatSender.BOT,
                content = botAnswer,
                timestamp = "Şimdi",
                matchedCategory = category
            )
        }
    }
}
