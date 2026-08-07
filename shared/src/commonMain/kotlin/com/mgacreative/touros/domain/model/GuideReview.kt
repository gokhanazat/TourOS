package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.5.4 Rehber Müşteri Değerlendirmesi Domain Modeli.
 */
@Serializable
data class GuideReview(
    val id: String = "",
    val guideId: String = "",
    val departureId: String = "",
    val bookingId: String? = null,
    val customerName: String = "",
    val rating: Int = 5, // 1 - 5 Yıldız
    val comment: String? = null,
    val tenantId: String = "",
    val createdAt: String = ""
)
