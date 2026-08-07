package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.engine.HumanSupportHandoffEngine
import com.mgacreative.touros.domain.model.faq.SupportHandoffTicket
import com.mgacreative.touros.domain.repository.HumanSupportHandoffRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class HumanSupportHandoffRepositoryImpl(
    private val engine: HumanSupportHandoffEngine,
    private val supabaseClient: SupabaseClient? = null
) : HumanSupportHandoffRepository {

    override suspend fun initiateHandoff(
        customerId: String,
        chatSummary: String,
        tenantId: String
    ): Result<SupportHandoffTicket> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject {
                    put("p_customer_id", customerId)
                    put("p_chat_summary", chatSummary)
                    put("p_tenant_id", tenantId)
                }
                supabaseClient.postgrest.rpc("initiate_human_support_handoff", params)
                    .decodeSingle<SupportHandoffTicket>()
            } else {
                engine.createHandoffTicket(chatSummary)
            }
        }.recover {
            engine.createHandoffTicket(chatSummary)
        }
    }
}
