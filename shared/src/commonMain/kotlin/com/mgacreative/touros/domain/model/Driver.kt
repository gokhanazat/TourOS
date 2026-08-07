package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.4.2 Şoför Domain Modeli.
 */
@Serializable
data class Driver(
    val id: String = "",
    val fullName: String = "",
    val phone: String? = null,
    val email: String? = null,
    val licenseClass: String? = null,
    val licenseExpiry: String? = null,
    val tcNo: String? = null,
    val isActive: Boolean = true,
    val tenantId: String = ""
)
