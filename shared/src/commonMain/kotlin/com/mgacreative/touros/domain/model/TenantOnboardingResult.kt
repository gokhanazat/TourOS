package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tenant onboarding işleminin sonucunu temsil eden veri modeli.
 */
@Serializable
data class TenantOnboardingResult(
    @SerialName("company_id")
    val companyId: String,
    @SerialName("company_name")
    val companyName: String,
    @SerialName("tenant_id")
    val tenantId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("role")
    val role: String = "SYSTEM_ADMIN"
)
