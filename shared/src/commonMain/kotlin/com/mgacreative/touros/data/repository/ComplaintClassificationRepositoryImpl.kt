package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.engine.ComplaintClassifierEngine
import com.mgacreative.touros.domain.model.feedback.ClassifiedComplaint
import com.mgacreative.touros.domain.repository.ComplaintClassificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ComplaintClassificationRepositoryImpl(
    private val engine: ComplaintClassifierEngine,
    private val supabaseClient: SupabaseClient? = null
) : ComplaintClassificationRepository {

    override suspend fun classifyComplaint(
        complaintText: String,
        tenantId: String
    ): Result<ClassifiedComplaint> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject {
                    put("p_complaint_text", complaintText)
                    put("p_tenant_id", tenantId)
                }
                supabaseClient.postgrest.rpc("classify_and_tag_complaint", params)
                    .decodeSingle<ClassifiedComplaint>()
            } else {
                engine.classifyText(complaintText)
            }
        }.recover {
            engine.classifyText(complaintText)
        }
    }
}
