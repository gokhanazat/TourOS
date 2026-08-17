package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * companies tablosu – Firma (Tenant) entity.
 */
@Serializable
data class CompanyEntity(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    @SerialName("legal_title") val legalTitle: String? = null,
    @SerialName("tax_office") val taxOffice: String? = null,
    @SerialName("tax_number") val taxNumber: String? = null,
    @SerialName("trade_registry_no") val tradeRegistryNo: String? = null,
    @SerialName("mersis_no") val mersisNo: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("company_type") val companyType: String = "tur_operatoru", // tur_operatoru veya acente
    @SerialName("operator_code") val operatorCode: String? = null, // örn. ANK, IST
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("theme_color") val themeColor: String = "#1976D2",
    @SerialName("tax_rate") val taxRate: Double = 20.0,
    val seasons: List<com.mgacreative.touros.domain.model.CompanySeason> = emptyList(),
    @SerialName("supported_currencies") val supportedCurrencies: List<String> = listOf("TRY", "EUR", "USD"),
    @SerialName("supported_languages") val supportedLanguages: List<String> = listOf("tr", "en"),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("bank_name") val bankName: String? = null,
    val iban: String? = null,
    @SerialName("account_holder") val accountHolder: String? = null,
    @SerialName("paypal_email") val paypalEmail: String? = null,
    @SerialName("paypal_me_url") val paypalMeUrl: String? = null,
    @SerialName("default_master_agency_id") val defaultMasterAgencyId: String? = "00000000-0000-0000-0000-000000000001",
    @SerialName("default_master_agency_code") val defaultMasterAgencyCode: String? = "AGN-MASTER",
    @SerialName("header_image_url") val headerImageUrl: String? = null,
    @SerialName("hero_subtitle") val heroSubtitle: String? = null,
    @SerialName("footer_text") val footerText: String? = null,
    @SerialName("web_phone") val webPhone: String? = null,
    @SerialName("web_whatsapp") val webWhatsapp: String? = null,
    @SerialName("web_email") val webEmail: String? = null,
    @SerialName("web_address") val webAddress: String? = null,
    @SerialName("web_mersis_no") val webMersisNo: String? = null,
    @SerialName("web_tax_office") val webTaxOffice: String? = null,
    @SerialName("web_tax_number") val webTaxNumber: String? = null,
    @SerialName("promo_banners") val promoBanners: List<com.mgacreative.touros.domain.model.PromoBannerItem> = emptyList(),
    @SerialName("service_cards") val serviceCards: List<com.mgacreative.touros.domain.model.ServiceCardItem> = emptyList(),
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * roles tablosu – Kullanıcı rolleri entity.
 */
@Serializable
data class RoleEntity(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * permissions tablosu – Yetki tanımları entity.
 */
@Serializable
data class PermissionEntity(
    val id: String = "",
    @SerialName("role_id") val roleId: String = "",
    val resource: String = "",
    val action: String = "",
    @SerialName("is_allowed") val isAllowed: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * users tablosu – Kullanıcı entity.
 */
@Serializable
data class UserEntity(
    val id: String = "",
    @SerialName("auth_id") val authId: String? = null,
    val email: String = "",
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("role_id") val roleId: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
