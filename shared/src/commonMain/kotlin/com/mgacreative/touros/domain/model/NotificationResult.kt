package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.4.4 Bildirim Gönderim Sonuç Modeli.
 */
@Serializable
data class NotificationResult(
    @SerialName("notification_id") val id: String = "",
    val channel: NotificationChannel = NotificationChannel.PUSH,
    val recipient: String = "",
    val title: String? = null,
    val content: String = "",
    val status: String = "SENT", // PENDING, SENT, FAILED
    val provider: String = "FCM",
    @SerialName("created_at") val createdAt: String = ""
)
