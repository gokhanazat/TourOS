package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.NotificationPayload
import com.mgacreative.touros.domain.model.NotificationResult
import com.mgacreative.touros.domain.service.NotificationDispatcherFactory

/**
 * 3.4.4 Push, SMS, WhatsApp ve E-Posta Kanallarına Bildirim Gönderme Use Case.
 */
class SendNotificationUseCase(
    private val factory: NotificationDispatcherFactory
) {
    suspend operator fun invoke(payload: NotificationPayload, tenantId: String): Result<NotificationResult> {
        if (payload.recipient.isBlank()) {
            return Result.failure(IllegalArgumentException("Bildirim alıcısı boş olamaz."))
        }
        val service = factory.getService(payload.channel)
        return service.sendNotification(payload, tenantId)
    }
}
