package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.4.4 Bildirim Kanalları Enum.
 */
@Serializable
enum class NotificationChannel {
    PUSH,
    SMS,
    WHATSAPP,
    EMAIL
}
