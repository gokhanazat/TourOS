package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.6 B2C Push Bildirim Modeli.
 */
@Serializable
data class B2CPushNotificationItem(
    @SerialName("notification_id") val notificationId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "REMINDER", // REMINDER, PROMOTION, RESERVATION
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
)

/**
 * 4.2.6 B2C Tur Değerlendirme ve Yorum Talep Modeli.
 */
@Serializable
data class B2CTourReviewRequest(
    val tourId: String = "t101",
    val rating: Double = 5.0,
    val comment: String = ""
)
