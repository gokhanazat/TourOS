package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Temel kullanıcı domain modeli.
 * Supabase Auth profil verileriyle eşleşir.
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val tenantId: String? = null,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false
)
