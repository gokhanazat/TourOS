package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rol domain modeli.
 */
@Serializable
data class Role(
    val id: String = "",
    val name: String,
    val description: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("tenant_id") val tenantId: String = "",
    val permissions: List<Permission> = emptyList()
)
