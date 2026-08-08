package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * agency_published_tours tablosu – Acentenin operatör turlarını yayınlama/gizleme tercihi Entity.
 */
@Serializable
data class AgencyPublishedTourEntity(
    val id: String = "",
    @SerialName("agency_id") val agencyId: String = "",
    @SerialName("tour_id") val tourId: String = "",
    @SerialName("tour_title") val tourTitle: String = "",
    @SerialName("tour_code") val tourCode: String = "",
    @SerialName("operator_name") val operatorName: String = "",
    @SerialName("base_price") val basePrice: Double = 0.0,
    @SerialName("calculated_price") val calculatedPrice: Double = 0.0,
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("custom_price_override") val customPriceOverride: Double? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
