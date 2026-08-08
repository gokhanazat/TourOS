package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * agency_operator_connections tablosu – Acente ile Tur Operatörü arasındaki Pazaryeri Bağlantısı Entity.
 */
@Serializable
data class AgencyOperatorConnectionEntity(
    val id: String = "",
    @SerialName("agency_id") val agencyId: String = "",
    @SerialName("operator_company_id") val operatorCompanyId: String = "",
    @SerialName("price_adjustment_type") val priceAdjustmentType: String = "percentage", // 'percentage' veya 'fixed'
    @SerialName("price_adjustment_value") val priceAdjustmentValue: Double = 0.0,
    @SerialName("commission_rate") val commissionRate: Double = 10.0,
    val status: String = "ACTIVE", // 'PENDING', 'ACTIVE', 'PAUSED', 'TERMINATED'
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
