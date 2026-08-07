package com.mgacreative.touros.domain.service

import com.mgacreative.touros.domain.model.NotificationPayload
import com.mgacreative.touros.domain.model.NotificationResult

/**
 * 3.4.4 Push, SMS, WhatsApp ve E-Posta Kanallarını Soyutlayan Ortak NotificationService Arayüzü.
 */
interface NotificationService {
    suspend fun sendNotification(payload: NotificationPayload, tenantId: String): Result<NotificationResult>
}
