package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * TourOS - Kanonik Tur Operatörü Modeli
 * Yalnızca tanımlı ve aktif operatörlerin turlarının çekilmesini ve aranmasını sağlar.
 */
@Serializable
data class CanonicalOperator(
    @SerialName("id") val id: Int = 0,
    @SerialName("canonical_name") val canonicalName: String = "",
    @SerialName("tourvisor_code") val tourvisorCode: Int? = null,
    @SerialName("aliases") val aliases: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 0
)
