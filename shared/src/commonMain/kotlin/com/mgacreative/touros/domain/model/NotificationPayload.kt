package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.4.4 Bildirim Gönderim Yükü (Payload) Modeli.
 */
@Serializable
data class NotificationPayload(
    val recipient: String,
    val title: String? = null,
    val content: String,
    val channel: NotificationChannel = NotificationChannel.PUSH,
    val metadata: Map<String, String> = emptyMap()
)
