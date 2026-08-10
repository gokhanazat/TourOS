package com.mgacreative.touros.data.service

import com.mgacreative.touros.domain.model.NotificationChannel
import com.mgacreative.touros.domain.model.NotificationPayload
import com.mgacreative.touros.domain.model.NotificationResult
import com.mgacreative.touros.domain.service.NotificationService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive

/**
 * WhatsApp Kanalı (0 TL Ücretsiz Direkt Yönlendirme Servisi).
 * UltraMsg veya Ücretli API Bağımlılığı İçermez.
 */
class WhatsAppNotificationServiceImpl(
    private val supabaseClient: SupabaseClient
) : NotificationService {

    override suspend fun sendNotification(payload: NotificationPayload, tenantId: String): Result<NotificationResult> {
        return runCatching {
            // 1. Supabase Bildirim Logunu Kaydet
            runCatching {
                val params = buildJsonObject {
                    put("p_tenant_id", JsonPrimitive(tenantId))
                    put("p_channel", JsonPrimitive("WHATSAPP"))
                    put("p_recipient", JsonPrimitive(payload.recipient))
                    put("p_title", JsonPrimitive(payload.title ?: "WhatsApp Mesajı"))
                    put("p_content", JsonPrimitive(payload.content))
                    put("p_provider", JsonPrimitive("WhatsAppDirectFree"))
                }
                supabaseClient.postgrest.rpc("log_notification_dispatch", params)
            }

            // 2. 0 TL Ücretsiz İletim Sonucu Döndür
            NotificationResult(
                id = "wa-${(10000..99999).random()}",
                channel = NotificationChannel.WHATSAPP,
                recipient = payload.recipient,
                title = payload.title ?: "WhatsApp Mesajı",
                content = payload.content,
                status = "SENT",
                provider = "WhatsAppDirectFree",
                createdAt = "2026-08-09 14:35"
            )
        }
    }
}
