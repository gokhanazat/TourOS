package com.mgacreative.touros.domain.service

import com.mgacreative.touros.domain.model.NotificationChannel

/**
 * 3.4.4 Seçilen Bildirim Kanalına Göre Doğru Implemetasyonu Döndüren Fabrika (Factory).
 */
class NotificationDispatcherFactory(
    private val pushService: NotificationService,
    private val smsService: NotificationService,
    private val whatsAppService: NotificationService,
    private val emailService: NotificationService
) {
    fun getService(channel: NotificationChannel): NotificationService {
        return when (channel) {
            NotificationChannel.PUSH -> pushService
            NotificationChannel.SMS -> smsService
            NotificationChannel.WHATSAPP -> whatsAppService
            NotificationChannel.EMAIL -> emailService
        }
    }
}
