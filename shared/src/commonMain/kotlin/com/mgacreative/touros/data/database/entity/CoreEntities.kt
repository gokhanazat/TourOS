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
    @SerialName("company_type") val companyType: String = "tur_operatoru", // tur_operatoru veya acente
    @SerialName("operator_code") val operatorCode: String? = null, // örn. ANK, IST
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("theme_color") val themeColor: String = "#1976D2",
    @SerialName("tax_rate") val taxRate: Double = 20.0,
    val seasons: String = "[]",
    @SerialName("supported_currencies") val supportedCurrencies: List<String> = listOf("TRY", "EUR", "USD"),
    @SerialName("supported_languages") val supportedLanguages: List<String> = listOf("tr", "en"),
    @SerialName("is_active") val isActive: Boolean = true,
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
