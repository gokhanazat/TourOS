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
    @SerialName("operator_name") val operatorName: String = "",
    @SerialName("operator_logo") val operatorLogo: String = "",
    @SerialName("operator_type") val operatorType: String = "GLOBAL", // 'GLOBAL', 'DOMESTIC', 'DMC', 'CUSTOM'
    @SerialName("integration_type") val integrationType: String = "API", // 'API', 'SOAP', 'REST', 'MANUAL'
    @SerialName("api_endpoint") val apiEndpoint: String = "",
    @SerialName("api_key") val apiKey: String = "",
    @SerialName("price_adjustment_type") val priceAdjustmentType: String = "percentage", // 'percentage' veya 'fixed'
    @SerialName("price_adjustment_value") val priceAdjustmentValue: Double = 0.0,
    @SerialName("commission_rate") val commissionRate: Double = 10.0,
    @SerialName("currency") val currency: String = "TRY",
    @SerialName("tax_office") val taxOffice: String = "",
    @SerialName("tax_number") val taxNumber: String = "",
    @SerialName("iban") val iban: String = "",
    @SerialName("bank_name") val bankName: String = "",
    @SerialName("contact_name") val contactName: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    @SerialName("contact_email") val contactEmail: String = "",
    val status: String = "ACTIVE", // 'PENDING', 'ACTIVE', 'PAUSED', 'TERMINATED'
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
