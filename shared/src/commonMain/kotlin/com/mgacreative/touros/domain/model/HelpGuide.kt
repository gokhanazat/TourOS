package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sayfa İçi Akıllı Yardım & Soru-Cevap Rehberi Domain Modeli.
 */
@Serializable
data class HelpGuide(
    val id: String = "",
    @SerialName("screen_route") val screenRoute: String,
    @SerialName("field_key") val fieldKey: String? = null,
    val category: String = "GENEL",
    val lang: String = "tr",
    val title: String,
    val question: String,
    val answer: String,
    @SerialName("step_order") val stepOrder: Int = 1,
    @SerialName("is_active") val isActive: Boolean = true
)
