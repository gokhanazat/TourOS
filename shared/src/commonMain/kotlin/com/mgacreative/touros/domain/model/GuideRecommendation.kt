package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.5.2 Akıllı Rehber Önerisi & Atama Domain Modeli.
 */
@Serializable
data class GuideRecommendation(
    val guide: Guide,
    val isAvailable: Boolean = true,
    val matchScore: Int = 100, // % 100 Eşleşme Skoru
    val languageMatch: Boolean = true,
    val recommendationReason: String = "Tur dili ile birebir eşleşiyor & ⭐ 5.0 Puan"
)
