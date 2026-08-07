package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Kiracı (Tenant) domain modeli.
 * Multi-tenant SaaS mimarisinde her tur operatörü bir tenant'tır.
 * RLS politikaları tenant_id üzerinden uygulanır.
 */
@Serializable
data class Tenant(
    val id: String,
    val name: String,
    val slug: String,
    val logoUrl: String? = null,
    val isActive: Boolean = true,
    val plan: TenantPlan = TenantPlan.FREE
)

@Serializable
enum class TenantPlan(val displayName: String) {
    FREE("Ücretsiz"),
    STARTER("Başlangıç"),
    PROFESSIONAL("Profesyonel"),
    ENTERPRISE("Kurumsal")
}
