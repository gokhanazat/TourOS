package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.3.1 Otel Domain Modeli (Ad, Konum, Yıldız, Açıklama, Fotoğraf).
 */
@Serializable
data class Hotel(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val starRating: Int? = 4,
    val address: String? = null,
    val city: String? = null,
    val country: String = "Türkiye",
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isActive: Boolean = true,
    val tenantId: String = ""
)
