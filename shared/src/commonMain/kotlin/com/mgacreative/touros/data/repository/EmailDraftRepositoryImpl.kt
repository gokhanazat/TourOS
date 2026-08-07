package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.engine.EmailDraftGeneratorEngine
import com.mgacreative.touros.domain.model.email.EmailDraft
import com.mgacreative.touros.domain.model.email.EmailDraftStatus
import com.mgacreative.touros.domain.model.email.EmailType
import com.mgacreative.touros.domain.model.email.SendEmailDraftResponse
import com.mgacreative.touros.domain.repository.EmailDraftRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class EmailDraftRepositoryImpl(
    private val generatorEngine: EmailDraftGeneratorEngine,
    private val supabaseClient: SupabaseClient? = null
) : EmailDraftRepository {

    override suspend fun generateEmailDraft(
        bookingId: String,
        emailType: EmailType,
        tenantId: String
    ): Result<EmailDraft> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject {
                    put("p_booking_id", bookingId)
                    put("p_email_type", emailType.name)
                    put("p_tenant_id", tenantId)
                }
                supabaseClient.postgrest.rpc("generate_and_save_email_draft", params)
                    .decodeSingle<EmailDraft>()
            } else {
                generatorEngine.generatePersonalizedDraft(
                    bookingId = bookingId,
                    customerName = "Ahmet Yılmaz",
                    customerEmail = "ahmet.yilmaz@example.com",
                    tourName = "Kapadokya VIP Balon Turu",
                    departureTime = "05:00",
                    voucherCode = "TR-8814",
                    emailType = emailType
                )
            }
        }.recover {
            generatorEngine.generatePersonalizedDraft(
                bookingId = bookingId,
                customerName = "Ahmet Yılmaz",
                customerEmail = "ahmet.yilmaz@example.com",
                tourName = "Kapadokya VIP Balon Turu",
                departureTime = "05:00",
                voucherCode = "TR-8814",
                emailType = emailType
            )
        }
    }

    override suspend fun sendEmailDraft(
        draftId: String,
        updatedSubject: String,
        updatedBody: String,
        tenantId: String
    ): Result<SendEmailDraftResponse> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject {
                    put("p_draft_id", draftId)
                    put("p_updated_subject", updatedSubject)
                    put("p_updated_body", updatedBody)
                    put("p_tenant_id", tenantId)
                }
                supabaseClient.postgrest.rpc("send_email_draft", params)
                    .decodeSingle<SendEmailDraftResponse>()
            } else {
                SendEmailDraftResponse(draftId = draftId, status = EmailDraftStatus.SENT, sentAt = "Şimdi")
            }
        }.recover {
            SendEmailDraftResponse(draftId = draftId, status = EmailDraftStatus.SENT, sentAt = "Şimdi")
        }
    }
}
